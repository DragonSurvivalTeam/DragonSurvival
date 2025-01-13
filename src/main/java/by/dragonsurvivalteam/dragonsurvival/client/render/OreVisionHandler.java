package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.FrustumAccess;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber(Dist.CLIENT)
public class OreVisionHandler {
    private static final int NO_COLOR = -1;
    /** Extend the search as a buffer while the background thread is searching */
    private static final int EXTENDED_SEARCH_RANGE = 32;
    /** Forces all sides to be rendered (boolean amount is proportional to {@link Direction}) */
    private static final boolean[] FULL_DRAW = {true, true, true, true, true, true};

    // TODO
    private static int visibleRange = 32;

    private static Cache<LevelChunkSection, Boolean[]> CHUNK_CACHE;

    private static final List<Data> RENDER_DATA = new ArrayList<>();
    private static final List<Data> SEARCH_RESULT = new ArrayList<>();
    private static final List<BlockPos> REMOVAL = new ArrayList<>();

    private static Vec3 lastPosition;

    private static boolean isSearching;
    private static boolean hasPendingUpdate;

    private record Data(float x, float y, float z, boolean[] renderSides, int color) {
        public boolean isInRange(final Vec3 position, final int visibleRange) {
            return position.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) <= visibleRange * visibleRange;
        }

        public void render(final VertexConsumer buffer, final PoseStack.Pose pose) {
            drawLines(buffer, pose, x, y, z, x + 1, y + 1, z + 1, renderSides, color);
        }
    }

    public static void updateEntry(final BlockPos position, final BlockState oldState, final BlockState newState) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (oldState.getBlock() == newState.getBlock()) {
            // There is no block state property support
            return;
        }

        // Subtract extended range so that they are considered part of the buffered data
        if (lastPosition != null && player.position().distanceToSqr(lastPosition) - EXTENDED_SEARCH_RANGE * EXTENDED_SEARCH_RANGE > visibleRange * visibleRange) {
            return;
        }

        if (!RENDER_DATA.isEmpty() && getColor(oldState) != NO_COLOR) {
            REMOVAL.add(position);
        }

        int color = getColor(newState);

        if (color != NO_COLOR) {
            RENDER_DATA.add(new Data(position.getX(), position.getY(), position.getZ(), FULL_DRAW, color));
        }
    }

    @SubscribeEvent
    public static void clearData(final EntityLeaveLevelEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            RENDER_DATA.clear();
            SEARCH_RESULT.clear();
            REMOVAL.clear();

            lastPosition = null;
            isSearching = false;
            hasPendingUpdate = false;
        }
    }

    @SubscribeEvent
    public static void handleOreVision(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        initCache();

        if (!isSearching && hasPendingUpdate) {
            RENDER_DATA.clear();
            RENDER_DATA.addAll(SEARCH_RESULT);
            SEARCH_RESULT.clear();

            REMOVAL.clear();
            hasPendingUpdate = false;
        }

        if (!isSearching && isOutsideRange(visibleRange)) {
            lastPosition = player.position();
            isSearching = true;

            Util.backgroundExecutor().submit(() -> {
                collect(player, visibleRange + EXTENDED_SEARCH_RANGE);
                isSearching = false;
                hasPendingUpdate = true;
            });
        }

        if (RENDER_DATA.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        pose.mulPose(event.getModelViewMatrix());
        pose.translate(-camera.x(), -camera.y(), -camera.z());

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (int index = 0; index < RENDER_DATA.size(); index++) {
            Data data = RENDER_DATA.get(index);

            if (wasRemoved(data)) {
                // It's more efficient to remove these here (than iterating through all current entries)
                // Since this list would usually be rather small
                RENDER_DATA.remove(index);
                index--;
                continue;
            }

            if (!data.isInRange(player.getEyePosition(), visibleRange)) {
                continue;
            }

            if (((FrustumAccess) event.getFrustum()).dragonSurvival$cubeInFrustum(data.x(), data.y(), data.z(), data.x() + 1, data.y() + 1, data.z() + 1)) {
                data.render(buffer, pose.last());
            }
        }

        MeshData meshData = buffer.build();

        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        pose.popPose();

        RenderSystem.enableDepthTest();
        RenderType.cutout().clearRenderState();
    }

    private static void collect(final Player player, int range) {
        BlockPos startPosition = player.blockPosition();
        ChunkPos currentChunkPosition = new ChunkPos(startPosition);
        LevelChunk currentChunk = null;

        int minChunkX = startPosition.getX() - range;
        int maxChunkX = startPosition.getX() + range;
        int minChunkY = Math.max(player.level().getMinBuildHeight(), startPosition.getY() - range);
        int maxChunkY = Math.min(player.level().getMaxBuildHeight(), startPosition.getY() + range);
        int minChunkZ = startPosition.getZ() - range;
        int maxChunkZ = startPosition.getZ() + range;

        boolean foundSection = false;
        BlockPos.MutableBlockPos mutablePosition = BlockPos.ZERO.mutable();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                int sectionX = SectionPos.blockToSectionCoord(x);
                int sectionZ = SectionPos.blockToSectionCoord(z);

                if (currentChunk == null || currentChunkPosition.x != sectionX || currentChunkPosition.z != sectionZ) {
                    currentChunkPosition = new ChunkPos(sectionX, sectionZ);
                    currentChunk = player.level().getChunk(sectionX, sectionZ);
                }

                foundSection = false;

                for (int y = maxChunkY; y >= minChunkY; y--) {
                    int sectionIndex = currentChunk.getSectionIndex(y);
                    LevelChunkSection section = currentChunk.getSection(sectionIndex);

                    mutablePosition.set(x, y, z);

                    if (foundSection || containsOres(currentChunk, section, sectionIndex)) {
                        foundSection = true;

                        int color = getColor(currentChunk, mutablePosition);

                        if (color != NO_COLOR) {
                            // TODO :: this logic doesn't exactly work anymore due to the buffer range
                            //  previously it was supposed to close the shape
                            //  removing blocks also causes the not rendered lines to now look bad
                            //  maybe possible if you calculate the sides / directions that are now missing lines?
                            //  would require additional checks around the removed block
                            //  generally this logic may just take up too much performance
//                            boolean[] renderSides = new boolean[Direction.values().length];
//
//                            for (Direction direction : Direction.values()) {
//                                BlockPos relative = mutablePosition.relative(direction, 1);
//
//                                if (isWithin(relative, minChunkX, minChunkY, minChunkZ, maxChunkX, maxChunkY, maxChunkZ)) {
//                                    renderSides[direction.ordinal()] = color != getColor(currentChunk, relative);
//                                } else {
//                                    renderSides[direction.ordinal()] = true;
//                                }
//                            }

                            SEARCH_RESULT.add(new Data(x, y, z, FULL_DRAW, color));
                        }
                    }

                    if (!foundSection && y != minChunkY) {
                        // Move to the next section (the bit shifting truncates the y value)
                        y = Math.max(minChunkY, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(y)));
                    }
                }

                if (!foundSection && z != maxChunkZ) {
                    // Move to the next section
                    z = Math.min(maxChunkZ, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(z) + 1));
                }
            }

            if (!foundSection && x != maxChunkX) {
                // Move to the next section
                x = Math.min(maxChunkX, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(x) + 1));
            }
        }
    }

    private static boolean isWithin(final BlockPos position, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        return position.getX() >= xMin && position.getX() <= xMax && position.getY() >= yMin && position.getY() <= yMax && position.getZ() >= zMin && position.getZ() <= zMax;
    }

    private static boolean containsOres(final LevelChunk chunk, final LevelChunkSection section, int sectionIndex) {
        Boolean[] containsOres = CHUNK_CACHE.getIfPresent(section);

        if (containsOres == null || containsOres[sectionIndex] == null) {
            boolean containsOre = !section.hasOnlyAir() && section.maybeHas(state -> getColor(state) != NO_COLOR);

            if (containsOres == null) {
                containsOres = new Boolean[chunk.getSections().length];
            }

            containsOres[sectionIndex] = containsOre;
            CHUNK_CACHE.put(section, containsOres);
        }

        return containsOres[sectionIndex];
    }

    private static int getColor(final LevelChunk chunk, final BlockPos position) {
        if (isWithin(position, chunk.getPos().getMinBlockX(), position.getY(), chunk.getPos().getMinBlockZ(), chunk.getPos().getMaxBlockX(), position.getY(), chunk.getPos().getMaxBlockZ())) {
            return getColor(chunk.getBlockState(position));
        } else {
            // Block is outside the current chunk
            Player player = Minecraft.getInstance().player;
            //noinspection DataFlowIssue -> player is present
            return getColor(player.level().getBlockState(position));
        }
    }

    private static int getColor(final BlockState state) {
        if (state.isAir()) {
            return NO_COLOR;
        }

        Block block = state.getBlock();

        // Try to avoid checking the tags of some very common block types
        if (block == Blocks.BEDROCK || block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.STONE || block == Blocks.DEEPSLATE || block == Blocks.SAND || block == Blocks.GRAVEL) {
            return NO_COLOR;
        }

        if (state.is(Tags.Blocks.ORES)) {
            return DSColors.withAlpha(DSColors.GOLD, 1);
        }

        return NO_COLOR;
    }

    private static boolean wasRemoved(final Data data) {
        for (int i = 0; i < REMOVAL.size(); i++) {
            BlockPos position = REMOVAL.get(i);

            if (position.getX() == data.x() && position.getY() == data.y() && position.getZ() == data.z()) {
                REMOVAL.remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns 'true' if the player moved at least half the distance of their visible range
     * away from the last position that was used as the search origin for the block data
     */
    private static boolean isOutsideRange(int visibleRange) {
        if (lastPosition == null) {
            return true;
        }

        Player player = Minecraft.getInstance().player;
        //noinspection DataFlowIssue -> player is present
        Vec3 currentPosition = player.position();

        float halfRange = visibleRange / 2f;
        return currentPosition.distanceToSqr(lastPosition) > halfRange * halfRange;
    }

    private static void drawLines(final VertexConsumer buffer, final PoseStack.Pose pose, final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final boolean[] renderSides, final int color) {
        boolean renderNegativeY = renderSides[Direction.DOWN.ordinal()];
        boolean renderPositiveY = renderSides[Direction.UP.ordinal()];
        boolean renderNegativeZ = renderSides[Direction.NORTH.ordinal()];
        boolean renderPositiveZ = renderSides[Direction.SOUTH.ordinal()];
        boolean renderNegativeX = renderSides[Direction.WEST.ordinal()];
        boolean renderPositiveX = renderSides[Direction.EAST.ordinal()];

        if (renderNegativeY && renderNegativeZ) {
            drawLine(buffer, pose, minX, minY, minZ, maxX, minY, minZ, 1, 0, 0, color);
        }

        if (renderNegativeX && renderNegativeZ) {
            drawLine(buffer, pose, minX, minY, minZ, minX, maxY, minZ, 0, 1, 0, color);
        }

        if (renderNegativeX && renderNegativeY) {
            drawLine(buffer, pose, minX, minY, minZ, minX, minY, maxZ, 0, 0, 1, color);
        }

        if (renderPositiveX && renderNegativeZ) {
            drawLine(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, 0, 1, 0, color);
        }

        if (renderPositiveY && renderNegativeZ) {
            drawLine(buffer, pose, maxX, maxY, minZ, minX, maxY, minZ, -1, 0, 0, color);
        }

        if (renderPositiveY && renderNegativeX) {
            drawLine(buffer, pose, minX, maxY, minZ, minX, maxY, maxZ, 0, 0, 1, color);
        }

        if (renderPositiveZ && renderNegativeX) {
            drawLine(buffer, pose, minX, maxY, maxZ, minX, minY, maxZ, 0, -1, 0, color);
        }

        if (renderPositiveZ && renderNegativeY) {
            drawLine(buffer, pose, minX, minY, maxZ, maxX, minY, maxZ, 1, 0, 0, color);
        }

        if (renderPositiveX && renderNegativeY) {
            drawLine(buffer, pose, maxX, minY, maxZ, maxX, minY, minZ, 0, 0, -1, color);
        }

        if (renderPositiveY && renderPositiveZ) {
            drawLine(buffer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, 1, 0, 0, color);
        }

        if (renderPositiveX && renderPositiveZ) {
            drawLine(buffer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, 0, 1, 0, color);
        }

        if (renderPositiveX && renderPositiveY) {
            drawLine(buffer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, 0, 0, 1, color);
        }
    }

    private static void drawLine(final VertexConsumer buffer, final PoseStack.Pose pose, float fromX, float fromY, float fromZ, float toX, float toY, float toZ, int normalX, int normalY, int normalZ, final int color) {
        buffer.addVertex(pose, fromX, fromY, fromZ).setColor(color).setNormal(pose, normalX, normalY, normalZ);
        buffer.addVertex(pose, toX, toY, toZ).setColor(color).setNormal(pose, normalX, normalY, normalZ);
    }

    private static void initCache() {
        if (CHUNK_CACHE == null) {
            CHUNK_CACHE = CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS)
                    .concurrencyLevel(1)
                    .build();
        }
    }
}
