package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.network.modifiers.SyncModifierWithDuration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class ModifiersWithDuration extends Storage<ModifierWithDuration.Instance> {
    public void syncModifiersToPlayer(final ServerPlayer player) {
        if (storage == null) {
            return;
        }

        for (ModifierWithDuration.Instance modifier : storage.values()) {
            PacketDistributor.sendToPlayer(player, new SyncModifierWithDuration(player.getId(), modifier, false));
        }
    }

    @SubscribeEvent
    public static void tickModifiers(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).ifPresent(data -> {
                data.tick();

                if (data.isEmpty()) {
                    livingEntity.removeData(DSDataAttachments.MODIFIERS_WITH_DURATION);
                }
            });
        }
    }

    @SubscribeEvent
    public static void removeModifiers(final PlayerEvent.Clone event) {
        // Since the modifiers are applied as permanent they need to be removed on death
        // Otherwise we lose the ability to track them and cannot remove them at all
        if (event.isWasDeath()) {
            event.getOriginal().getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).ifPresent(data -> data.all().forEach(entry -> entry.onRemovalFromStorage(event.getEntity())));
        }
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final ModifierWithDuration.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected ModifierWithDuration.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return ModifierWithDuration.Instance.load(provider, tag);
    }
}
