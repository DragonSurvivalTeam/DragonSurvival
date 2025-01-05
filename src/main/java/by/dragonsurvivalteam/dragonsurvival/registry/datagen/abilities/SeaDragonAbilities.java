package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.*;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.*;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AreaCloudEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.*;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DragonBreathTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ItemUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.LevelUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.SizeUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;
import java.util.Optional;

public class SeaDragonAbilities {
    // --- Active --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Breathe out a stream of sparks and electricity. Targets become §c«Electrified»§r and deal electric damage to everything nearby.\n",
            "■ Charges creepers, and may summon thunderbolts during a storm.\n",
            "■ Range depends on the age of the dragon."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Storm Breath")
    public static final ResourceKey<DragonAbility> STORM_BREATH = DragonAbilities.key("storm_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Shoot out a condensed ball of electrical energy. Deals damage and §celectrifies§r nearby enemies as it travels.\n",
            "■ During a thunderstorm, lightning may strike the ball."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Ball Lightning")
    public static final ResourceKey<DragonAbility> BALL_LIGHTNING = DragonAbilities.key("ball_lightning");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Give a buff to yourself and your allies that multiplies the amount of §2experience§r gained from monsters.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Soul Revelation")
    public static final ResourceKey<DragonAbility> SOUL_REVELATION = DragonAbilities.key("soul_revelation");

    @Translation(type = Translation.Type.ABILITY_EFFECT, comments = "Revealing the Soul")
    public static final ResourceLocation SOUL_REVELATION_MODIFIER = DragonSurvival.res("revealing_the_soul");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Give yourself §2Sea Vision§r for a short time.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Vision")
    public static final ResourceKey<DragonAbility> SEA_EYES = DragonAbilities.key("sea_eyes");

    // --- Passive --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Upgrading this ability increases your maximum mana pool. Additionally, mana regeneration will be sped up while standing on wet blocks.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Magic")
    public static final ResourceKey<DragonAbility> SEA_MAGIC = DragonAbilities.key("sea_magic");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Standing on wet surfaces will increase your movement speed.")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Athletics")
    public static final ResourceKey<DragonAbility> SEA_ATHLETICS = DragonAbilities.key("sea_athletics");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Increases your capacity for hydration while outside of water. Will help you to survive while venturing onto land, or even in the Nether.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Hydration")
    public static final ResourceKey<DragonAbility> HYDRATION = DragonAbilities.key("hydration");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Gives a chance to make your attack ignore enemy armor.")
    @Translation(type = Translation.Type.ABILITY, comments = "Spectral Impact")
    public static final ResourceKey<DragonAbility> SPECTRAL_IMPACT = DragonAbilities.key("spectral_impact");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons can dig blocks that require shovels without tools. This ability gets stronger as you grow.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Claws and Teeth")
    public static final ResourceKey<DragonAbility> SEA_CLAWS_AND_TEETH = DragonAbilities.key("sea_claws_and_teeth");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Dragons use §2levitation§r to fly, but are rarely born with that ability. Only one dragon in this world can share their power of flight with you.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Wings")
    public static final ResourceKey<DragonAbility> SEA_WINGS = DragonAbilities.key("sea_wings");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ You can spin through the air and in water, boosting your speed.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Spin")
    public static final ResourceKey<DragonAbility> SEA_SPIN = DragonAbilities.key("sea_spin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons have an innate immunity to lightning.")
    @Translation(type = Translation.Type.ABILITY, comments = "Electric Immunity")
    public static final ResourceKey<DragonAbility> ELECTRIC_IMMUNITY = DragonAbilities.key("electric_immunity");

    @Translation(type = Translation.Type.ABILITY_EFFECT, comments = "Electric Immunity")
    public static final ResourceLocation ELECTRIC_IMMUNITY_EFFECT = DragonSurvival.res("electric_immunity");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons are amphibious, and do not need to hold their breath underwater. In addition, they can swim much faster than other dragons.")
    @Translation(type = Translation.Type.ABILITY, comments = "Amphibious")
    public static final ResourceKey<DragonAbility> AMPHIBIOUS = DragonAbilities.key("amphibious");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
        registerPassiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(STORM_BREATH, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_CHANNELED,
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        true,
                        Activation.Sound.of(DSSounds.STORM_BREATH_START.get(), null, DSSounds.STORM_BREATH_LOOP.get(), DSSounds.STORM_BREATH_END.get()),
                        Optional.of(new Activation.Animations(
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("breath", AnimationLayer.BREATH, 5, false, false)),
                                Optional.empty()
                        ))
                ),
                Optional.of(new LevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.LIGHTNING_BREATH), LevelBasedValue.perLevel(1)),
                                        PotionEffect.single(LevelBasedValue.constant(0), LevelBasedValue.constant(Functions.secondsToTicks(30)), LevelBasedValue.constant(0.5f), DSEffects.CHARGED).getFirst()
                                ),
                                AbilityTargeting.EntityTargetingMode.TARGET_ENEMIES
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(
                                List.of(new AreaCloudEffect(
                                        PotionData.of(LevelBasedValue.constant(0), LevelBasedValue.constant(Functions.secondsToTicks(30)), DSEffects.CHARGED),
                                        LevelBasedValue.constant(Functions.secondsToTicks(2)),
                                        0.3,
                                        new LargeLightningParticleOption(37, false)
                                ))
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.04f,
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

        context.register(BALL_LIGHTNING, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(20))),
                        true,
                        Optional.empty(),
                        Optional.empty()
                ),
                Optional.of(new LevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 20f, 45f, 50f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        Condition.thisEntity(EntityCondition.isLiving()).build(),
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

        context.register(SOUL_REVELATION, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        false,
                        Optional.of(new Activation.Sound(
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(SoundEvents.UI_TOAST_IN)
                        )),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation(SimpleAbilityAnimation.CAST_MASS_BUFF, AnimationLayer.BASE, 2, true, true))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation(SimpleAbilityAnimation.MASS_BUFF, AnimationLayer.BASE, 0, true, true))
                        ))
                ),
                Optional.of(new LevelUpgrade(3, LevelBasedValue.lookup(List.of(0f, 15f, 35f), LevelBasedValue.perLevel(15)))),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(false)).build()).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        List.of(new ModifierEffect(List.of(new ModifierWithDuration(
                                SOUL_REVELATION_MODIFIER,
                                DragonSurvival.res("textures/modifiers/revealing_the_soul.png"),
                                List.of(new Modifier(DSAttributes.EXPERIENCE, LevelBasedValue.perLevel(0.5f), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                LevelBasedValue.perLevel(Functions.secondsToTicks(60)),
                                false
                        ))),
                        new ParticleEffect(
                                new SpawnParticles(ParticleTypes.SOUL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                LevelBasedValue.constant(20)
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), LevelBasedValue.constant(5)), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/revealing_the_soul_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/revealing_the_soul_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/revealing_the_soul_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/revealing_the_soul_3"), 3)
                ))
        ));

        context.register(SEA_EYES, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        false,
                        Activation.Sound.of(null, null, null, SoundEvents.UI_TOAST_IN),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation("cast_self_buff", AnimationLayer.BASE, 2, true, false))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("self_buff", AnimationLayer.BASE, 0, true, false))
                        ))
                ),
                Optional.of(new LevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 45f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(false)).build()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        PotionEffect.single(LevelBasedValue.constant(0), LevelBasedValue.perLevel(Functions.secondsToTicks(30)), DSEffects.WATER_VISION),
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

    private static void registerPassiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(SEA_MAGIC, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(10, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.single(new ModifierWithDuration(
                                        DragonSurvival.res("sea_magic"),
                                        ModifierWithDuration.DEFAULT_MODIFIER_ICON,
                                        List.of(new Modifier(DSAttributes.MANA, LevelBasedValue.perLevel(1), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                        true
                                )),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                        ), true), LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_WET)).or(Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_WET))).build(),
                                ModifierEffect.single(new ModifierWithDuration(
                                        DragonAbilities.GOOD_MANA_CONDITION,
                                        ModifierWithDuration.DEFAULT_MODIFIER_ICON,
                                        List.of(new Modifier(DSAttributes.MANA_REGENERATION, LevelBasedValue.perLevel(1), AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Optional.empty())),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                        true
                                )),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                        ), true), LevelBasedValue.constant(1))
                ),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_5"), 5),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_6"), 6),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_7"), 7),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_8"), 8),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_9"), 9),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_magic_10"), 10)
                ))
        ));

        context.register(SEA_ATHLETICS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(5, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.SPEEDS_UP_SEA_DRAGON)).build(),
                        PotionEffect.single(LevelBasedValue.perLevel(1), LevelBasedValue.perLevel(Functions.secondsToTicks(5)), MobEffects.MOVEMENT_SPEED),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), false), LevelBasedValue.constant(Functions.secondsToTicks(1)))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_athletics_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_athletics_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_athletics_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_athletics_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_athletics_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_athletics_5"), 5)
                ))
        ));

        context.register(HYDRATION, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(7, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.single(new ModifierWithDuration(
                                DragonSurvival.res("dry_resilience"),
                                ModifierWithDuration.DEFAULT_MODIFIER_ICON,
                                List.of(new Modifier(DSAttributes.PENALTY_RESISTANCE_TIME, LevelBasedValue.perLevel(Functions.secondsToTicks(60)), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                true
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_5"), 5),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_6"), 6),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/water_7"), 7)
                ))
        ));

        context.register(SPECTRAL_IMPACT, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(3, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.single(new ModifierWithDuration(
                                DragonSurvival.res("spectral_impact"),
                                ModifierWithDuration.DEFAULT_MODIFIER_ICON,
                                List.of(new Modifier(DSAttributes.ARMOR_IGNORE_CHANCE, LevelBasedValue.perLevel(0.15f), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                true
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/spectral_impact_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/spectral_impact_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/spectral_impact_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/spectral_impact_3"), 3)
                ))
        ));

        context.register(SEA_CLAWS_AND_TEETH, new DragonAbility(
                Activation.passive(),
                Optional.of(new SizeUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 40f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        HarvestBonusEffect.single(new HarvestBonus(
                                DragonSurvival.res("sea_claws_and_teeth"),
                                Optional.of(context.lookup(Registries.BLOCK).getOrThrow(DSBlockTags.SEA_DRAGON_HARVESTABLE)),
                                LevelBasedValue.perLevel(1, 0.5f),
                                LevelBasedValue.perLevel(0.5f),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_4"), 4)
                ))
        ));

        context.register(SEA_WINGS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ItemUpgrade(List.of(HolderSet.direct(DSItems.WING_GRANT_ITEM)), HolderSet.empty())),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().markedByEnderDragon(true).build()).build()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new FlightEffect(1)),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_wings_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_wings_1"), 1)
                ))
        ));

        context.register(SEA_SPIN, new DragonAbility(
                Activation.passive(),
                Optional.of(new ItemUpgrade(List.of(HolderSet.direct(DSItems.SPIN_GRANT_ITEM)), HolderSet.empty())),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().markedByEnderDragon(true).build()).build()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new SpinEffect(1, Optional.of(NeoForgeMod.WATER_TYPE))),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_wings_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_wings_1"), 1)
                ))
        ));

        context.register(ELECTRIC_IMMUNITY, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        DamageModificationEffect.single(new DamageModification(
                                ELECTRIC_IMMUNITY_EFFECT,
                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypeTags.IS_ELECTRIC),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                false
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALL
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_dragon_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/sea_dragon_1"), 1)
                ))
        ));

        context.register(AMPHIBIOUS, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new SwimEffect(LevelBasedValue.constant(SwimData.UNLIMITED_OXYGEN), NeoForgeMod.WATER_TYPE)),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALL), true), LevelBasedValue.constant(1)
                        ),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.single(new ModifierWithDuration(
                                        DragonSurvival.res("amphibious"),
                                        ModifierWithDuration.DEFAULT_MODIFIER_ICON,
                                        List.of(new Modifier(NeoForgeMod.SWIM_SPEED, LevelBasedValue.constant(1f), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                        true
                                )),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALL), true), LevelBasedValue.constant(1)
                        )
                ),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/amphibian_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/sea/amphibian_1"), 1)
                ))
        ));
    }
}
