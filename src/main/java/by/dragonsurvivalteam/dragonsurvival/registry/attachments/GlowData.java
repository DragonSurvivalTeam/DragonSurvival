package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import by.dragonsurvivalteam.dragonsurvival.common.compat.attachments.AttachmentType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import by.dragonsurvivalteam.dragonsurvival.common.compat.event.EntityTickEvent;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
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
        all().forEach(glow -> colors.add(DSColors.withAlpha(glow.getColor(), 1)));
        return Functions.lerpColor(colors);
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        AttachmentManager.getExistingData(event.getEntity(), DSDataAttachments.GLOW).ifPresent(storage -> {
            storage.tick(event.getEntity());

            if (storage.isEmpty()) {
                AttachmentManager.removeData(event.getEntity(), DSDataAttachments.GLOW);
            }
        });
    }

    @SubscribeEvent
    public static void shareData(final PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();

        AttachmentManager.getExistingData(target, DSDataAttachments.GLOW).ifPresent(data -> {
            PacketDistributor.sendToPlayersTrackingEntity(target, new SyncData(target.getId(), DSDataAttachments.ATTACHMENT_TYPES.get().getKey(data.type()), data.serializeNBT(target.level().registryAccess())));
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
