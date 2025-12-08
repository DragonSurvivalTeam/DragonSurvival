package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.client.render.block_vision.BlockVisionOutline;
import by.dragonsurvivalteam.dragonsurvival.client.render.block_vision.BlockVisionParticle;
import by.dragonsurvivalteam.dragonsurvival.client.render.block_vision.BlockVisionShaderSimple;
import by.dragonsurvivalteam.dragonsurvival.client.render.block_vision.BlockVisionTreasure;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.FrustumAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.BlockVisionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Currently, these effects are rendered for blocks that are in the players' vision but hidden behind other blocks </br>
 * TODO :: find a performant way to check and potentially skip such blocks
 */
@EventBusSubscriber(Dist.CLIENT)
public class BlockVisionHandler {
    /** Extend the search as a buffer while the background thread is searching */
    private static final int EXTENDED_SEARCH_RANGE = 16;

    private static Cache<LevelChunkSection, Boolean[]> CHUNK_CACHE;

    private static final List<Data> RENDER_DATA = new ArrayList<>();
    private static final List<Data> SHADER_RENDER_DATA = new ArrayList<>();
    private static final List<Data> SEARCH_RESULT = new ArrayList<>();
    private static final List<BlockPos> REMOVAL = new ArrayList<>();

    private static BlockVisionData vision;
    private static Vec3 lastScanCenter;

    private static int previousStorage;
    private static boolean isSearching;
    private static boolean hasPendingUpdate;

    public record Data(BlockState state, int range, BlockVision.DisplayType displayType, int particleRate, float x, float y, float z) {
        public boolean isInRange(final Vec3 position, final int visibleRange) {
            return position.distanceToSqr(x + 0.5, y + 0.5, z + 0.5) <= visibleRange * visibleRange;
        }
    }

    /**
     * Handles normal effects </br>
     * This allows for x-ray effects that do not render over the entity, since this tage occurs before entities are rendered
     */
    @SubscribeEvent
    public static void handleCore(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }

        if (Compat.isRenderingShadows()) {
            return;
        }

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        vision = player.getExistingData(DSDataAttachments.BLOCK_VISION).orElse(null);

        if (vision == null || vision.isEmpty()) {
            clear();
            return;
        }

        if (previousStorage != vision.size()) {
            // Reset the data since entries have been adjusted
            clear();
        }

        previousStorage = vision.size();
        initCache();

        if (!isSearching && hasPendingUpdate) {
            RENDER_DATA.clear();
            RENDER_DATA.addAll(SEARCH_RESULT);
            SEARCH_RESULT.clear();

            REMOVAL.clear();
            hasPendingUpdate = false;
        }

        if (!isSearching && isOutsideRange(vision.getRange(null))) {
            lastScanCenter = player.position();
            isSearching = true;

            Util.backgroundExecutor().submit(() -> {
                collect(player, vision.getRange(null) + EXTENDED_SEARCH_RANGE);
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

        for (int index = 0; index < RENDER_DATA.size(); index++) {
            Data data = RENDER_DATA.get(index);

            if (wasRemoved(data)) {
                // It's more efficient to remove these here (than iterating through all current entries)
                // Since this list would usually be rather small
                RENDER_DATA.remove(index);
                index--;
                continue;
            }

            if (data.range() == BlockVision.NO_RANGE || !data.isInRange(player.getEyePosition(), data.range())) {
                continue;
            }

            if (((FrustumAccess) event.getFrustum()).dragonSurvival$cubeInFrustum(data.x(), data.y(), data.z(), data.x() + 1, data.y() + 1, data.z() + 1)) {
                pose.pushPose();
                pose.translate(data.x(), data.y(), data.z());

                int colorARGB = vision.getColor(data.state().getBlock());
                int tick = event.getRenderTick();
                float partialTick = event.getPartialTick().getRealtimeDeltaTicks();

                switch (data.displayType()) {
                    case OUTLINE -> BlockVisionOutline.render(data, pose, colorARGB);
                    case PARTICLES -> BlockVisionParticle.spawnParticle(data, player);
                    case TREASURE -> BlockVisionTreasure.render(data, pose, colorARGB, tick, partialTick);
                    case TREASURE_SHADER -> SHADER_RENDER_DATA.add(data);
                }

                pose.popPose();
            }
        }

        pose.popPose();
    }

    /**
     * Renders effects that rely on core shaders </br>
     * When shaders from Iris are enabled they won't appear on the {@link RenderLevelStageEvent.Stage#AFTER_CUTOUT_BLOCKS} stage </br>
     * This also means we can't allow x-ray functionality for these because otherwise they'd render over entities
     */
    @SubscribeEvent
    public static void handleShader(final RenderLevelStageEvent event) {
        if (Compat.isRenderingShadows()) {
            return;
        }

        boolean isUsingShader = Compat.isShaderActive();

        if (isUsingShader && event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            // Iris does not really support core shaders - the only stage they work at is after it has finished doing its rendering
            return;
        } else if (!isUsingShader && event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            // This stage allows rendering the shader through water
            return;
        }

        if (SHADER_RENDER_DATA.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        pose.mulPose(event.getModelViewMatrix());
        pose.translate(-camera.x(), -camera.y(), -camera.z());

        BlockVisionShaderSimple.beginBatch();

        for (Data data : SHADER_RENDER_DATA) {
            pose.pushPose();
            pose.translate(data.x(), data.y(), data.z());

            int colorARGB = vision.getColor(data.state().getBlock());

            switch (data.displayType()) {
                case TREASURE_SHADER -> BlockVisionShaderSimple.render(data, pose, colorARGB);
            }

            pose.popPose();
        }

        BlockVisionShaderSimple.endBatch();

        pose.popPose();
        SHADER_RENDER_DATA.clear();
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

        if (player == null || vision == null) {
            return;
        }

        Block newBlock = newState.getBlock();

        if (oldState.getBlock() == newBlock) {
            // There is no block state property support
            return;
        }

        int searchRange = vision.getRange(null);

        // Subtract extended range so that they are considered part of the buffered data
        if (lastScanCenter != null && player.position().distanceToSqr(lastScanCenter) - EXTENDED_SEARCH_RANGE * EXTENDED_SEARCH_RANGE > searchRange * searchRange) {
            return;
        }

        if (!RENDER_DATA.isEmpty() && vision.getRange(oldState.getBlock()) != BlockVision.NO_RANGE) {
            REMOVAL.add(position);
        }

        int range = vision.getRange(newBlock);

        if (range != BlockVision.NO_RANGE) {
            RENDER_DATA.add(new Data(newState, range, vision.getDisplayType(newBlock), vision.getParticleRate(newBlock), position.getX(), position.getY(), position.getZ()));
        }
    }

    /** The searching algorithm is referenced from <a href="https://github.com/TelepathicGrunt/Bumblezone/blob/d4b2a29d7075749e1f4e8289debbc4cef3fc74c4/common/src/main/java/com/telepathicgrunt/the_bumblezone/items/essence/LifeEssence.java#L127">TelepathicGrunt</a> */
    private static void collect(final Player player, int searchRange) {
        BlockPos startPosition = player.blockPosition();
        ChunkPos currentChunkPosition = new ChunkPos(startPosition);
        LevelChunk currentChunk = null;

        int minChunkX = startPosition.getX() - searchRange;
        int maxChunkX = startPosition.getX() + searchRange;
        int minChunkY = Math.max(player.level().getMinBuildHeight(), startPosition.getY() - searchRange);
        int maxChunkY = Math.min(player.level().getMaxBuildHeight(), startPosition.getY() + searchRange);
        int minChunkZ = startPosition.getZ() - searchRange;
        int maxChunkZ = startPosition.getZ() + searchRange;

        BlockPos.MutableBlockPos mutablePosition = BlockPos.ZERO.mutable();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            boolean foundXSection = false;

            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                boolean foundZSection = false;

                int sectionX = SectionPos.blockToSectionCoord(x);
                int sectionZ = SectionPos.blockToSectionCoord(z);

                if (currentChunk == null || currentChunkPosition.x != sectionX || currentChunkPosition.z != sectionZ) {
                    currentChunkPosition = new ChunkPos(sectionX, sectionZ);
                    currentChunk = player.level().getChunk(sectionX, sectionZ);
                }

                for (int y = maxChunkY; y >= minChunkY; y--) {
                    int sectionIndex = currentChunk.getSectionIndex(y);
                    LevelChunkSection section = currentChunk.getSection(sectionIndex);

                    mutablePosition.set(x, y, z);

                    if (foundXSection || foundZSection || containsOres(currentChunk, section, sectionIndex)) {
                        foundXSection = true;
                        foundZSection = true;

                        BlockState state = getState(currentChunk, mutablePosition);

                        if (state.isAir()) {
                            continue;
                        }

                        Block block = state.getBlock();
                        int range = vision.getRange(block);

                        if (range != BlockVision.NO_RANGE) {
                            SEARCH_RESULT.add(new Data(state, range, vision.getDisplayType(block), vision.getParticleRate(block), x, y, z));
                        }
                    } else if (y != minChunkY) {
                        // Move to the next section (the bit shifting truncates the y value)
                        y = Math.max(minChunkY, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(y)));
                    }
                }

                if (!foundZSection && z != maxChunkZ) {
                    // Move to the next section
                    z = Math.min(maxChunkZ, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(z) + 1));
                }
            }

            if (!foundXSection && x != maxChunkX) {
                // Move to the next section
                x = Math.min(maxChunkX, SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(x) + 1));
            }
        }
    }

    private static boolean isWithin(final BlockPos position, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        return position.getX() >= xMin && position.getX() <= xMax && position.getY() >= yMin && position.getY() <= yMax && position.getZ() >= zMin && position.getZ() <= zMax;
    }

    private static boolean containsOres(final LevelChunk chunk, final LevelChunkSection section, int sectionIndex) {
        Boolean[] cachedSection = CHUNK_CACHE.getIfPresent(section);

        if (cachedSection == null || cachedSection[sectionIndex] == null) {
            boolean containsRelevantBlock = !section.hasOnlyAir() && section.maybeHas(state -> vision.getRange(state.getBlock()) != BlockVision.NO_RANGE);

            if (cachedSection == null) {
                cachedSection = new Boolean[chunk.getSections().length];
            }

            cachedSection[sectionIndex] = containsRelevantBlock;
            CHUNK_CACHE.put(section, cachedSection);
        }

        return cachedSection[sectionIndex];
    }

    private static BlockState getState(final LevelChunk chunk, final BlockPos position) {
        if (isWithin(position, chunk.getPos().getMinBlockX(), position.getY(), chunk.getPos().getMinBlockZ(), chunk.getPos().getMaxBlockX(), position.getY(), chunk.getPos().getMaxBlockZ())) {
            return chunk.getBlockState(position);
        } else {
            // Block is outside the current chunk
            Player player = Minecraft.getInstance().player;
            //noinspection DataFlowIssue -> player is present
            return player.level().getBlockState(position);
        }
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
