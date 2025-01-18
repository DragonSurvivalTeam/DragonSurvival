package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import it.unimi.dsi.fastutil.Pair;
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
import java.util.Map;

@EventBusSubscriber
public class BlockVisionData extends Storage<BlockVision.Instance> {
    private final Map<Block, Pair</* Range */ Integer, /* Color */ Integer>> cache = new HashMap<>();
    private int maximumRange = -1;

    public int getRange(@Nullable final Block block) {
        if (block == null) {
            if (maximumRange == -1) {
                maximumRange = storeRange(null);
            }

            return maximumRange;
        }

        return cache.computeIfAbsent(block, this::storeData).first();
    }

    public int getColor(final Block block) {
        return cache.computeIfAbsent(block, this::storeData).second();
    }

    private Pair<Integer, Integer> storeData(final Block block) {
        return Pair.of(storeRange(block), storeColor(block));
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

    private int storeColor(final Block block) {
        for (BlockVision.Instance instance : all()) {
            int color = instance.getColor(block);

            if (color != BlockVision.NO_COLOR) {
                return DSColors.withAlpha(color, 1);
            }
        }

        return BlockVision.NO_COLOR;
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
