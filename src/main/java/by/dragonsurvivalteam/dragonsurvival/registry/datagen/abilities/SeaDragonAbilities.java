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
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AreaCloudEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.BreathParticlesEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.DamageEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.DamageModificationEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.FlightEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.HarvestBonusEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ModifierEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.PotionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ProjectileEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SpinEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SwimEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DragonBreathTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ConditionUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.DragonSizeUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceLevelUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;
import java.util.Optional;

public class SeaDragonAbilities {
    // --- Active --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Breathe out a stream of sparks and electricity. Targets become §c«Electrified»§r and deal electric damage to everything nearby.\n",
            "■ §fCharges creepers§r§7, and may summon §fthunderbolts§r§7 during a storm.\n",
            "■ §fRange§r§8 depends on the age of the dragon."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Storm Breath")
    public static final ResourceKey<DragonAbility> STORM_BREATH = DragonAbilities.key("storm_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Shoot out a condensed ball of electrical energy. Deals damage and §celectrifies§r nearby enemies as it travels.\n",
            "■ During a thunderstorm, lightning may strike the ball."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Ball Lightning")
    public static final ResourceKey<DragonAbility> BALL_LIGHTNING = DragonAbilities.key("ball_lightning");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Give a buff to yourself and your allies that multiplies the amount of §2experience§r gained from monsters.")
    @Translation(type = Translation.Type.ABILITY, comments = "Soul Revelation")
    public static final ResourceKey<DragonAbility> SOUL_REVELATION = DragonAbilities.key("soul_revelation");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Give yourself §2Sea Vision§r for a short time. Makes water more clear and less dark.")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Vision")
    public static final ResourceKey<DragonAbility> SEA_EYES = DragonAbilities.key("sea_eyes");

    // --- Passive --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Upgrading this ability increases your §2maximum mana pool§r§7 and allows to restore mana by standing on §fwet blocks§r§7.\n",
            "■ §8The more levels you have, the more mana you get automatically."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Magic")
    public static final ResourceKey<DragonAbility> SEA_MAGIC = DragonAbilities.key("sea_magic");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Standing on wet, sandy and icy surfaces increases §2running speed§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Athletics")
    public static final ResourceKey<DragonAbility> SEA_ATHLETICS = DragonAbilities.key("sea_athletics");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Increases your §2capacity for hydration§r§7 while outside of water. Will help you to survive while venturing onto land, or even in the §fNether§r§7.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Hydration")
    public static final ResourceKey<DragonAbility> HYDRATION = DragonAbilities.key("hydration");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Gives a chance to make your attack §cignore enemy armor§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Spectral Impact")
    public static final ResourceKey<DragonAbility> SPECTRAL_IMPACT = DragonAbilities.key("spectral_impact");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons can dig blocks that require shovels §2without tools§r§7. This ability gets stronger as you grow.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Claws and Teeth")
    public static final ResourceKey<DragonAbility> SEA_CLAWS_AND_TEETH = DragonAbilities.key("sea_claws_and_teeth");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Dragons use §2levitation§r to fly, but are rarely born with that ability.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Wings")
    public static final ResourceKey<DragonAbility> SEA_WINGS = DragonAbilities.key("sea_wings");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ You can §2spin§r§7 through the §fair§r§7 and in §fwater§r§7, boosting your speed. Head to §2the End§r §7to learn this skill.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Spin")
    public static final ResourceKey<DragonAbility> SEA_SPIN = DragonAbilities.key("sea_spin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons have an innate §2immunity to lightning§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Electric Immunity")
    public static final ResourceKey<DragonAbility> ELECTRIC_IMMUNITY = DragonAbilities.key("electric_immunity");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons do not need to hold their §2breath underwater§r§7. In addition, they can §2swim much faster§r§7 than other dragons.")
    @Translation(type = Translation.Type.ABILITY, comments = "Amphibious")
    public static final ResourceKey<DragonAbility> AMPHIBIOUS = DragonAbilities.key("amphibious");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Dexterity in water allows sea dragons to §2mine resources underwater§r§7 without penalty.")
    @Translation(type = Translation.Type.ABILITY, comments = "Diver")
    public static final ResourceKey<DragonAbility> DIVER = DragonAbilities.key("diver");

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
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.LIGHTNING_BREATH), LevelBasedValue.perLevel(1)),
                                        PotionEffect.single(LevelBasedValue.constant(0), LevelBasedValue.constant(Functions.secondsToTicks(30)), LevelBasedValue.constant(0.5f), false, DSEffects.CHARGED)
                                ),
                                TargetingMode.NON_ALLIES
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(
                                List.of(new AreaCloudEffect(
                                        PotionData.of(LevelBasedValue.constant(0), LevelBasedValue.constant(Functions.secondsToTicks(30)), false, DSEffects.CHARGED),
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
                                TargetingMode.ALL
                        )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/storm_breath_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/storm_breath_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/storm_breath_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/storm_breath_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/storm_breath_4"), 4)
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
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 20f, 45f, 50f), LevelBasedValue.perLevel(15)))),
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
                        TargetingMode.ALL
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(
                        List.of(
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ball_lightning_0"), 0),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ball_lightning_1"), 1),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ball_lightning_2"), 2),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ball_lightning_3"), 3),
                                new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ball_lightning_4"), 4)
                        )
                )
        ));

        context.register(SOUL_REVELATION, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(3))),
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
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(7f, 15f, 35f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        List.of(
                                ModifierEffect.single(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("revealing_the_soul")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(60))).customIcon(DragonSurvival.res("textures/ability_effect/revealing_the_soul.png")).build(),
                                        List.of(new Modifier(DSAttributes.EXPERIENCE, Either.left(LevelBasedValue.perLevel(0.5f)), AttributeModifier.Operation.ADD_VALUE, Optional.empty()))
                                )),
                                new ParticleEffect(
                                        new SpawnParticles(ParticleTypes.SOUL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                        LevelBasedValue.constant(20)
                                )),
                        TargetingMode.ALLIES_AND_SELF
                ), LevelBasedValue.constant(5)), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_3"), 3)
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
                        Activation.Sound.end(SoundEvents.UI_TOAST_IN),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation("cast_self_buff", AnimationLayer.BASE, 2, true, false))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("self_buff", AnimationLayer.BASE, 0, true, false))
                        ))
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 45f, 60f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground (except when in water)
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false))
                        .and(Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.WATER))).invert()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        PotionEffect.only(LevelBasedValue.constant(0), LevelBasedValue.perLevel(Functions.secondsToTicks(30)), false, DSEffects.WATER_VISION),
                        TargetingMode.ALLIES_AND_SELF
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_eyes_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_eyes_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_eyes_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_eyes_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_eyes_4"), 4)
                ))
        ));
    }

    private static void registerPassiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(SEA_MAGIC, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(10, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("sea_magic")).infinite().removeAutomatically().hidden().build(),
                                        List.of(new Modifier(DSAttributes.MANA, Either.left(LevelBasedValue.perLevel(1)), AttributeModifier.Operation.ADD_VALUE, Optional.empty()))
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        )), LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                // Enable when on (or within) said block tag or when in water
                                Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_WET))
                                        .or(Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_WET)))
                                        .or(Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.WATER)))).build(),
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("good_mana_condition")).infinite().removeAutomatically().hidden().build(),
                                        List.of(new Modifier(DSAttributes.MANA_REGENERATION, Either.left(LevelBasedValue.perLevel(1)), AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Optional.empty()))
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        )), LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_5"), 5),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_6"), 6),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_7"), 7),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_8"), 8),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_9"), 9),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_magic_10"), 10)
                ))
        ));

        context.register(SEA_ATHLETICS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(5, LevelBasedValue.perLevel(25))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        // Enable when on said block tag
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.SPEEDS_UP_SEA_DRAGON)).build(),
                        PotionEffect.only(LevelBasedValue.perLevel(0.2f), LevelBasedValue.perLevel(Functions.secondsToTicks(1)), false, MobEffects.MOVEMENT_SPEED),
                        TargetingMode.ALLIES_AND_SELF
                )), LevelBasedValue.constant(Functions.secondsToTicks(1)))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_athletics_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_athletics_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_athletics_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_athletics_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_athletics_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_athletics_5"), 5)
                ))
        ));

        context.register(HYDRATION, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(7, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("dry_resilience")).infinite().removeAutomatically().hidden().build(),
                                List.of(new Modifier(DSAttributes.PENALTY_RESISTANCE_TIME, Either.left(LevelBasedValue.perLevel(Functions.secondsToTicks(60))), AttributeModifier.Operation.ADD_VALUE, Optional.empty()))
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_5"), 5),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_6"), 6),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/water_7"), 7)
                ))
        ));

        context.register(SPECTRAL_IMPACT, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(3, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("spectral_impact")).infinite().removeAutomatically().hidden().build(),
                                List.of(new Modifier(DSAttributes.ARMOR_IGNORE_CHANCE, Either.left(LevelBasedValue.perLevel(0.15f)), AttributeModifier.Operation.ADD_VALUE, Optional.empty()))
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/spectral_impact_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/spectral_impact_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/spectral_impact_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/spectral_impact_3"), 3)
                ))
        ));

        context.register(SEA_CLAWS_AND_TEETH, new DragonAbility(
                Activation.passive(),
                Optional.of(new DragonSizeUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 40f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        HarvestBonusEffect.only(new HarvestBonus(
                                DurationInstanceBase.create(DragonSurvival.res("sea_claws_and_teeth")).infinite().removeAutomatically().customIcon(DragonSurvival.res("textures/ability_effect/sea_claw.png")).build(),
                                Optional.of(context.lookup(Registries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_SHOVEL)),
                                Optional.of(new LevelBasedTier(List.of(
                                        new LevelBasedTier.Entry(Tiers.WOOD, 1),
                                        new LevelBasedTier.Entry(Tiers.STONE, 2)
                                ))),
                                LevelBasedValue.perLevel(1, 0.5f),
                                LevelBasedValue.perLevel(0.25f)
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_claws_and_teeth_4"), 4)
                ))
        ));

        context.register(SEA_WINGS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.flightWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon or when trapped / wings are broken
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isMarked(true)),
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.TRAPPED)),
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.BROKEN_WINGS))
                ).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new FlightEffect(1, DragonSurvival.res("textures/ability_effect/sea_dragon_wings.png"))),
                        TargetingMode.ALLIES_AND_SELF
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_wings_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_wings_1"), 1)
                ))
        ));

        context.register(SEA_SPIN, new DragonAbility(
                Activation.passive(),
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.spinWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new SpinEffect(1, Optional.of(HolderSet.direct(NeoForgeMod.WATER_TYPE)))),
                        TargetingMode.ALLIES_AND_SELF
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_spin_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_spin_1"), 1)
                ))
        ));

        context.register(ELECTRIC_IMMUNITY, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        DamageModificationEffect.only(new DamageModification(
                                DurationInstanceBase.create(DragonSurvival.res("electric_immunity")).infinite().removeAutomatically().customIcon(DragonSurvival.res("textures/ability_effect/electric_immunity.png")).build(),
                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypeTags.IS_ELECTRIC),
                                LevelBasedValue.constant(0)
                        )),
                        TargetingMode.ALL
                )), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_dragon_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/sea_dragon_1"), 1)
                ))
        ));

        context.register(AMPHIBIOUS, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new SwimEffect(LevelBasedValue.constant(SwimData.UNLIMITED_OXYGEN), NeoForgeMod.WATER_TYPE)),
                                TargetingMode.ALL
                        )), LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("amphibious")).infinite().removeAutomatically().hidden().build(),
                                        List.of(new Modifier(NeoForgeMod.SWIM_SPEED, Either.left(LevelBasedValue.constant(1f)), AttributeModifier.Operation.ADD_VALUE, Optional.empty()))
                                )),
                                TargetingMode.ALL
                        )), LevelBasedValue.constant(1)),
                        // FIXME :: Put in a separate ability? Put in a different ability from this one? Just needed to move it since we deleted built in modifiers for dragon species
                        //  unlock the first level of the resistance abilities? (i.e. set it to exp 0)
                        //  would also need auto leveling logic? only for 0 experience though
                        //  meaning you could only de-level up to the first non-0 experience level of the ability
                        //  (since you can manually disable it that should be fine)
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("amphibious_penalty_resistance")).infinite().removeAutomatically().hidden().build(),
                                        List.of(new Modifier(DSAttributes.PENALTY_RESISTANCE_TIME, Either.left(LevelBasedValue.constant(Functions.secondsToTicks(60))), AttributeModifier.Operation.ADD_VALUE, Optional.empty()))
                                )),
                                TargetingMode.ALL
                        )), LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/amphibian_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/amphibian_1"), 1)
                ))
        ));

        context.register(DIVER, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("diver")).infinite().removeAutomatically().hidden().build(),
                                        List.of(new Modifier(Attributes.SUBMERGED_MINING_SPEED, Either.left(LevelBasedValue.constant(0.8f)), AttributeModifier.Operation.ADD_VALUE, Optional.empty()))
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        )), LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/diver_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/diver_1"), 1)
                ))
        ));
    }
}
