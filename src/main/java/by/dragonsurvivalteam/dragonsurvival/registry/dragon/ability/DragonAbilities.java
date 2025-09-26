package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.*;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationKey;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.*;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnDeath;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnSelfHit;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnTargetKilled;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.RunFunctionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.HealEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SmeltItemEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.TeleportEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceLevelUpgrade;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final LevelBasedValue INFINITE_DURATION = LevelBasedValue.constant(DurationInstance.INFINITE_DURATION);

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res("test_smelt")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.empty(), new OnTargetKilled(Optional.empty())),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                new SmeltItemEffect(Optional.empty(), Optional.empty(), true)
                        ), TargetingMode.ITEMS), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res("test_heal")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))), OnDeath.INSTANCE),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                                new HealEffect(LevelBasedValue.constant(0.3f))
                        ), TargetingMode.ALLIES_AND_SELF)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res("test_function")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))), new OnSelfHit(Optional.empty())),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                                new RunFunctionEffect(DragonSurvival.res("test"))
                        ), TargetingMode.ALLIES_AND_SELF)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res("test_teleport")), new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(15)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().end(SoundEvents.PLAYER_TELEPORT).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                            List.of(
                                new TeleportEffect(
                                        TargetDirection.lookingAt(),
                                        LevelBasedValue.perLevel(100, 50)
                                )), TargetingMode.ALL_EXCEPT_SELF
                ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)),
                new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                    List.of(
                        new TeleportEffect(
                                TargetDirection.lookingAt(),
                                LevelBasedValue.perLevel(100, 50)
                        ),
                        new ParticleEffect(
                                new SpawnParticles(ParticleTypes.PORTAL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                LevelBasedValue.constant(20)
                        )), TargetingMode.ALL)
                    ), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)
                )),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res("test_teleport_directional")), new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(15)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().end(SoundEvents.PLAYER_TELEPORT).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                                List.of(
                                        new TeleportEffect(
                                                TargetDirection.of(Direction.UP),
                                                LevelBasedValue.perLevel(10, 5)
                                        ),
                                        new ParticleEffect(
                                                new SpawnParticles(ParticleTypes.PORTAL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                                LevelBasedValue.constant(20)
                                        )), TargetingMode.ALL_EXCEPT_SELF
                        ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));
    }

    public static ResourceKey<DragonAbility> key(final ResourceLocation location) {
        return ResourceKey.create(DragonAbility.REGISTRY, location);
    }

    public static ResourceKey<DragonAbility> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
