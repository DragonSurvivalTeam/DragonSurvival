package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.FrustumAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PhasingData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PhasingData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EventBusSubscriber(Dist.CLIENT)
public class PhasingHandler {
    /** Extend the search as a buffer while the background thread is searching */
    private static final int EXTENDED_SEARCH_RANGE = 16;

    private static Cache<LevelChunkSection, Boolean[]> CHUNK_CACHE;

    private static final List<Data> RENDER_DATA = new ArrayList<>();
    private static final List<Data> SEARCH_RESULT = new ArrayList<>();
    private static final List<BlockPos> REMOVAL = new ArrayList<>();

    private static PhasingData phase;
    private static Vec3 lastScanCenter;

    private static boolean isSearching;
    private static boolean hasPendingUpdate;

    private record Data(Block block, Map<Direction, Boolean> validFaces, int range, float x, float y, float z, int alpha) {
        public boolean isInRange(final Vec3 position, final int visibleRange) {
            return position.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) <= visibleRange * visibleRange;
        }

        public void render(final VertexConsumer buffer, final PoseStack pose) {
            drawQuads(buffer, validFaces, pose, x, y, z, x + 1, y + 1, z + 1, alpha);
        }
    }

    @SubscribeEvent
    public static void handlePhasing(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        phase = player.getExistingData(DSDataAttachments.PHASING).orElse(null);

        if (phase == null || phase.isEmpty()) {
            clear();
            return;
        }

        initCache();

        if (!isSearching && hasPendingUpdate) {
            RENDER_DATA.clear();
            RENDER_DATA.addAll(SEARCH_RESULT);
            SEARCH_RESULT.clear();

            REMOVAL.clear();
            hasPendingUpdate = false;
        }

        if (!isSearching && isOutsideRange(phase.getRange(null))) {
            lastScanCenter = player.position();
            isSearching = true;

            Util.backgroundExecutor().submit(() -> {
                collect(player, phase.getRange(null) + EXTENDED_SEARCH_RANGE);
                isSearching = false;
                hasPendingUpdate = true;
            }); // We don't want to search for specific blocks here unless we cache the alpha
        }

        if (RENDER_DATA.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        pose.mulPose(event.getModelViewMatrix());
        pose.translate(-camera.x(), -camera.y(), -camera.z());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.TRANSLUCENT);

        for (int index = 0; index < RENDER_DATA.size(); index++) {
            Data data = RENDER_DATA.get(index);

            if (wasRemoved(data)) {
                // It's more efficient to remove these here (than iterating through all current entries)
                // Since this list would usually be rather small
                RENDER_DATA.remove(index);
                index--;
                continue;
            }

            if (!player.isInWall() ||!data.isInRange(player.getEyePosition(), data.range())) {
                continue;
            }

            if (((FrustumAccess) event.getFrustum()).dragonSurvival$cubeInFrustum(data.x(), data.y(), data.z(), data.x() + 1, data.y() + 1, data.z() + 1)) {
                data.render(buffer, pose);
            }
        }

        MeshData meshData = buffer.build();

        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        pose.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderType.translucent().clearRenderState();
    }

    @SubscribeEvent
    public static void clearData(final EntityLeaveLevelEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            clear();
        }
    }

    public static void updateEntry(final BlockPos position, final BlockState oldState, final BlockState newState) {
        if (oldState == null || newState == null) {
            // Should not be the case, but mods do weird things
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || phase == null) {
            return;
        }

        Block newBlock = newState.getBlock();

        if (oldState.getBlock() == newBlock) {
            // There is no block state property support
            return;
        }

        int searchRange = phase.getRange(null);

        // Subtract extended range so that they are considered part of the buffered data
        if (lastScanCenter != null && player.position().distanceToSqr(lastScanCenter) - EXTENDED_SEARCH_RANGE * EXTENDED_SEARCH_RANGE > searchRange * searchRange) {
            return;
        }

        if (!RENDER_DATA.isEmpty() && phase.getRange(oldState.getBlock()) != Phasing.NO_RANGE) {
            REMOVAL.add(position);
        }

        int range = phase.getRange(newBlock);
        int alpha = phase.getAlpha(newBlock);
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();

        // We only want to render faces that aren't already
        Map<Direction, Boolean> validFaces = Direction.stream().collect(Collectors.toMap(direction -> direction, direction ->
                //newBlock.hidesNeighborFace(player.level(), position, newState, getState(position.offset(direction.getNormal())), direction)
                true
        ));

        RENDER_DATA.add(new Data(newBlock, validFaces, range, x, y, z, alpha));
    }

    private static void collect(final Player player, int searchRange) {
        // We can't cache the same way as BlockVision - Phasing would cache far too many blocks
        // Instead, cache the range or maybe slightly more
        BlockPos startPosition = player.blockPosition();

        int cacheRange = phase.getRange(null);

        Stream<BlockPos> blockStream = BlockPos.withinManhattanStream(startPosition, cacheRange, cacheRange, cacheRange);

        blockStream.forEach(blockPos -> {
            BlockState state = getState(blockPos);
            if (state.isAir()) {
                return;
            }
            Block block = state.getBlock();
            int range = phase.getRange(block);
            int alpha = phase.getAlpha(block);
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();

            // We only want to render faces that aren't already
            Map<Direction, Boolean> validFaces = Direction.stream().collect(Collectors.toMap(direction -> direction, direction ->
                //block.hidesNeighborFace(player.level(), blockPos, state, getState(blockPos.offset(direction.getNormal())), direction)
                true
            ));

            SEARCH_RESULT.add(new Data(block, validFaces, range, x, y, z, alpha));
        });
    }

    private static boolean isWithin(final BlockPos position, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        return position.getX() >= xMin && position.getX() <= xMax && position.getY() >= yMin && position.getY() <= yMax && position.getZ() >= zMin && position.getZ() <= zMax;
    }

    private static BlockState getState(final BlockPos position) {
        Player player = Minecraft.getInstance().player;
        //noinspection DataFlowIssue -> player is present
        return player.level().getBlockState(position);
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
        if (lastScanCenter == null) {
            return true;
        }

        Player player = Minecraft.getInstance().player;
        //noinspection DataFlowIssue -> player is present
        Vec3 currentPosition = player.position();

        float halfRange = visibleRange / 2f;
        return currentPosition.distanceToSqr(lastScanCenter) > halfRange * halfRange;
    }

    private static void drawQuads(final VertexConsumer buffer, final Map<Direction, Boolean> validFaces, final PoseStack pose, final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final int alpha) {
        // We should probably only draw quads the player can see - check all normals and if facing > 90 degrees away, cull
        // But that would be difficult to cache
        Direction.stream().filter(validFaces::get).forEach(
                direction -> {
                    LevelRenderer.renderFace(pose, buffer, direction, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
                }
        );
        //LevelRenderer.renderFace(pose, buffer, Direction.DOWN, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
        //LevelRenderer.renderFace(pose, buffer, Direction.UP, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
        //LevelRenderer.renderFace(pose, buffer, Direction.NORTH, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
        //LevelRenderer.renderFace(pose, buffer, Direction.EAST, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
        //LevelRenderer.renderFace(pose, buffer, Direction.SOUTH, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
        //LevelRenderer.renderFace(pose, buffer, Direction.WEST, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
        //drawQuad(buffer, pose, minX, minY, minZ, maxX, maxY, minZ, 0, 0, -1, alpha); // Bottom
        //drawQuad(buffer, pose, minX, minY, minZ, maxX, minY, maxZ, 0, -1, 0, alpha); // Left
        //drawQuad(buffer, pose, minX, minY, minZ, minX, maxY, maxZ, -1, 0, 0, alpha); // Front
        //drawQuad(buffer, pose, maxX, maxY, maxZ, minX, minY, maxZ, 0, 0, 1, alpha); // Top
        //drawQuad(buffer, pose, maxX, maxY, maxZ, minX, maxY, minZ, 0, 1, 0, alpha); // Right
        //drawQuad(buffer, pose, maxX, maxY, maxZ, maxX, minY, minZ, 1, 0, 0, alpha); // Back
    }

    private static void drawQuad(final VertexConsumer buffer, final Matrix4f pose, float fromX, float fromY, float fromZ, float toX, float toY, float toZ, int normalX, int normalY, int normalZ, final int alpha) {
        buffer.addVertex(pose, fromX, fromY, fromZ).setColor(0, 0, 0, alpha);
        buffer.addVertex(pose, fromX, fromY, toZ).setColor(0, 0, 0, alpha);
        buffer.addVertex(pose, fromX, toY, toZ).setColor(0, 0, 0, alpha);
        buffer.addVertex(pose, toX, toY, fromZ).setColor(0, 0, 0, alpha);
    }

    private static void clear() {
        if (CHUNK_CACHE == null) {
            // There is nothing to clean up
            return;
        }

        RENDER_DATA.clear();
        SEARCH_RESULT.clear();
        REMOVAL.clear();

        lastScanCenter = null;
        isSearching = false;
        hasPendingUpdate = false;

        CHUNK_CACHE.invalidateAll();
        CHUNK_CACHE = null;
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
