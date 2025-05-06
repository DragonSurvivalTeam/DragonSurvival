package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            if (!player.level().isClientSide()) {
                if (DragonStateProvider.getData(player).species().is(BuiltInDragonSpecies.FOREST_DRAGON)) { return false; }

                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100));
                if (amplifier > 1) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100));
                } else if (amplifier > 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100));
                }
            }
        } else if (livingEntity instanceof Mob mob){
            if (!mob.level().isClientSide()) {
                if (mob.getType().is(DSEntityTypeTags.CONFOUNDED_TARGET_BLACKLIST)) {
                    return true; // Keep the effect but don't change targets
                }
                List<LivingEntity> list1 = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(5.0));
                // Remove all forest dragons from potential targets
                // Also remove self as target
                list1 = list1.stream().filter(e -> {
                    if (e instanceof Player p) { return (DragonStateProvider.getData(p).species().is(BuiltInDragonSpecies.FOREST_DRAGON)); }
                    else if (e == mob) return false;
                    else if (mob.getType().is(DSEntityTypeTags.CONFOUNDED_TARGET_BLACKLIST)) return false;
                    return true;
                }).toList();
                if (list1.size() <= 0) {
                    // No valid targets to swap to.
                    return true;
                }
                int targetIndex = mob.getRandom().nextInt(list1.size());
                mob.setTarget(list1.get(targetIndex));
            }
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % Functions.secondsToTicks(5) == 0;
    }
}
