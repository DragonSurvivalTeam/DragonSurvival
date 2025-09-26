package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.HarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedTier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationKey;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.BlockCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.ItemCondition;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargePoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeSunParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallPoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallSunParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Animations;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.ChanneledActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Notification;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.SimpleActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Sound;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BlockConversionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BonemealEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.BreathParticlesEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.DamageEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.DamageModificationEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.FlightEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.HarvestBonusEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ItemConversionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ModifierEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.PotionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ProjectileEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SpinEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DragonBreathTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ConditionUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.DragonGrowthUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceLevelUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;
import java.util.Optional;

public class ForestDragonAbilities {
    // --- Active --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Breathe out toxic gas that causes §c«Drain»§r, which withers away enemies over time.\n",
            "■ §fRange§r§7 depends on age of the dragon.\n",
            "■ §8Cannot be used while affected by «Stress».§r"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Breath")
    public static final ResourceKey<DragonAbility> FOREST_BREATH = DragonAbilities.key("forest_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Breathe out solar energy stored within you, helping §2plants grow faster§r§7. Turns §fdirt§r§7 into other blocks with a small chance. It also §fpoisons§r§7 potatoes.\n",
            "■ §fRange§r§7 depends on age of the dragon.\n",
            "■ §8Cannot be used while affected by «Stress».§r"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Sun Breath")
    public static final ResourceKey<DragonAbility> SUN_BREATH = DragonAbilities.key("sun_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Shoot out sharp §cdarts§r, which fly a large distance to pierce your target.\n",
            "■ §8Less effective underwater."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Spike")
    public static final ResourceKey<DragonAbility> SPIKE = DragonAbilities.key("spike");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Provides §2Haste§r to you and all nearby creatures. Increasing block §fharvesting speed§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Inspiration")
    public static final ResourceKey<DragonAbility> INSPIRATION = DragonAbilities.key("inspiration");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Cover yourself and allies in a thick carpet of herbs. This grants §ffast§r§7 movement and §2Invisibility§r§7 whilst in the grass.\n",
            "■ Your first attack while invisible is a critical hit with a §c%-based damage§r§7 bonus.\n",
            "■ §8Effect does not stack. Will be removed early if you take damage, or attack a target.§r",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Hunters")
    public static final ResourceKey<DragonAbility> HUNTER = DragonAbilities.key("hunter");

    // --- Passive --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Upgrading this ability increases your §2maximum mana pool§r§7 and allows to restore mana by standing on §fgrass§r§7 or under direct §fsunlight§r§7.\n",
            "■ §8The more levels you have, the more mana you get automatically."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Magic")
    public static final ResourceKey<DragonAbility> FOREST_MAGIC = DragonAbilities.key("forest_magic");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Standing on §fgrassy§r§7 or §fwooden§r§7 surfaces will increase your §2movement speed§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Athletics")
    public static final ResourceKey<DragonAbility> FOREST_ATHLETICS = DragonAbilities.key("forest_athletics");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Allows you to §2stay longer§r§7 in §fdark§r§7 areas.")
    @Translation(type = Translation.Type.ABILITY, comments = "Light the Dark")
    public static final ResourceKey<DragonAbility> LIGHT_IN_DARKNESS = DragonAbilities.key("light_in_darkness");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Forest dragons are very §flight§r§7. Your landing becomes much §2softer§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Cliffhanger")
    public static final ResourceKey<DragonAbility> CLIFFHANGER = DragonAbilities.key("cliffhanger");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Forest dragons can §2chop§7 trees faster §fwithout tools§r§7. This ability gets stronger as you grow.")
    @Translation(type = Translation.Type.ABILITY, comments = "Claws and Teeth")
    public static final ResourceKey<DragonAbility> FOREST_CLAWS_AND_TEETH = DragonAbilities.key("forest_claws_and_teeth");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Dragons use §flevitation§r§7 to §2fly§r§7, but are rarely born with that ability.")
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Wings")
    public static final ResourceKey<DragonAbility> FOREST_WINGS = DragonAbilities.key("forest_wings");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ You can §2spin§r§7 through the §fair§r§7 and in §fwater§r§7, boosting your speed. Head to §fthe End§r §7to learn this skill.")
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Spin")
    public static final ResourceKey<DragonAbility> FOREST_SPIN = DragonAbilities.key("forest_spin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ The skin of forest dragons is covered with §ffoliage§r§7 that give you §2immunity to thorn bushes and cacti§r§7."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Immunity")
    public static final ResourceKey<DragonAbility> FOREST_IMMUNITY = DragonAbilities.key("forest_immunity");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
        registerPassiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(FOREST_BREATH, new DragonAbility(
                new ChanneledActivation(
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.empty(),
                        Notification.DEFAULT,
                        true,
                        Sound.create().start(DSSounds.FOREST_BREATH_START.get()).looping(DSSounds.FOREST_BREATH_LOOP.get()).end(DSSounds.FOREST_BREATH_END.get()).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build())
                                .looping(SimpleAbilityAnimation.create(AnimationKey.BREATH, AnimationLayer.BREATH).transitionLength(5).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))),
                // Disable when affected by the 'STRESS' effect
                Optional.of(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.STRESS)).build()),
                List.of(
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.FOREST_BREATH), LevelBasedValue.perLevel(2)),
                                        new PotionEffect(PotionData.create(DSEffects.DRAIN).duration(10).probability(0.3f).build())
                                ),
                                TargetingMode.NON_ALLIES
                        ), LevelBasedValue.constant(1)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(10)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.04f,
                                        0.02f,
                                        new SmallPoisonParticleOption(37, true),
                                        new LargePoisonParticleOption(37, false)
                                )),
                                TargetingMode.ALL
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/poisonous_breath_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/poisonous_breath_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/poisonous_breath_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/poisonous_breath_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/poisonous_breath_4"), 4)
                ))
        ));

        context.register(SUN_BREATH, new DragonAbility(
                new ChanneledActivation(
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.04f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.empty(),
                        Notification.DEFAULT,
                        true,
                        Sound.create().start(DSSounds.FOREST_BREATH_START.get()).looping(DSSounds.FOREST_BREATH_LOOP.get()).end(DSSounds.FOREST_BREATH_END.get()).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build())
                                .looping(SimpleAbilityAnimation.create(AnimationKey.BREATH, AnimationLayer.BREATH).transitionLength(5).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(2, LevelBasedValue.lookup(List.of(0f, 24f), LevelBasedValue.perLevel(15)))),
                // Disable when affected by the 'STRESS' effect
                Optional.of(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.STRESS)).build()),
                List.of(
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                List.of(new ItemConversionEffect(
                                        List.of(new ItemConversionEffect.ItemConversionData(ItemCondition.is(Items.POTATO), WeightedRandomList.create(ItemConversionEffect.ItemTo.of(Items.POISONOUS_POTATO)))),
                                        LevelBasedValue.constant(0.5f)
                                )),
                                TargetingMode.ITEMS
                        ), LevelBasedValue.constant(1)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(
                                List.of(
                                        new BonemealEffect(LevelBasedValue.constant(2), LevelBasedValue.perLevel(0.5f)),
                                        new BlockConversionEffect(List.of(new BlockConversionEffect.BlockConversionData(
                                                BlockCondition.blocks(Blocks.DIRT, Blocks.COARSE_DIRT),
                                                SimpleWeightedRandomList.create(
                                                        new BlockConversionEffect.BlockTo(Blocks.GRASS_BLOCK.defaultBlockState(), 25),
                                                        new BlockConversionEffect.BlockTo(Blocks.PODZOL.defaultBlockState(), 5),
                                                        new BlockConversionEffect.BlockTo(Blocks.MYCELIUM.defaultBlockState(), 1),
                                                        new BlockConversionEffect.BlockTo(Blocks.COARSE_DIRT.defaultBlockState(), 3)
                                                ))
                                        ), LevelBasedValue.constant(0.2f))
                                )
                        ), LevelBasedValue.constant(1)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(10)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.02f,
                                        0.02f,
                                        new SmallSunParticleOption(37, true),
                                        new LargeSunParticleOption(37, false)
                                )),
                                TargetingMode.ALL
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/sun_breath_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/sun_breath_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/sun_breath_2"), 2)
                ))
        ));

        context.register(SPIKE, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(0.1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(3))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().end(SoundEvents.ARROW_SHOOT).optional(),
                        Animations.create().startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build()).optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 20f, 30f, 40f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        Condition.thisEntity(EntityCondition.isLiving()).build(),
                        List.of(new ProjectileEffect(
                                context.lookup(ProjectileData.REGISTRY).getOrThrow(Projectiles.SPIKE),
                                TargetDirection.lookingAt(),
                                LevelBasedValue.perLevel(1),
                                LevelBasedValue.constant(1.5f),
                                LevelBasedValue.constant(1)
                        )),
                        TargetingMode.ALL
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(
                        List.of(
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/spike_0"), 0),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/spike_1"), 1),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/spike_2"), 2),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/spike_3"), 3),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/spike_4"), 4)
                        )
                )
        ));

        context.register(INSPIRATION, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(2)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(4))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        Notification.DEFAULT,
                        false,
                        Sound.create().end(SoundEvents.UI_TOAST_IN).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MASS_BUFF, AnimationLayer.BASE).transitionLength(2).locksNeck().locksTail().build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MASS_BUFF, AnimationLayer.BASE).locksNeck().locksTail().build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(5f, 15f, 35f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        List.of(
                                new PotionEffect(PotionData.create(MobEffects.DIG_SPEED).amplifierPer(1).durationPer(200).build()),
                                new ParticleEffect(
                                        new SpawnParticles(ParticleTypes.END_ROD, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                        LevelBasedValue.constant(20)
                                )
                        ), TargetingMode.NON_ENEMIES
                ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/inspiration_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/inspiration_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/inspiration_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/inspiration_3"), 3)
                ))
        ));

        context.register(HUNTER, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(3)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(3))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        Notification.DEFAULT,
                        false,
                        Sound.create().end(SoundEvents.UI_TOAST_IN).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MASS_BUFF, AnimationLayer.BASE).transitionLength(2).locksNeck().locksTail().build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MASS_BUFF, AnimationLayer.BASE).locksNeck().locksTail().build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 35f, 55f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        List.of(
                                new PotionEffect(PotionData.create(DSEffects.HUNTER).amplifierPer(1).durationPer(30).build()),
                                new ParticleEffect(
                                        new SpawnParticles(ParticleTypes.DRAGON_BREATH, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.1f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.1f)), ConstantFloat.of(0.1f)),
                                        LevelBasedValue.constant(20)
                                )
                        ), TargetingMode.NON_ENEMIES
                ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/hunter_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/hunter_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/hunter_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/hunter_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/hunter_4"), 4)
                ))
        ));
    }

    private static void registerPassiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(FOREST_MAGIC, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(10, LevelBasedValue.perLevel(36))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("forest_magic")).infinite().removeAutomatically().hidden().build(),
                                        List.of(Modifier.per(DSAttributes.MANA, 1, AttributeModifier.Operation.ADD_VALUE))
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                // Enable when on (or within) said block tag or when under sunlight with a strength of at least 10
                                Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_GRASSY))
                                        .or(Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_GRASSY)))
                                        .or(Condition.thisEntity(EntityCondition.isInSunlight(10))).build(),
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("good_mana_condition")).infinite().removeAutomatically().hidden().build(),
                                        List.of(Modifier.per(DSAttributes.MANA_REGENERATION, 0.01f, AttributeModifier.Operation.ADD_VALUE))
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_5"), 5),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_6"), 6),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_7"), 7),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_8"), 8),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_9"), 9),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_magic_10"), 10)
                ))
        ));

        context.register(FOREST_ATHLETICS, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(5, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        // Enable when on said block tag
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.SPEEDS_UP_FOREST_DRAGON)).build(),
                        PotionEffect.only(PotionData.create(MobEffects.MOVEMENT_SPEED).amplifierPer(0.2f).durationPer(1).build()),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(Functions.secondsToTicks(1)))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_athletics_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_athletics_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_athletics_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_athletics_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_athletics_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_athletics_5"), 5)
                ))
        ));

        context.register(LIGHT_IN_DARKNESS, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(8, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("light_in_darkness")).infinite().removeAutomatically().hidden().build(),
                                List.of(Modifier.per(DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(20), AttributeModifier.Operation.ADD_VALUE))
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_5"), 5),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_6"), 6),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_7"), 7),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/light_in_darkness_8"), 8)
                ))
        ));

        context.register(CLIFFHANGER, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(6, LevelBasedValue.perLevel(16))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("cliffhanger")).infinite().removeAutomatically().hidden().build(),
                                List.of(Modifier.perWithBase(Attributes.SAFE_FALL_DISTANCE, 5, 2, AttributeModifier.Operation.ADD_VALUE))
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/cliffhanger_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/cliffhanger_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/cliffhanger_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/cliffhanger_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/cliffhanger_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/cliffhanger_5"), 5),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/cliffhanger_6"), 6)
                ))
        ));

        context.register(FOREST_CLAWS_AND_TEETH, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new DragonGrowthUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 40f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        HarvestBonusEffect.only(new HarvestBonus(
                                DurationInstanceBase.create(DragonSurvival.res("forest_claws_and_teeth")).infinite().removeAutomatically().customIcon(DragonSurvival.res("textures/ability_effect/forest_claw.png")).build(),
                                Optional.of(context.lookup(Registries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_AXE)),
                                Optional.of(new LevelBasedTier(List.of(
                                        new LevelBasedTier.Entry(Tiers.WOOD, 1),
                                        new LevelBasedTier.Entry(Tiers.STONE, 2)
                                ))),
                                LevelBasedValue.perLevel(1, 0.5f),
                                LevelBasedValue.perLevel(0.25f)
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_4"), 4)
                ))
        ));

        context.register(FOREST_WINGS, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.flightWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new FlightEffect(1, DragonSurvival.res("textures/ability_effect/forest_dragon_wings.png"))),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_wings_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_wings_1"), 1)
                ))
        ));

        context.register(FOREST_SPIN, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.spinWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new SpinEffect(1, Optional.of(HolderSet.direct(NeoForgeMod.WATER_TYPE)))),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_spin_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_spin_1"), 1)
                ))
        ));

        context.register(FOREST_IMMUNITY, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        DamageModificationEffect.only(new DamageModification(
                                DurationInstanceBase.create(DragonSurvival.res("forest_immunity")).infinite().removeAutomatically().customIcon(DragonSurvival.res("textures/ability_effect/drain_immunity.png")).build(),
                                HolderSet.direct(
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.SWEET_BERRY_BUSH),
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.CACTUS),
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.DRAIN),
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.FOREST_BREATH)
                                ),
                                LevelBasedValue.constant(0)
                        )),
                        TargetingMode.ALL
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_dragon_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/forest/forest_dragon_1"), 1)
                ))
        ));
    }
}
