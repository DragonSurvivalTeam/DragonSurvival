package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class BlockVisionData extends Storage<BlockVision.Instance> {
    private final Map<Block, CacheEntry> cache = new HashMap<>();
    private int maximumRange = -1;

    record CacheEntry(int range, List<Integer> colors, BlockVision.DisplayType displayType, int particleRate, double colorShiftRate) {}

    public int getRange(@Nullable final Block block) {
        if (block == null) {
            if (maximumRange == -1) {
                maximumRange = storeRange(null);
            }

            return maximumRange;
        }

        return cache.computeIfAbsent(block, this::storeData).range();
    }

    public int getColor(final Block block) {
        return Functions.lerpColor(cache.computeIfAbsent(block, this::storeData).colors(), cache.computeIfAbsent(block, this::storeData).colorShiftRate(), 0);
    }

    public List<Integer> getColors(final Block block) {
        return cache.computeIfAbsent(block, this::storeData).colors();
    }

    public BlockVision.DisplayType getDisplayType(final Block block) {
        return cache.computeIfAbsent(block, this::storeData).displayType();
    }

    public int getParticleRate(final Block block) {
        return cache.computeIfAbsent(block, this::storeData).particleRate();
    }

    private CacheEntry storeData(final Block block) {
        return new CacheEntry(storeRange(block), storeColor(block), storeDisplayType(block), storeParticleRate(block), storeColorShiftRate(block));
    }

    /** If the passed state is 'null' it will return the range as well */
    private int storeRange(@Nullable final Block block) {
        int currentRange = BlockVision.NO_RANGE;

        for (BlockVision.Instance instance : all()) {
            int range = instance.getRange(block);

            if (range > currentRange) {
                currentRange = range;
            }
        }

        return currentRange;
    }

    private List<Integer> storeColor(final Block block) {
        for (BlockVision.Instance instance : all()) {
            List<Integer> colors = instance.getColors(block);

            if (!colors.isEmpty()) {
                return colors;
            }
        }

        return List.of();
    }

    private BlockVision.DisplayType storeDisplayType(final Block block) {
        for (BlockVision.Instance instance : all()) {
            BlockVision.DisplayType displayType = instance.getDisplayType(block);

            if (displayType != BlockVision.DisplayType.NONE) {
                return displayType;
            }
        }

        return BlockVision.DisplayType.NONE;
    }

    private int storeParticleRate(final Block block) {
        for (BlockVision.Instance instance : all()) {
            int particleRate = instance.getParticleRate(block);

            if (particleRate != BlockVision.NO_VALUE) {
                return particleRate;
            }
        }

        return BlockVision.NO_VALUE;
    }

    private double storeColorShiftRate(final Block block) {
        for (BlockVision.Instance instance : all()) {
            double colorShiftRate = instance.getColorShiftRate(block);

            if (colorShiftRate != BlockVision.NO_VALUE) {
                return colorShiftRate;
            }
        }

        return BlockVision.NO_VALUE;
    }

    @Override
    public void invalidateCache() {
        cache.clear();
        maximumRange = -1;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.BLOCK_VISION).ifPresent(storage -> {
                storage.tick(player);

                if (storage.isEmpty()) {
                    player.removeData(DSDataAttachments.BLOCK_VISION);
                }
            });
        }
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final BlockVision.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected BlockVision.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return BlockVision.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.BLOCK_VISION.value();
    }
}
