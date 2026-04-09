package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.client.render.block_vision.BlockVisionOutline;
import by.dragonsurvivalteam.dragonsurvival.client.render.block_vision.BlockVisionParticle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.BlockVisionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Searches nearby chunks for blocks affected by Block Vision and drives the client-side visuals.
 */
@EventBusSubscriber(Dist.CLIENT)
public final class BlockVisionHandler {
    /** Extend the search as a buffer while the background thread is searching. */
    private static final int EXTENDED_SEARCH_RANGE = 16;

    private static Cache<LevelChunkSection, Boolean> chunkCache;

    private static final List<Data> renderData = new ArrayList<>();
    private static final List<BlockPos> removals = new ArrayList<>();

    private static volatile List<Data> pendingSearchResult = List.of();
    private static volatile boolean isSearching;
    private static volatile boolean hasPendingUpdate;

    private static BlockVisionData vision;
    private static Vec3 lastScanCenter;
    private static int previousStorage;

    private BlockVisionHandler() {}

    public record Data(BlockState state, int range, BlockVision.DisplayType displayType, int particleRate, float x, float y, float z) {
        public boolean isInRange(final Vec3 position, final int visibleRange) {
            return position.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) <= visibleRange * visibleRange;
        }
    }

    @SubscribeEvent
    public static void handleRender(final RenderLevelStageEvent.AfterTranslucentParticles event) {
        if (Compat.isRenderingShadows()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        BlockVisionData currentVision = player.getExistingData(DSDataAttachments.BLOCK_VISION).orElse(null);
        vision = currentVision;

        if (currentVision == null || currentVision.isEmpty()) {
            clear();
            return;
        }

        if (previousStorage != currentVision.size()) {
            clear();
            vision = currentVision;
        }

        previousStorage = currentVision.size();
        initCache();

        if (!isSearching && hasPendingUpdate) {
            renderData.clear();
            renderData.addAll(pendingSearchResult);
            pendingSearchResult = List.of();
            removals.clear();
            hasPendingUpdate = false;
        }

        int visibleRange = currentVision.getRange(null);

        if (!isSearching && isOutsideRange(visibleRange)) {
            lastScanCenter = player.position();
            isSearching = true;

            CompletableFuture.runAsync(() -> {
                pendingSearchResult = collect(player, currentVision, visibleRange + EXTENDED_SEARCH_RANGE);
                isSearching = false;
                hasPendingUpdate = true;
            });
        }

        Vec3 cameraPosition = event.getLevelRenderState().cameraRenderState.pos;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lineBuffer = bufferSource.getBuffer(BlockVisionOutline.renderType());
        PoseStack pose = event.getPoseStack();

        pose.pushPose();
        pose.translate(-cameraPosition.x(), -cameraPosition.y(), -cameraPosition.z());

        for (int index = 0; index < renderData.size(); index++) {
            Data data = renderData.get(index);

            if (wasRemoved(data)) {
                renderData.remove(index--);
                continue;
            }

            if (data.range() == BlockVision.NO_RANGE || !data.isInRange(player.getEyePosition(), data.range())) {
                continue;
            }

            switch (data.displayType()) {
                case OUTLINE -> BlockVisionOutline.render(data, pose, lineBuffer, currentVision.getColor(data.state().getBlock()));
                case PARTICLES -> BlockVisionParticle.spawnParticle(data, player);
                case NONE -> { }
            }
        }

        pose.popPose();
        bufferSource.endBatch(BlockVisionOutline.renderType());
    }

    @SubscribeEvent
    public static void clearData(final EntityLeaveLevelEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) {
            clear();
        }
    }

    public static void updateEntry(final BlockPos position, final BlockState oldState, final BlockState newState) {
        if (oldState == null || newState == null) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || vision == null) {
            return;
        }

        Block newBlock = newState.getBlock();

        if (oldState.getBlock() == newBlock) {
            return;
        }

        int searchRange = vision.getRange(null);

        if (searchRange == BlockVision.NO_RANGE) {
            return;
        }

        // Subtract the extended range so buffered data is still considered current.
        if (lastScanCenter != null && player.position().distanceToSqr(lastScanCenter) - EXTENDED_SEARCH_RANGE * EXTENDED_SEARCH_RANGE > searchRange * searchRange) {
            return;
        }

        if (!renderData.isEmpty() && vision.getRange(oldState.getBlock()) != BlockVision.NO_RANGE) {
            removals.add(position);
        }

        int range = vision.getRange(newBlock);

        if (range != BlockVision.NO_RANGE) {
            renderData.add(new Data(newState, range, vision.getDisplayType(newBlock), vision.getParticleRate(newBlock), position.getX(), position.getY(), position.getZ()));
        }
    }

    /** The searching algorithm is referenced from TelepathicGrunt's Bumblezone project. */
    private static List<Data> collect(final Player player, final BlockVisionData visionData, final int searchRange) {
        List<Data> searchResult = new ArrayList<>();
        BlockPos startPosition = player.blockPosition();
        ChunkPos currentChunkPosition = new ChunkPos(SectionPos.blockToSectionCoord(startPosition.getX()), SectionPos.blockToSectionCoord(startPosition.getZ()));
        LevelChunk currentChunk = null;

        int minChunkX = startPosition.getX() - searchRange;
        int maxChunkX = startPosition.getX() + searchRange;
        int minChunkY = startPosition.getY() - searchRange;
        int maxChunkY = startPosition.getY() + searchRange;
        int minChunkZ = startPosition.getZ() - searchRange;
        int maxChunkZ = startPosition.getZ() + searchRange;

        BlockPos.MutableBlockPos mutablePosition = BlockPos.ZERO.mutable();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            boolean foundXSection = false;

            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                boolean foundZSection = false;

                int sectionX = SectionPos.blockToSectionCoord(x);
                int sectionZ = SectionPos.blockToSectionCoord(z);

                if (currentChunk == null || currentChunkPosition.x() != sectionX || currentChunkPosition.z() != sectionZ) {
                    currentChunkPosition = new ChunkPos(sectionX, sectionZ);
                    currentChunk = player.level().getChunk(sectionX, sectionZ);
                }

                for (int y = maxChunkY; y >= minChunkY; y--) {
                    int sectionIndex = currentChunk.getSectionIndex(y);

                    if (sectionIndex < 0 || sectionIndex >= currentChunk.getSections().length) {
                        continue;
                    }

                    LevelChunkSection section = currentChunk.getSection(sectionIndex);

                    mutablePosition.set(x, y, z);

                    if (foundXSection || foundZSection || containsRelevantBlocks(section, visionData)) {
                        foundXSection = true;
                        foundZSection = true;

                        BlockState state = getState(player, currentChunk, mutablePosition);

                        if (state.isAir()) {
                            continue;
                        }

                        Block block = state.getBlock();
                        int range = visionData.getRange(block);

                        if (range != BlockVision.NO_RANGE) {
                            searchResult.add(new Data(state, range, visionData.getDisplayType(block), visionData.getParticleRate(block), x, y, z));
                        }
                    } else if (y != minChunkY) {
                        y = Math.max(minChunkY, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(y)));
                    }
                }

                if (!foundZSection && z != maxChunkZ) {
                    z = Math.min(maxChunkZ, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(z) + 1));
                }
            }

            if (!foundXSection && x != maxChunkX) {
                x = Math.min(maxChunkX, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(x) + 1));
            }
        }

        return searchResult;
    }

    private static boolean isWithin(final BlockPos position, final int xMin, final int yMin, final int zMin, final int xMax, final int yMax, final int zMax) {
        return position.getX() >= xMin && position.getX() <= xMax && position.getY() >= yMin && position.getY() <= yMax && position.getZ() >= zMin && position.getZ() <= zMax;
    }

    private static boolean containsRelevantBlocks(final LevelChunkSection section, final BlockVisionData visionData) {
        Boolean cached = chunkCache.getIfPresent(section);

        if (cached == null) {
            cached = !section.hasOnlyAir() && section.maybeHas(state -> visionData.getRange(state.getBlock()) != BlockVision.NO_RANGE);
            chunkCache.put(section, cached);
        }

        return cached;
    }

    private static BlockState getState(final Player player, final LevelChunk chunk, final BlockPos position) {
        if (isWithin(position, chunk.getPos().getMinBlockX(), position.getY(), chunk.getPos().getMinBlockZ(), chunk.getPos().getMaxBlockX(), position.getY(), chunk.getPos().getMaxBlockZ())) {
            return chunk.getBlockState(position);
        }

        return player.level().getBlockState(position);
    }

    private static boolean wasRemoved(final Data data) {
        for (int i = 0; i < removals.size(); i++) {
            BlockPos position = removals.get(i);

            if (position.getX() == data.x() && position.getY() == data.y() && position.getZ() == data.z()) {
                removals.remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if the player moved at least half their visible range
     * away from the last search origin.
     */
    private static boolean isOutsideRange(final int visibleRange) {
        if (lastScanCenter == null) {
            return true;
        }

        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return false;
        }

        float halfRange = visibleRange / 2f;
        return player.position().distanceToSqr(lastScanCenter) > halfRange * halfRange;
    }

    private static void clear() {
        renderData.clear();
        removals.clear();
        pendingSearchResult = List.of();

        lastScanCenter = null;
        previousStorage = 0;
        isSearching = false;
        hasPendingUpdate = false;
        vision = null;

        if (chunkCache != null) {
            chunkCache.invalidateAll();
            chunkCache = null;
        }
    }

    private static void initCache() {
        if (chunkCache == null) {
            chunkCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS)
                    .concurrencyLevel(1)
                    .build();
        }
    }
}
