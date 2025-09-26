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
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallFireParticleOption;
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
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BlockBreakEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.FireEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.BreathParticlesEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.DamageEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.DamageModificationEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.FlightEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.HarvestBonusEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.IgniteEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ModifierEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.OnAttackEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.PotionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ProjectileEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SmeltItemEffect;
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
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class CaveDragonAbilities {
    // --- Active --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ The stream of fire that §cignites§r§7 enemies, items and blocks. Is able to §cdestroy§r§7 some blocks.\n",
            "■ §fRange§r§7 depends on age of the dragon.\n",
            "■ §8Cannot be used under water, and during rain.§r"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Nether Breath")
    public static final ResourceKey<DragonAbility> NETHER_BREATH = DragonAbilities.key("nether_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Shoots out a fireball that §cexplodes§r and sets the area on §cfire§r.")
    @Translation(type = Translation.Type.ABILITY, comments = "Fireball")
    public static final ResourceKey<DragonAbility> FIRE_BALL = DragonAbilities.key("fire_ball");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ §2Melts items§r§7 on the ground.\n",
            "■ The melting rate is §ffaster§r§7 when there are fewer items in the stack. Items §fretain§r§7 their melting degree for a short duration if you stop casting, but §fpicking up§r§7 the item will discard all progress.\n",
            "■ Inflicts §clittle damage§r§7 compared to Nether Breath.\n",
            "■ §8Cannot be used under water, and during rain.§r"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Furnace Heat")
    public static final ResourceKey<DragonAbility> FURNACE_HEAT = DragonAbilities.key("furnace_heat");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Gives you and your allies additional §2armor points§r§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Sturdy Skin")
    public static final ResourceKey<DragonAbility> STURDY_SKIN = DragonAbilities.key("sturdy_skin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Makes lava more §2transparent§r for you.")
    @Translation(type = Translation.Type.ABILITY, comments = "Lava Vision")
    public static final ResourceKey<DragonAbility> LAVA_VISION = DragonAbilities.key("lava_vision");

    // --- Passive --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Upgrading this ability increases your §2maximum mana pool§r§7 and allows to restore mana by standing on §fhot blocks§r§7.\n",
            "■ §8The more levels you have, the more mana you get automatically."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Magic")
    public static final ResourceKey<DragonAbility> CAVE_MAGIC = DragonAbilities.key("cave_magic");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Standing on §fstone§7 surfaces will increase your §2movement speed§7.")
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Athletics")
    public static final ResourceKey<DragonAbility> CAVE_ATHLETICS = DragonAbilities.key("cave_athletics");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ You can increase your §2resistance§r§7 to §frain§r§7, §fsnow§r§7 and §fsnowfall§r§7 by upgrading this ability.\n",
            "■ §8Water, potions and snowballs are still dangerous."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Contrast Shower")
    public static final ResourceKey<DragonAbility> CONTRAST_SHOWER = DragonAbilities.key("contrast_shower");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Your target has a chance to receive the §c«Burned»§r effect from your attacks.\n",
            "■ The effect deals damage when the target §fmoves§r§7. The faster the movement, the §fmore damage§r§7 is done.\n",
            "■ §8Creatures with fire resistance are immune to this effect.§r"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Burn")
    public static final ResourceKey<DragonAbility> BURN = DragonAbilities.key("burn");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Cave dragons can §2mine stone blocks§r§7 and various ores §fwithout tools§r§7. This ability gets stronger as you grow.")
    @Translation(type = Translation.Type.ABILITY, comments = "Claws and Teeth")
    public static final ResourceKey<DragonAbility> CAVE_CLAWS_AND_TEETH = DragonAbilities.key("cave_claws_and_teeth");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Dragons use §flevitation§r§7 to §2fly§r§7, but are rarely born with that ability.")
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Wings")
    public static final ResourceKey<DragonAbility> CAVE_WINGS = DragonAbilities.key("cave_wings");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ You can §2spin§r§7 through the §fair§r§7 and in §flava§r§7, boosting your speed. Head to §fthe End§r §7to learn this skill.")
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Spin")
    public static final ResourceKey<DragonAbility> CAVE_SPIN = DragonAbilities.key("cave_spin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Cave dragons have innate §2immunity to fire§r. They feel at home in The Nether."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Fire Immunity")
    public static final ResourceKey<DragonAbility> FIRE_IMMUNITY = DragonAbilities.key("fire_immunity");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Coat your allies with §fprotective dust§r§7 and they get the §2Fire Resistance§r§7 effect.\n",
            "■ Your pets will stop dying by your §ffire§r§7 and your friends will be able to bathe in §flava§r§7. For a while."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Friendly Fire")
    public static final ResourceKey<DragonAbility> FRIENDLY_FIRE = DragonAbilities.key("friendly_fire");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Cave dragons can §2swim in lava§r§7, but they can't stay here too long because the §ftemperature§r§7 is too high.")
    @Translation(type = Translation.Type.ABILITY, comments = "Lava Swimming")
    public static final ResourceKey<DragonAbility> LAVA_SWIMMING = DragonAbilities.key("lava_swimming");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
        registerPassiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(NETHER_BREATH, new DragonAbility(
                new ChanneledActivation(
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.empty(),
                        Notification.DEFAULT,
                        true,
                        Sound.create().start(DSSounds.FIRE_BREATH_START.get()).looping(DSSounds.FIRE_BREATH_LOOP.get()).end(DSSounds.FIRE_BREATH_END.get()).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build())
                                .looping(SimpleAbilityAnimation.create(AnimationKey.BREATH, AnimationLayer.BREATH).transitionLength(5).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))),
                // Disable underwater
                Optional.of(Condition.thisEntity(EntityCondition.isEyeInFluid(NeoForgeMod.WATER_TYPE)).or(Condition.thisEntity(EntityCondition.isInRainOrSnow())).build()),
                List.of(
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.FIRE_BREATH), LevelBasedValue.perLevel(3)),
                                        new IgniteEffect(LevelBasedValue.perLevel(Functions.secondsToTicks(5))),
                                        new PotionEffect(PotionData.create(DSEffects.BURN).duration(10).probability(0.3f).build())
                                ),
                                TargetingMode.NON_ALLIES
                        ), LevelBasedValue.constant(1)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(List.of(
                                new FireEffect(LevelBasedValue.constant(0.05f)),
                                new BlockBreakEffect(BlockCondition.blocks(Blocks.SNOW, Blocks.SHORT_GRASS), LevelBasedValue.constant(1), false),
                                new BlockBreakEffect(BlockCondition.blocks(Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW), LevelBasedValue.perLevel(0.05f), false)
                        )), LevelBasedValue.constant(1)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.04f,
                                        0.02f,
                                        new SmallFireParticleOption(37, true),
                                        new LargeFireParticleOption(37, false)
                                )),
                                TargetingMode.ALL
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/nether_breath_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/nether_breath_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/nether_breath_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/nether_breath_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/nether_breath_4"), 4)
                ))
        ));

        context.register(FURNACE_HEAT, new DragonAbility(
                new ChanneledActivation(
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.030f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.empty(),
                        Notification.DEFAULT,
                        true,
                        Sound.create().start(DSSounds.FIRE_BREATH_START.get()).looping(DSSounds.FIRE_BREATH_LOOP.get()).end(DSSounds.FIRE_BREATH_END.get()).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build())
                                .looping(SimpleAbilityAnimation.create(AnimationKey.BREATH, AnimationLayer.BREATH).transitionLength(5).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 12f, 32f, 64f), LevelBasedValue.perLevel(15)))),
                Optional.of(Condition.thisEntity(EntityCondition.isEyeInFluid(NeoForgeMod.WATER_TYPE)).or(Condition.thisEntity(EntityCondition.isInRainOrSnow())).build()),
                List.of(
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(List.of(
                                new SmeltItemEffect(Optional.empty(), Optional.of(LevelBasedValue.perLevel(1.0f)), true)
                        ), TargetingMode.ITEMS), LevelBasedValue.constant(1)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.FIRE_BREATH), LevelBasedValue.perLevel(0.5f)),
                                        new IgniteEffect(LevelBasedValue.perLevel(Functions.secondsToTicks(1)))
                                ),
                                TargetingMode.NON_ALLIES
                        ), LevelBasedValue.constant(1)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(10)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.04f,
                                        0.02f,
                                        new SmallFireParticleOption(12, true),
                                        new SmallFireParticleOption(27, true)
                                )),
                                TargetingMode.ALL
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/furnace_heat_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/furnace_heat_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/furnace_heat_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/furnace_heat_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/furnace_heat_3"), 4)
                ))
        ));

        context.register(FIRE_BALL, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(7))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().end(SoundEvents.FIRECHARGE_USE).optional(),
                        Animations.create().startAndCharging(SimpleAbilityAnimation.create(AnimationKey.SPELL_CHARGE, AnimationLayer.BREATH).transitionLength(5).build()).optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 20f, 40f, 45f), LevelBasedValue.perLevel(15)))),
                // Disable underwater
                Optional.of(Condition.thisEntity(EntityCondition.isEyeInFluid(NeoForgeMod.WATER_TYPE)).or(Condition.thisEntity(EntityCondition.isInRainOrSnow())).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        Condition.thisEntity(EntityCondition.isLiving()).build(),
                        List.of(new ProjectileEffect(
                                context.lookup(ProjectileData.REGISTRY).getOrThrow(Projectiles.FIREBALL),
                                TargetDirection.lookingAt(),
                                LevelBasedValue.constant(1),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(1)
                        )),
                        TargetingMode.ALL
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/fireball_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/fireball_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/fireball_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/fireball_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/fireball_4"), 4)
                ))
        ));

        context.register(STURDY_SKIN, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(2)),
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
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(0f, 15f, 35f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        List.of(
                                new PotionEffect(PotionData.create(DSEffects.STURDY_SKIN).durationPer(60).build()),
                                new ParticleEffect(
                                        new SpawnParticles(ParticleTypes.MYCELIUM, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.1f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.1f)), ConstantFloat.of(0.05f)),
                                        LevelBasedValue.constant(50)
                                )),
                        TargetingMode.NON_ENEMIES
                ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/sturdy_skin_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/sturdy_skin_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/sturdy_skin_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/sturdy_skin_3"), 3)
                ))
        ));

        context.register(FRIENDLY_FIRE, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(4)),
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
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(26f, 45f, 60f), LevelBasedValue.perLevel(120)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        List.of(
                                new PotionEffect(PotionData.create(MobEffects.FIRE_RESISTANCE).amplifierPer(1).durationPer(264).build()),
                                new ParticleEffect(
                                        new SpawnParticles(ParticleTypes.LAVA, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.1f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.1f)), ConstantFloat.of(0.1f)),
                                        LevelBasedValue.constant(20)
                                )
                        ), TargetingMode.ALL
                ), LevelBasedValue.constant(25)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/friendly_fire_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/friendly_fire_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/friendly_fire_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/friendly_fire_3"), 3)
                ))
        ));

        context.register(LAVA_VISION, new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
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
                // Disable when not on ground (except when in lava)
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false))
                        .and(Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.LAVA))).invert()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        PotionEffect.only(PotionData.create(DSEffects.LAVA_VISION).durationPer(30).build()),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/lava_vision_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/lava_vision_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/lava_vision_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/lava_vision_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/lava_vision_4"), 4)
                ))
        ));
    }

    private static void registerPassiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(CAVE_MAGIC, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(10, LevelBasedValue.perLevel(36))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("cave_magic")).infinite().removeAutomatically().hidden().build(),
                                        List.of(Modifier.per(DSAttributes.MANA, 1, AttributeModifier.Operation.ADD_VALUE))
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                // Enable when on (or within) said block tag, when in lava or when on certain lit blocks
                                AnyOfCondition.anyOf(
                                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_WARM)),
                                        Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_WARM)),
                                        Condition.thisEntity(EntityCondition.isOnBlock(Blocks.FIRE)),
                                        Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.LAVA))),
                                        Condition.thisEntity(EntityCondition.isOnBlock(BlockTags.CAMPFIRES, BlockStateProperties.LIT, true)),
                                        Condition.thisEntity(EntityCondition.isOnBlock(Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES, BlockStateProperties.LIT, true)),
                                        Condition.thisEntity(EntityCondition.isOnBlock(Blocks.SMOKER, BlockStateProperties.LIT, true)),
                                        Condition.thisEntity(EntityCondition.isOnBlock(Blocks.BLAST_FURNACE, BlockStateProperties.LIT, true))
                                ).build(),
                                ModifierEffect.only(new ModifierWithDuration(
                                        DurationInstanceBase.create(DragonSurvival.res("good_mana_condition")).infinite().removeAutomatically().hidden().build(),
                                        List.of(Modifier.per(DSAttributes.MANA_REGENERATION, 0.03f, AttributeModifier.Operation.ADD_VALUE))
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_5"), 5),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_6"), 6),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_7"), 7),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_8"), 8),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_9"), 9),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_magic_10"), 10)
                ))
        ));

        context.register(CAVE_ATHLETICS, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(5, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        // Enable when on said block tag
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.SPEEDS_UP_CAVE_DRAGON)).build(),
                        PotionEffect.only(PotionData.create(MobEffects.MOVEMENT_SPEED).amplifierPer(0.2f).durationPer(1).build()),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(Functions.secondsToTicks(1)))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_athletics_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_athletics_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_athletics_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_athletics_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_athletics_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_athletics_5"), 5)
                ))
        ));

        context.register(CONTRAST_SHOWER, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(8, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("contrast_shower")).infinite().removeAutomatically().hidden().build(),
                                List.of(Modifier.per(DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(10), AttributeModifier.Operation.ADD_VALUE))
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_4"), 4),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_5"), 5),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_6"), 6),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_7"), 7),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/contrast_shower_8"), 8)
                ))
        ));

        context.register(BURN, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ExperiencePointsUpgrade(4, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new OnAttackEffect(PotionData.create(DSEffects.BURN).durationPer(5).probabilityPer(0.15f).build())),
                        TargetingMode.ALL
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/burn_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/burn_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/burn_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/burn_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/burn_4"), 4)
                ))
        ));

        context.register(CAVE_CLAWS_AND_TEETH, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new DragonGrowthUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 40f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        HarvestBonusEffect.only(new HarvestBonus(
                                DurationInstanceBase.create(DragonSurvival.res("cave_claws_and_teeth")).infinite().removeAutomatically().customIcon(DragonSurvival.res("textures/ability_effect/cave_claw.png")).build(),
                                Optional.of(context.lookup(Registries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE)),
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
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_1"), 1),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_2"), 2),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_3"), 3),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_4"), 4)
                ))
        ));

        context.register(CAVE_WINGS, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.flightWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new FlightEffect(1, DragonSurvival.res("textures/ability_effect/cave_dragon_wings.png"))),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_wings_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_wings_1"), 1)
                ))
        ));

        context.register(CAVE_SPIN, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.spinWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new SpinEffect(1, Optional.of(HolderSet.direct(NeoForgeMod.LAVA_TYPE)))),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_spin_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_spin_1"), 1)
                ))
        ));

        context.register(FIRE_IMMUNITY, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        DamageModificationEffect.only(new DamageModification(
                                DurationInstanceBase.create(DragonSurvival.res("fire_immunity")).infinite().removeAutomatically().customIcon(DragonSurvival.res("textures/ability_effect/fire_immunity.png")).build(),
                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_FIRE),
                                LevelBasedValue.constant(0)
                        )),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_dragon_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/cave_dragon_1"), 1)
                ))
        ));

        context.register(LAVA_SWIMMING, new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new SwimEffect(LevelBasedValue.perLevel(Functions.secondsToTicks(180), Functions.secondsToTicks(60)), NeoForgeMod.LAVA_TYPE)),
                        TargetingMode.ALLIES_AND_SELF
                )), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                false, // To prevent regaining oxygen
                new LevelBasedResource(List.of(
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/lava_swimming_0"), 0),
                        new LevelBasedResource.Entry(DragonSurvival.res("abilities/cave/lava_swimming_1"), 1)
                ))
        ));
    }
}
