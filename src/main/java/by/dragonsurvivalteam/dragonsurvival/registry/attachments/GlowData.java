package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.FastColor;
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
    private static final float DELTA = 0.01f;

    private float timer;
    private boolean reversed;

    public int getColor() {
        if (isEmpty()) {
            return NO_COLOR;
        }

        List<Integer> colors = new ArrayList<>();
        all().forEach(glow -> colors.add(glow.getColor()));

        if (colors.size() == 1) {
            return colors.getFirst();
        }

        if (timer == 0) {
            return colors.getFirst();
        } else if (timer == 1) {
            return colors.getLast();
        }

        float indexedTimer = (colors.size() - 1) * timer;
        int colorIndex = (int) Math.floor(indexedTimer);

        int color = DSColors.withAlpha(colors.get(colorIndex), 255);
        int nextColor = DSColors.withAlpha(colors.get(colorIndex + 1), 255);

        return FastColor.ARGB32.lerp(indexedTimer - colorIndex, color, nextColor);
    }

    public void tickTimer() {
        if (reversed) {
            timer = Math.clamp(timer - DELTA, 0, 1);
        } else {
            timer = Math.clamp(timer + DELTA, 0, 1);
        }

        if (timer == 0) {
            reversed = false;
        } else if (timer == 1) {
            reversed = true;
        }
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        event.getEntity().getExistingData(DSDataAttachments.GLOW).ifPresent(storage -> {
            storage.tick(event.getEntity());

            if (storage.isEmpty()) {
                event.getEntity().removeData(DSDataAttachments.GLOW);
            } else {
                storage.tickTimer();
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