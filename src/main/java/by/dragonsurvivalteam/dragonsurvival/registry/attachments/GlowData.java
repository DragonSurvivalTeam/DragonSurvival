package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class GlowData extends Storage<Glow.Instance> {
    public static final int NO_COLOR = -1;

    public int getColor() {
        if (isEmpty()) {
            return NO_COLOR;
        }

        List<Integer> colors = new ArrayList<>();
        all().forEach(glow -> colors.add(glow.getColor()));
        return Functions.lerpColor(colors);
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        event.getEntity().getExistingData(DSDataAttachments.GLOW).ifPresent(storage -> {
            storage.tick(event.getEntity());

            if (storage.isEmpty()) {
                event.getEntity().removeData(DSDataAttachments.GLOW);
            }
        });
    }

    @SubscribeEvent
    public static void shareData(final PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();

        target.getExistingData(DSDataAttachments.GLOW).ifPresent(data -> {
            PacketDistributor.sendToPlayersTrackingEntity(target, new SyncData(target.getId(), NeoForgeRegistries.ATTACHMENT_TYPES.getKey(data.type()), data.serializeNBT(target.registryAccess())));
        });
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final Glow.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected Glow.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return Glow.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.GLOW.get();
    }
}