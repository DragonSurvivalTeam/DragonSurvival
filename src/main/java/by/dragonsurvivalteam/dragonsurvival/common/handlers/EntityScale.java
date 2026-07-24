package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

/**
 * Compatibility access for the entity scale API introduced after Minecraft 1.20.1.
 */
public final class EntityScale {
    private EntityScale() {
    }

    public static float get(final LivingEntity entity) {
        if (entity instanceof Override override) {
            return override.dragonSurvival$getScale();
        }

        return getAttributeValue(entity);
    }

    public static float getAttributeValue(final LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(DSAttributes.SCALE.get());
        return instance == null ? 1.0F : (float) instance.getValue();
    }

    public interface Override {
        float dragonSurvival$getScale();
    }
}
