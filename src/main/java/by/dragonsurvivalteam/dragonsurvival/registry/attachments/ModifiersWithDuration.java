package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.EffectModification;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncModifierWithDuration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class ModifiersWithDuration extends Storage<ModifierWithDuration.Instance> {
    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        // Attribute modifiers are only relevant for living entities
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).ifPresent(data -> {
                data.tick(event.getEntity());

                if (data.isEmpty()) {
                    livingEntity.removeData(DSDataAttachments.MODIFIERS_WITH_DURATION);
                }
            });
        }
    }

    @Override
    public void sync(final ServerPlayer player) {
        if (storage == null) {
            return;
        }

        for (ModifierWithDuration.Instance modifier : storage.values()) {
            PacketDistributor.sendToPlayer(player, new SyncModifierWithDuration(player.getId(), modifier, false));
        }
    }

    @Override
    protected void save(@NotNull ValueOutput valueOutput, final ModifierWithDuration.Instance entry, final String key) {
        entry.save(valueOutput, key);
    }

    @Override
    protected ModifierWithDuration.Instance load(@NotNull ValueInput valueInput, final String key) {
        return ModifierWithDuration.Instance.load(valueInput, key);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.MODIFIERS_WITH_DURATION.get();
    }
}
