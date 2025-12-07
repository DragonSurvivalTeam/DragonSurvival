package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class PhasingData extends Storage<Phasing.Instance> {
    private final Map<Block, PhasingData.CacheEntry> cache = new HashMap<>();
    private int maximumRange = -1;
    private int minimumAlpha = 255;

    record CacheEntry(int range, int alpha) {}

    public int getRange(@Nullable final Block block) {
        if (block == null) {
            if (maximumRange == -1) {
                maximumRange = storeRange(null);
            }

            return maximumRange;
        }

        return cache.computeIfAbsent(block, this::storeData).range();
    }

    public int getAlpha(@Nullable final Block block) {
        if (block == null) {
            if (minimumAlpha == 255) {
                minimumAlpha = storeAlpha(null);
            }

            return minimumAlpha;
        }

        return cache.computeIfAbsent(block, this::storeData).alpha();
    }

    public boolean testValidBlocks(Block block, BlockPos blockPos, Vec3 blockVec, Vec3 blockStraightVec, boolean above, Vec3 entityLookVec, float playerXRot, Player player) {
        if (isEmpty()) {
            return false;
        }

        return all().stream().anyMatch(phasing -> phasing.getAngleCheck(block, blockPos, blockVec, blockStraightVec, above, entityLookVec, playerXRot, player));
    }

    private PhasingData.CacheEntry storeData(final Block block) {
        return new PhasingData.CacheEntry(storeRange(block), storeAlpha(block));
    }

    /** If the passed state is 'null' it will return the range as well */
    private int storeRange(@Nullable final Block block) {
        int currentRange = Phasing.NO_RANGE;

        for (Phasing.Instance instance : all()) {
            int range = instance.getRange(block);

            if (range > currentRange) {
                currentRange = range;
            }
        }

        return currentRange;
    }

    /** If the passed state is 'null' it will return the alpha as well */
    private int storeAlpha(@Nullable final Block block) {
        int currentAlpha = Phasing.NO_ALPHA;

        for (Phasing.Instance instance : all()) {
            int alpha = instance.getAlpha(block);

            if (alpha < currentAlpha) {
                currentAlpha = alpha;
            }
        }

        return currentAlpha;
    }

    @Override
    public void invalidateCache() {
        cache.clear();
        maximumRange = -1;
        minimumAlpha = 255;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        event.getEntity().getExistingData(DSDataAttachments.PHASING).ifPresent(storage -> {
            storage.tick(event.getEntity());

            if (storage.isEmpty()) {
                event.getEntity().removeData(DSDataAttachments.PHASING);
            }
        });
    }

    @SubscribeEvent
    public static void shareData(final PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();

        target.getExistingData(DSDataAttachments.PHASING).ifPresent(data -> {
            PacketDistributor.sendToPlayersTrackingEntity(target, new SyncData(target.getId(), NeoForgeRegistries.ATTACHMENT_TYPES.getKey(data.type()), data.serializeNBT(target.registryAccess())));
        });
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final Phasing.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected Phasing.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return Phasing.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.PHASING.get();
    }
}