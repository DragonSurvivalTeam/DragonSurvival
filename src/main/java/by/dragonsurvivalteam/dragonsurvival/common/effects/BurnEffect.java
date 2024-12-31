package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.EffectHandler;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForgeMod;

public class BurnEffect extends ModifiableMobEffect {
    public BurnEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color, incurable);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(final LivingEntity entity, int amplifier) {
        if (entity.fireImmune() || entity.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()) || entity.isInWaterRainOrBubble()) {
            return false;
        }

        EntityStateHandler data = entity.getData(DSDataAttachments.ENTITY_HANDLER);

        if (data.lastPos == null) {
            data.lastPos = entity.position();
        }

        if (!DragonStateProvider.isDragon(entity)) {
            ParticleOptions particle = new SmallFireParticleOption(37F, false);

            for (int i = 0; i < 4; i++) {
                EffectHandler.renderEffectParticle(entity, particle);
            }
        }

        if (data.lastPos != null) {
            double distance = entity.distanceToSqr(data.lastPos);
            float damage = (amplifier + 1) * Mth.clamp((float) distance, 0, 10);

            if (damage > 0) {
                if (!entity.isOnFire()) {
                    // Short enough fire duration to not cause fire damage but still drop cooked items
                    entity.setRemainingFireTicks(1);
                }

                Entity effectApplier = null;

                if (entity.level() instanceof ServerLevel serverLevel) {
                    //noinspection DataFlowIssue -> effect cannot be null here
                    effectApplier = ((AdditionalEffectData) entity.getEffect(DSEffects.BURN)).dragonSurvival$getApplier(serverLevel);
                }

                entity.hurt(new DamageSource(DSDamageTypes.get(entity.level(), DSDamageTypes.BURN), effectApplier), damage);
            }
        }

        data.lastPos = entity.position();
        return super.applyEffectTick(entity, amplifier);
    }
}
