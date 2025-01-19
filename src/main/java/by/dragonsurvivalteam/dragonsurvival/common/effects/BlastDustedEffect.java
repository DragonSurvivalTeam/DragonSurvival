package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("unused")
@EventBusSubscriber
public class BlastDustedEffect extends ModifiableMobEffect {

    @ConfigRange(max = 50)
    @Translation(key = "blast_dust_explosion_radius", type = Translation.Type.CONFIGURATION, comments = "The explosion radius of Blast Dust")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "blast_dust"}, key = "blast_dust_explosion_radius")
    public static Float blastDustExplosionRadius = 0.6f;

    public BlastDustedEffect(MobEffectCategory type, int color, boolean incurable) { super(type, color, incurable); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if ((DragonStateProvider.getOptional(livingEntity).isPresent() && DragonStateProvider.getOptional(livingEntity).get().species().is(DSDragonSpeciesTags.CAVE_DRAGONS)) || livingEntity.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()) || livingEntity.isInWaterRainOrBubble()) {
            return false;
        }
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public void onMobHurt(@NotNull LivingEntity livingEntity, int amplifier, DamageSource damageSource, float amount) {
        if (damageSource.is(DamageTypeTags.IS_FIRE)) {
            if (!livingEntity.level().isClientSide()) {
                explode(livingEntity, amplifier);
                livingEntity.removeEffect(DSEffects.BLAST_DUSTED);
            }
        }
    }

    public static void explode(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide()) {
            Entity effectApplier = null;
            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                //noinspection DataFlowIssue
                effectApplier = ((AdditionalEffectData) livingEntity.getEffect(DSEffects.BLAST_DUSTED)).dragonSurvival$getApplier(serverLevel);
            }
            livingEntity.level().explode(livingEntity, new DamageSource(DSDamageTypes.get(livingEntity.level(), DSDamageTypes.BLAST_DUST), effectApplier), null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), blastDustExplosionRadius * (amplifier + 1), true, Level.ExplosionInteraction.MOB);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(final MobEffectEvent.Remove removeEvent) {
        LivingEntity livingEntity = removeEvent.getEntity();
        if (!(DragonStateProvider.isDragon(livingEntity))) {
            Optional<DragonStateHandler> optional = DragonStateProvider.getOptional(livingEntity);
            if (optional.isEmpty() || !optional.get().species().is(DSDragonSpeciesTags.CAVE_DRAGONS)) {
                removeEvent.getEntity().level().playLocalSound(livingEntity.position().x(), livingEntity.position().y(), livingEntity.position().z(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 1f, 1f, false);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(final MobEffectEvent.Expired expiredEvent) {
        if (expiredEvent.getEffectInstance() != null && expiredEvent.getEffectInstance().is(DSEffects.BLAST_DUSTED)) {
            if (!expiredEvent.getEntity().level().isClientSide) {
                explode(expiredEvent.getEntity(), expiredEvent.getEffectInstance().getAmplifier());
            }
        }
    }
}
