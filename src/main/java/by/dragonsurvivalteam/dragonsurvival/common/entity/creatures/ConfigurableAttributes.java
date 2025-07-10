package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface ConfigurableAttributes {
    private LivingEntity self() {
        return (LivingEntity) this;
    }

    default void setAttributes() {
        setBaseValue(Attributes.MAX_HEALTH, maxHealthConfig());
        setBaseValue(Attributes.ATTACK_DAMAGE, attackDamageConfig());
        setBaseValue(Attributes.ATTACK_KNOCKBACK, attackKnockback());
        setBaseValue(Attributes.MOVEMENT_SPEED, movementSpeedConfig());
        setBaseValue(Attributes.ARMOR, armorConfig());
        setBaseValue(Attributes.ARMOR_TOUGHNESS, armorToughnessConfig());
        setBaseValue(Attributes.KNOCKBACK_RESISTANCE, knockbackResistanceConfig());
    }

    default void setBaseValue(final Holder<Attribute> attribute, final double value) {
        if (Double.isNaN(value)) {
            return;
        }

        AttributeInstance instance = self().getAttribute(attribute);

        if (instance != null) {
            instance.setBaseValue(value);

            if (attribute == Attributes.MAX_HEALTH) {
                self().setHealth(self().getMaxHealth());
            }
        }
    }

    default double maxHealthConfig() {
        return Double.NaN;
    }

    default double attackDamageConfig() {
        return Double.NaN;
    }

    default double attackKnockback() {
        return Double.NaN;
    }

    default double movementSpeedConfig() {
        return Double.NaN;
    }

    default double armorConfig() {
        return Double.NaN;
    }

    default double armorToughnessConfig() {
        return Double.NaN;
    }

    default double knockbackResistanceConfig() {
        return Double.NaN;
    }
}
