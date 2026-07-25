package net.minecraftforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.damagesource.DamageContainer;
import net.minecraftforge.common.damagesource.IReductionFunction;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Forge 1.20 compatibility facade for NeoForge's 1.21 incoming damage event.
 */
@Cancelable
public class LivingIncomingDamageEvent extends LivingEvent {
    private final DamageContainer container;

    public LivingIncomingDamageEvent(final LivingEntity entity, final DamageContainer container) {
        super(entity);
        this.container = container;
    }

    public DamageContainer getContainer() {
        return container;
    }

    public DamageSource getSource() {
        return container.getSource();
    }

    public float getAmount() {
        return container.getNewDamage();
    }

    public float getOriginalAmount() {
        return container.getOriginalDamage();
    }

    public void setAmount(final float amount) {
        container.setNewDamage(amount);
    }

    public void addReductionModifier(final DamageContainer.Reduction type, final IReductionFunction reductionFunction) {
        container.addModifier(type, reductionFunction);
    }

    public void setInvulnerabilityTicks(final int ticks) {
        container.setPostAttackInvulnerabilityTicks(ticks);
    }
}
