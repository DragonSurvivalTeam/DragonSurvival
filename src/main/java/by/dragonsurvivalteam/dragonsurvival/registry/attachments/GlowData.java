package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
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
    @ConfigRange(min = 0, max = 1)
    @Translation(key = "glow_color_speed", type = Translation.Type.CONFIGURATION, comments = "Determines how fast colors switch when multiple glow colors are present")
    @ConfigOption(side = ConfigSide.CLIENT, category = "effects", key = "glow_color_speed")
    public static float SPEED = 0.025f;

    public static final int NO_COLOR = -1;

    private float timer;
    private int index;

    public int getColor() {
        if (isEmpty()) {
            return NO_COLOR;
        }

        List<Integer> colors = new ArrayList<>();
        all().forEach(glow -> colors.add(glow.getColor()));

        if (colors.size() == 1) {
            return colors.getFirst();
        }

        // Safety measure, since elements can get lost since the last timer update
        index = index % colors.size();

        int currentColor = colors.get(index);
        int nextColor = colors.get((index + 1) % colors.size());
        return FastColor.ARGB32.lerp(timer, DSColors.withAlpha(currentColor, 255), DSColors.withAlpha(nextColor, 255));
    }

    public void tickTimer() {
        timer += SPEED;

        if (timer >= 1) {
            timer = 0;
            index = (index + 1) % size();
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