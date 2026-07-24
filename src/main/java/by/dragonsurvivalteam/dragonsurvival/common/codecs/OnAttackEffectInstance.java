package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import net.minecraft.core.HolderSet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record OnAttackEffectInstance(HolderSet<MobEffect> effects, int amplifier, int duration, float probability) {
    public void apply(final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            effects.forEach(effect -> {
                MobEffectInstance currentInstance = livingEntity.getEffect(effect);
                if (currentInstance != null && (currentInstance.getAmplifier() >= amplifier && currentInstance.getDuration() >= duration)) {
                    // Don't do anything if the current effect is at least equally strong and has at least the same duration
                    // For all other cases this new effect will either override the current instance or be added as hidden effect
                    // (Whose duration etc. will be applied once the stronger (and shorter) effect runs out)
                    return;
                }

                if (livingEntity.getRandom().nextDouble() < probability) {
                    livingEntity.addEffect(new MobEffectInstance(effect, duration, amplifier));
                }
            });
        }
    }

}
