package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.EffectHandler;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChargedEffect extends ModifiableMobEffect {
    public static final int INFINITE_CHAINS = -1;

    @ConfigRange(min = INFINITE_CHAINS)
    @Translation(key = "charged_effect_max_chain", type = Translation.Type.CONFIGURATION, comments = "Determines the max. amount of times the charged effect can chain. Set to -1 for infinite chaining")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "charged"}, key = "charged_effect_max_chain")
    public static Integer maxChain = 5;

    @ConfigRange(min = 0)
    @Translation(key = "charged_effect_max_chain_targets", type = Translation.Type.CONFIGURATION, comments = "Amount of entities the charged effect can chain to at once")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "charged"}, key = "charged_effect_max_chain_targets")
    public static Integer maxChainTargets = 2;

    @ConfigRange(min = 0, max = 256)
    @Translation(key = "charged_effect_spread_radius", type = Translation.Type.CONFIGURATION, comments = "Determines the radius of the charged effect spread")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "charged"}, key = "charged_effect_spread_radius")
    public static Float spreadRadius = 3f;

    @ConfigRange(min = 0)
    @Translation(key = "charged_effect_damage", type = Translation.Type.CONFIGURATION, comments = "Determines the damage dealt by the charged effect")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "charged"}, key = "charged_effect_damage")
    public static Float damage = 1f;

    public ChargedEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color, incurable);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void onEffectStarted(@NotNull LivingEntity livingEntity, int amplifier) {
        // Make creepers become charged
        if (livingEntity instanceof Creeper creeper && !creeper.isPowered()) {
            creeper.getEntityData().set(Creeper.DATA_IS_POWERED, true);
        }
    }

    @Override
    public boolean applyEffectTick(final LivingEntity entity, int amplifier) {
        entity.hurt(new DamageSource(DSDamageTypes.get(entity.level(), DSDamageTypes.ELECTRIC)), damage * (amplifier + 1));

        if (!DragonStateProvider.isDragon(entity)) {
            ParticleOptions particle = new SmallLightningParticleOption(37F, false);

            for (int i = 0; i < 4; i++) {
                EffectHandler.renderEffectParticle(entity, particle);
            }
        }

        chargedEffectChain(entity, damage * amplifier + 1);
        return super.applyEffectTick(entity, amplifier);
    }

    public static void drawParticleLine(LivingEntity source, LivingEntity target) {
        if (source.level().isClientSide()) {
            return;
        }

        Vec3 start = source.position().add(0, source.getEyeHeight() / 2, 0);
        Vec3 end = target.position().add(0, target.getEyeHeight() / 2, 0);
        Vec3 trailMidpoint = end.subtract(start).scale(0.5).add(start);
        PacketDistributor.sendToPlayersNear(
                (ServerLevel) source.level(),
                null,
                trailMidpoint.x,
                trailMidpoint.y,
                trailMidpoint.z,
                64,
                new SyncParticleTrail(start.toVector3f(), end.toVector3f(), new LargeLightningParticleOption(37F, false)));
    }

    public static void chargedEffectChain(final LivingEntity source, float damage) {
        List<LivingEntity> secondaryTargets = source.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat(), source, source.getBoundingBox().inflate(spreadRadius));
        secondaryTargets.sort((c1, c2) -> Boolean.compare(c1.hasEffect(DSEffects.CHARGED), c2.hasEffect(DSEffects.CHARGED))); // Prioritize non-charged entities

        if (secondaryTargets.size() > maxChainTargets) {
            secondaryTargets = secondaryTargets.subList(0, maxChainTargets);
        }

        for (LivingEntity target : secondaryTargets) {
            Entity effectApplier = null;

            if (source.level() instanceof ServerLevel serverLevel) {
                //noinspection DataFlowIssue -> effect cannot be null here
                effectApplier = ((AdditionalEffectData) source.getEffect(DSEffects.CHARGED)).dragonSurvival$getApplier(serverLevel);
            }

            target.hurt(new DamageSource(DSDamageTypes.get(target.level(), DSDamageTypes.ELECTRIC), effectApplier), damage);
            drawParticleLine(source, target);

            if (target.level().isClientSide()) {
                return;
            }

            if (target != source && !target.getType().is(DSEntityTypeTags.CHARGED_SPREAD_BLACKLIST)) {
                EntityStateHandler sourceData = source.getData(DSDataAttachments.ENTITY_HANDLER);
                EntityStateHandler targetData = target.getData(DSDataAttachments.ENTITY_HANDLER);

                targetData.chainCount = sourceData.chainCount + 1;

                if ((targetData.chainCount < maxChain || maxChain == INFINITE_CHAINS) && Functions.chance(target.getRandom(), 40)) {
                    target.addEffect(new MobEffectInstance(DSEffects.CHARGED, Functions.secondsToTicks(10), 0, false, false), effectApplier);
                }
            }
        }
    }
}
