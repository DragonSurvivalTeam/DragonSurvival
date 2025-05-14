package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.BlockVision;
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
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Animations;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.ChanneledActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Notification;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.SimpleActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Sound;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AreaCloudEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.BlockVisionEffect;
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
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.DragonGrowthUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceLevelUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
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
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;

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

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Give yourself §2Sea Vision§r§7 for a short time. Makes water more clear and less dark.")
    @Translation(type = Translation.Type.ABILITY, comments = "Sea Vision")
    public static final ResourceKey<DragonAbility> SEA_EYES = DragonAbilities.key("sea_eyes");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Gives you and the players around you the ability to §2illuminate ore§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Ore Glow")
    public static final ResourceKey<DragonAbility> ORE_GLOW = DragonAbilities.key("ore_glow");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Give a buff to yourself and your allies that multiplies the amount of §2experience§r gained from monsters.")
    @Translation(type = Translation.Type.ABILITY, comments = "Soul Revelation")
    public static final ResourceKey<DragonAbility> SOUL_REVELATION = DragonAbilities.key("soul_revelation");

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

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons can dig blocks that require shovels §2without tools§r§7. This ability gets stronger as you grow.")
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

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Sea dragons' natural dexterity in water allows them to §2mine resources underwater§r§7 without a speed penalty.")
    @Translation(type = Translation.Type.ABILITY, comments = "Diver")
    public static final ResourceKey<DragonAbility> DIVER = DragonAbilities.key("diver");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
        registerPassiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(STORM_BREATH, new DragonAbility(
                new ChanneledActivation(
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().start(DSSounds.STORM_BREATH_START.get()).looping(DSSounds.STORM_BREATH_LOOP.get()).end(DSSounds.STORM_BREATH_END.get()).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build())
                                .looping(SimpleAbilityAnimation.create(AnimationKey.BREATH, AnimationLayer.BREATH).transitionLength(5).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.LIGHTNING_BREATH), LevelBasedValue.perLevel(1)),
                                        new PotionEffect(PotionData.create(DSEffects.CHARGED).duration(30).probability(0.5f).build())
                                ),
                                TargetingMode.NON_ALLIES
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(
                                List.of(new AreaCloudEffect(
                                        PotionData.create(DSEffects.CHARGED).duration(30).build(),
                                        LevelBasedValue.constant(Functions.secondsToTicks(2)),
                                        LevelBasedValue.constant(0.3f),
                                        Optional.empty(),
                                        Optional.empty(),
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
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(20))),
                        Notification.DEFAULT,
                        true,
                        Optional.empty(),
                        Animations.create().startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build()).optional()
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

        context.register(SEA_EYES, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        Notification.DEFAULT,
                        false,
                        Sound.create().end(SoundEvents.UI_TOAST_IN).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).transitionLength(4).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 45f, 60f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground (except when in water)
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false))
                        .and(Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.WATER))).invert()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        PotionEffect.only(PotionData.create(DSEffects.WATER_VISION).durationPer(30).build()),
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

        //noinspection DataFlowIssue -> ignore
        context.register(ORE_GLOW, new DragonAbility(
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
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 20f, 45f, 50f), LevelBasedValue.perLevel(35)))),
                Optional.empty(),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("diamond_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_DIAMOND),
                                LevelBasedValue.constant(36),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(
                                        TextColor.fromLegacyFormat(ChatFormatting.GOLD),
                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE),
                                        TextColor.fromLegacyFormat(ChatFormatting.GREEN),
                                        TextColor.fromLegacyFormat(ChatFormatting.RED),
                                        TextColor.fromLegacyFormat(ChatFormatting.BLUE)
                                )
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("lapis_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_LAPIS),
                                LevelBasedValue.constant(16),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("gold_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_GOLD),
                                LevelBasedValue.constant(22),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(TextColor.fromLegacyFormat(ChatFormatting.GOLD))
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("redstone_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_REDSTONE),
                                LevelBasedValue.constant(16),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_RED))
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("coal_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_COAL),
                                LevelBasedValue.constant(16),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(TextColor.fromLegacyFormat(ChatFormatting.BLACK))
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("emerald_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_EMERALD),
                                LevelBasedValue.constant(26),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(TextColor.fromLegacyFormat(ChatFormatting.GREEN))
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("quartz_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_QUARTZ),
                                LevelBasedValue.constant(12),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(
                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_RED),
                                        TextColor.fromLegacyFormat(ChatFormatting.GRAY)
                                )
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("copper_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_COPPER),
                                LevelBasedValue.constant(16),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(
                                        TextColor.fromLegacyFormat(ChatFormatting.GOLD),
                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN)
                                )
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("netherite_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_NETHERITE_SCRAP),
                                LevelBasedValue.constant(36),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(
                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA),
                                        TextColor.fromLegacyFormat(ChatFormatting.BLACK),
                                        TextColor.fromLegacyFormat(ChatFormatting.AQUA),
                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY)
                                )
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("iron_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).hidden().build(),
                                context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_IRON),
                                LevelBasedValue.constant(22),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(
                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY),
                                        TextColor.fromLegacyFormat(ChatFormatting.GRAY),
                                        TextColor.fromLegacyFormat(ChatFormatting.WHITE)
                                )
                        )),
                        BlockVisionEffect.single(new BlockVision(
                                DurationInstanceBase.create(DragonSurvival.res("general_ore_vision")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(260))).customIcon(DragonSurvival.res("textures/ability_effect/general_ore_vision.png")).build(),
                                context.lookup(Registries.BLOCK).getOrThrow(DSBlockTags.GENERAL_ORES),
                                LevelBasedValue.constant(16),
                                BlockVision.DisplayType.PARTICLES,
                                List.of(TextColor.fromLegacyFormat(ChatFormatting.WHITE))
                        ))
                ), TargetingMode.NON_ENEMIES), LevelBasedValue.constant(5)), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ore_glow_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ore_glow_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ore_glow_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ore_glow_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/ore_glow_4"), 4)
                ))
        ));

        context.register(SOUL_REVELATION, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(6)),
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
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        List.of(
                                ModifierEffect.single(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("revealing_the_soul")).duration(LevelBasedValue.perLevel(Functions.secondsToTicks(60))).customIcon(DragonSurvival.res("textures/ability_effect/revealing_the_soul.png")).build(),
                                        List.of(Modifier.per(DSAttributes.EXPERIENCE, 0.5f, AttributeModifier.Operation.ADD_VALUE))
                                )),
                                new ParticleEffect(
                                        new SpawnParticles(ParticleTypes.SOUL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                        LevelBasedValue.constant(20)
                                )),
                        TargetingMode.NON_ENEMIES
                ), LevelBasedValue.constant(5)), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/sea/revealing_the_soul_3"), 3)
                ))
        ));
    }

    private static void registerPassiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(SEA_MAGIC, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(10, LevelBasedValue.perLevel(36))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("sea_magic")).infinite().removeAutomatically().hidden().build(),
                                        List.of(Modifier.per(DSAttributes.MANA, 1, AttributeModifier.Operation.ADD_VALUE))
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
                                        List.of(Modifier.per(DSAttributes.MANA_REGENERATION, 0.02f, AttributeModifier.Operation.ADD_VALUE))
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
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(5, LevelBasedValue.perLevel(25))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        // Enable when on said block tag
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.SPEEDS_UP_SEA_DRAGON)).build(),
                        PotionEffect.only(PotionData.create(MobEffects.MOVEMENT_SPEED).amplifierPer(0.2f).durationPer(1).build()),
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
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(7, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("dry_resilience")).infinite().removeAutomatically().hidden().build(),
                                List.of(Modifier.per(DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(60), AttributeModifier.Operation.ADD_VALUE))
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
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(3, LevelBasedValue.perLevel(64))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("spectral_impact")).infinite().removeAutomatically().hidden().build(),
                                List.of(Modifier.per(DSAttributes.ARMOR_IGNORE_CHANCE, 0.15f, AttributeModifier.Operation.ADD_VALUE))
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
                PassiveActivation.DEFAULT,
                Optional.of(new DragonGrowthUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 40f, 60f), LevelBasedValue.perLevel(15)))),
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
                PassiveActivation.DEFAULT,
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.flightWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
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
                PassiveActivation.DEFAULT,
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
                PassiveActivation.DEFAULT,
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
                PassiveActivation.DEFAULT,
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
                                        List.of(Modifier.constant(NeoForgeMod.SWIM_SPEED, 1, AttributeModifier.Operation.ADD_VALUE))
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
                                        List.of(Modifier.constant(DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(60), AttributeModifier.Operation.ADD_VALUE))
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
                PassiveActivation.DEFAULT,
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("diver")).infinite().removeAutomatically().hidden().build(),
                                        List.of(Modifier.constant(Attributes.SUBMERGED_MINING_SPEED, 0.8f, AttributeModifier.Operation.ADD_VALUE))
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
