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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class BlastDustedEffect extends ModifiableMobEffect {
    @ConfigRange(max = 50)
    @Translation(key = "blast_dust_explosion_radius", type = Translation.Type.CONFIGURATION, comments = "The explosion radius of Blast Dust")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "blast_dust"}, key = "blast_dust_explosion_radius")
    public static Float blastDustExplosionRadius = 0.6f;

    public BlastDustedEffect(MobEffectCategory type, int color, boolean incurable) {
        super(type, color, incurable);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull final LivingEntity entity, final int amplifier) {
        if (entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon() && handler.species().is(DSDragonSpeciesTags.CAVE_DRAGONS)) {
                return false;
            }
        }

        if (entity.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()) || entity.isInWaterRainOrBubble()) {
            return false;
        }

        return super.applyEffectTick(entity, amplifier);
    }

    @Override
    public void onMobHurt(@NotNull final LivingEntity entity, final int amplifier, @NotNull final DamageSource damageSource, final float amount) {
        if (entity.level().isClientSide()) {
            return;
        }

        if (damageSource.is(DamageTypeTags.IS_FIRE)) {
            explode(entity, amplifier);
            entity.removeEffect(DSEffects.BLAST_DUSTED);
        }
    }

    public static void explode(final LivingEntity entity, final int amplifier) {
        Entity effectApplier = null;

        if (entity.level() instanceof ServerLevel serverLevel) {
            //noinspection DataFlowIssue -> effect should be present
            effectApplier = ((AdditionalEffectData) entity.getEffect(DSEffects.BLAST_DUSTED)).dragonSurvival$getApplier(serverLevel);
        }

        entity.level().explode(entity, new DamageSource(DSDamageTypes.get(entity.level(), DSDamageTypes.BLAST_DUST), effectApplier), null, entity.getX(), entity.getY(), entity.getZ(), blastDustExplosionRadius * (amplifier + 1), true, Level.ExplosionInteraction.MOB);
    }

    @SubscribeEvent
    public static void onEffectRemoved(final MobEffectEvent.Remove event) {
        if (event.getEffect() != DSEffects.BLAST_DUSTED) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon() && handler.species().is(DSDragonSpeciesTags.CAVE_DRAGONS)) {
                return;
            }
        }

        event.getEntity().level().playLocalSound(event.getEntity().position().x(), event.getEntity().position().y(), event.getEntity().position().z(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 1f, 1f, false);
    }

    @SubscribeEvent
    public static void onEffectExpired(final MobEffectEvent.Expired event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEffectInstance() != null && event.getEffectInstance().is(DSEffects.BLAST_DUSTED)) {
            explode(event.getEntity(), event.getEffectInstance().getAmplifier());
        }
    }
}
