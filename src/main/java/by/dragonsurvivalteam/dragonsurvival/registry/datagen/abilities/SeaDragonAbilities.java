package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.Upgrade;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.ValueBasedUpgrade;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AreaCloudEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.*;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DragonBreathTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class SeaDragonAbilities {
    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Shoot out a condensed ball of electrical energy. Deals damage and §celectrifies§r nearby enemies as it travels.\n",
            "■ During a thunderstorm, lightning may strike the ball."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Ball Lightning")
    public static final ResourceKey<DragonAbility> BALL_LIGHTNING = DragonAbilities.key("ball_lightning");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Breathe out a stream of sparks and electricity. Targets become §c«Electrified»§r and deal electric damage to everything nearby.\n",
            "■ Charges creepers, and may summon thunderbolts during a storm.\n",
            "■ Range depends on the age of the dragon."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Storm Breath")
    public static final ResourceKey<DragonAbility> STORM_BREATH = DragonAbilities.key("storm_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Personal Buff: provides §2Sea Vision§r for a short time.\n"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Vision")
    public static final ResourceKey<DragonAbility> SEA_EYES = DragonAbilities.key("sea_eyes");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(BALL_LIGHTNING, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(20))),
                        Optional.empty(),
                        Optional.empty()
                ),
                Upgrade.value(ValueBasedUpgrade.Type.PASSIVE_LEVEL, 4, LevelBasedValue.lookup(List.of(0f, 20f, 45f, 50f), LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(Condition.living()),
                        List.of(new ProjectileEffect(
                                context.lookup(ProjectileData.REGISTRY).getOrThrow(Projectiles.BALL_LIGHTNING),
                                TargetDirection.lookingAt(),
                                LevelBasedValue.constant(1),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(1)
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALL
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(
                        List.of(
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/ball_lightning_0"), 0),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/ball_lightning_1"), 1),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/ball_lightning_2"), 2),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/ball_lightning_3"), 3),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/ball_lightning_4"), 4)
                        )
                )
        ));

        context.register(STORM_BREATH, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_CHANNELED,
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(new Activation.Sound(
                                Optional.of(DSSounds.STORM_BREATH_START.get()),
                                Optional.empty(),
                                Optional.of(DSSounds.STORM_BREATH_LOOP.get()),
                                Optional.of(DSSounds.STORM_BREATH_END.get())
                        )),
                        Optional.of(new Activation.Animations(
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("breath", AnimationLayer.BREATH, 5, false, false)),
                                Optional.empty()
                        ))
                ),
                Upgrade.value(ValueBasedUpgrade.Type.PASSIVE_LEVEL, 4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                List.of(Condition.living()),
                                List.of(
                                        new DamageEffect(
                                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.LIGHTNING_BREATH),
                                                LevelBasedValue.perLevel(1)
                                        ),
                                        new PotionEffect(new PotionData(
                                                HolderSet.direct(DSEffects.CHARGED),
                                                LevelBasedValue.constant(0),
                                                LevelBasedValue.constant(Functions.secondsToTicks(30)),
                                                LevelBasedValue.constant(0.5f)
                                        ))
                                ),
                                AbilityTargeting.EntityTargetingMode.TARGET_ENEMIES
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(
                                List.of(
                                        new AreaCloudEffect(
                                                new PotionData(
                                                        HolderSet.direct(DSEffects.CHARGED),
                                                        LevelBasedValue.constant(0),
                                                        LevelBasedValue.constant(Functions.secondsToTicks(30)),
                                                        LevelBasedValue.constant(1.0f)
                                                ),
                                                LevelBasedValue.constant(Functions.secondsToTicks(2)),
                                                0.3,
                                                new LargeLightningParticleOption(37, false)
                                        )
                                )
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.4f,
                                        0.02f,
                                        new SmallLightningParticleOption(37, true),
                                        new LargeLightningParticleOption(37, false)
                                )),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALL
                        ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/storm_breath_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/storm_breath_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/storm_breath_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/storm_breath_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/storm_breath_4"), 4)
                ))
        ));

        context.register(SEA_EYES, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        Optional.of(new Activation.Sound(
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(SoundEvents.UI_TOAST_IN)
                        )),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation("cast_self_buff", AnimationLayer.BASE, 2, true, false))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("self_buff", AnimationLayer.BASE, 0, true, false))
                        ))
                ),
                Upgrade.value(ValueBasedUpgrade.Type.PASSIVE_LEVEL, 4, LevelBasedValue.lookup(List.of(0f, 25f, 45f, 60f), LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new PotionEffect(new PotionData(
                                HolderSet.direct(DSEffects.WATER_VISION),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.perLevel(Functions.secondsToTicks(30)),
                                LevelBasedValue.constant(1)
                        ))),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_eyes_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_eyes_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_eyes_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_eyes_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_eyes_4"), 4)
                ))
        ));
    }
}
