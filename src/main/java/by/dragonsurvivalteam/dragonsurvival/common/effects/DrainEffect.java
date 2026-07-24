package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.EffectHandler;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallPoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class DrainEffect extends ModifiableMobEffect {
    @ConfigRange(min = 0)
    @Translation(key = "drain_effect_damage", type = Translation.Type.CONFIGURATION, comments = "Determines the damage dealt by the drain effect")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "drain"}, key = "drain_effect_damage")
    public static Float damage = 1f;

    public DrainEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color, incurable);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(@NotNull final LivingEntity entity, int amplifier) {
        if (!DragonStateProvider.isDragon(entity)) {
            ParticleOptions particle = new SmallPoisonParticleOption(37F, false);

            for (int i = 0; i < 4; i++) {
                EffectHandler.renderEffectParticle(entity, particle);
            }
        }

        Entity effectApplier = null;

        if (entity.level() instanceof ServerLevel serverLevel) {
            //noinspection DataFlowIssue -> effect cannot be null here
            effectApplier = ((AdditionalEffectData) entity.getEffect(DSEffects.DRAIN)).dragonSurvival$getApplier(serverLevel);
        }

        entity.hurt(new DamageSource(DSDamageTypes.get(entity.level(), DSDamageTypes.DRAIN), effectApplier), damage);

        return super.applyEffectTick(entity, amplifier);
    }
}
