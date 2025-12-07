package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.FrustumAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PhasingData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    private record Data(Block block, int range, float x, float y, float z, int alpha) {
        public boolean isInRange(final Vec3 position, final int visibleRange) {
            return position.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) <= visibleRange * visibleRange;
        }

        public void render(final VertexConsumer buffer, final PoseStack pose) {
            drawQuads(buffer, pose, x, y, z, x + 1, y + 1, z + 1, alpha);
        }
    }

    @SubscribeEvent
    public static void handlePhasing(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {  // If we use AFTER_TRIPWIRE_BLOCKS we can use depth test
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

        RenderStateShard.TRANSLUCENT_TRANSPARENCY.setupRenderState();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        //RenderSystem.enableDepthTest();
        //GlStateManager._disableCull();
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

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

        RenderStateShard.TRANSLUCENT_TRANSPARENCY.clearRenderState();
        //RenderSystem.disableDepthTest();
        RenderSystem.enableDepthTest();
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

        boolean onEdge = Direction.stream().anyMatch(direction ->
                !getState(position.offset(direction.getNormal())).isSolidRender(player.level(), position.offset(direction.getNormal()))
                // I don't know why the other two below this don't work
                //newState.skipRendering(getState(position.offset(direction.getNormal())), direction)
                //newBlock.hidesNeighborFace(player.level(), position, newState, getState(position.offset(direction.getNormal())), direction)
        );

        if (onEdge) {
            return;
        }

        RENDER_DATA.add(new Data(newBlock, range, x, y, z, alpha));
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

            boolean onEdge = Direction.stream().anyMatch(direction ->
                    !getState(blockPos.offset(direction.getNormal())).isSolidRender(player.level(), blockPos.offset(direction.getNormal()))
                    // I don't know why the other two below this don't work
                    //newState.skipRendering(getState(position.offset(direction.getNormal())), direction)
                    //newBlock.hidesNeighborFace(player.level(), position, newState, getState(position.offset(direction.getNormal())), direction)
            );

            if (onEdge) {
                return;
            }

            SEARCH_RESULT.add(new Data(block, range, x, y, z, alpha));
        });
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

    private static void drawQuads(final VertexConsumer buffer, final PoseStack pose, final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final int alpha) {
        // We should probably only draw quads the player can see - check all normals and if facing > 90 degrees away, cull
        // But that would be difficult to cache
        // Maybe add normals to these? - currently not using any
        Direction.stream().forEach(
                direction -> {
                    LevelRenderer.renderFace(pose, buffer, direction, minX, minY, minZ, maxX, maxY, maxZ, 0, 0, 0, alpha);
                }
        );
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
