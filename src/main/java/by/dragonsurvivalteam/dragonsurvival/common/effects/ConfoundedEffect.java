package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@SuppressWarnings("unused")
@EventBusSubscriber
public class ConfoundedEffect extends ModifiableMobEffect {
    public ConfoundedEffect(MobEffectCategory type, int color, boolean incurable) {
        super(type, color, incurable);
    }

    @SubscribeEvent
    public static void reflectDamage(LivingDamageEvent.Post damageEvent) {
        LivingEntity victim = damageEvent.getEntity();
        Entity damageSource = damageEvent.getSource().getEntity();
        if (damageSource instanceof LivingEntity livingSource) {
            if (livingSource.hasEffect(DSEffects.CONFOUNDED) && !damageEvent.getSource().is(DSDamageTypes.MIRROR_CURSE)) {
                Entity effectApplier = null;
                if (victim.level() instanceof ServerLevel serverLevel) {
                    //noinspection DataFlowIssue
                    effectApplier = ((AdditionalEffectData) livingSource.getEffect(DSEffects.CONFOUNDED)).dragonSurvival$getApplier(serverLevel);
                }

                livingSource.hurt(new DamageSource(DSDamageTypes.get(victim.level(), DSDamageTypes.MIRROR_CURSE), effectApplier), damageEvent.getNewDamage());
            }
        }
    }
}
