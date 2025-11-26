package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class PhasingData extends Storage<Phasing.Instance> {

    public boolean testValidBlocks(Level t, BlockPos u, Vec3 v, Vec3 w) {
        if (isEmpty()) {
            return false;
        }

        return all().stream().anyMatch(phasing -> phasing.testValidBlocks(t, u, v, w));
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