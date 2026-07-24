package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class DamageModifications extends Storage<DamageModification.Instance> {
    public boolean isFireImmune() {
        if (storage == null) {
            return false;
        }

        return storage.values().stream().anyMatch(DamageModification.Instance::isFireImmune);
    }

    public float calculate(final Holder<DamageType> damageType, float damageAmount) {
        if (storage == null) {
            return damageAmount;
        }

        float newDamageAmount = damageAmount;

        for (final DamageModification.Instance modification : storage.values()) {
            newDamageAmount = modification.calculate(damageType, newDamageAmount);

            if (newDamageAmount == 0) {
                break;
            }
        }

        return newDamageAmount;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).ifPresent(data -> {
            data.tick(event.getEntity());

            if (data.isEmpty()) {
                event.getEntity().removeData(DSDataAttachments.DAMAGE_MODIFICATIONS);
            }
        });
    }

    @SubscribeEvent
    public static void checkImmunity(final EntityInvulnerabilityCheckEvent event) {
        if (event.isInvulnerable()) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).ifPresent(modifications -> {
            // Supply a dummy damage amount (since it doesn't matter for this check but allows re-using the same method)
            if (modifications.calculate(event.getSource().typeHolder(), 1) == 0) {
                event.setInvulnerable(true);
            }
        });
    }

    @SubscribeEvent
    public static void reduceDamage(final LivingIncomingDamageEvent event) {
        event.getEntity().getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).ifPresent(modifications -> {
            event.setAmount(modifications.calculate(event.getSource().typeHolder(), event.getAmount()));
        });
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final DamageModification.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected DamageModification.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return DamageModification.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.DAMAGE_MODIFICATIONS.get();
    }
}
