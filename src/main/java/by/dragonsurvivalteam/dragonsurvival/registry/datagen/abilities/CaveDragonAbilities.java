package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.*;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.CustomPredicates;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.FireEffect;
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
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;
import java.util.Optional;

public class CaveDragonAbilities {
    // --- Active --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Elemental breath: a stream of fire that ignites enemies and blocks. Range depends on age of the dragon.",
            "■ Is able to destroy some blocks. Cannot be used under water, and during rain."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Nether breath")
    public static final ResourceKey<DragonAbility> NETHER_BREATH = DragonAbilities.key("nether_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Ranged attack: shoots out a fireball that §cexplodes§r and sets the area on fire.")
    @Translation(type = Translation.Type.ABILITY, comments = "Fireball")
    public static final ResourceKey<DragonAbility> FIRE_BALL = DragonAbilities.key("fire_ball");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Grants additional armor points to all entities in an area around the dragon.")
    @Translation(type = Translation.Type.ABILITY, comments = "Sturdy Skin")
    public static final ResourceKey<DragonAbility> STURDY_SKIN = DragonAbilities.key("sturdy_skin");

    @Translation(type = Translation.Type.ABILITY_EFFECT, comments = "Sturdy Skin")
    public static final ResourceLocation STURDY_SKIN_MODIFIER = DragonSurvival.res("sturdy_skin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Personal buff: makes lava more §2transparent§r while active.")
    @Translation(type = Translation.Type.ABILITY, comments = "Lava Vision")
    public static final ResourceKey<DragonAbility> LAVA_VISION = DragonAbilities.key("lava_vision");

    // --- Passive --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Upgrading this ability increases your maximum mana pool. Cave dragon mana is restored by standing on hot blocks.\n",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Magic")
    public static final ResourceKey<DragonAbility> CAVE_MAGIC = DragonAbilities.key("cave_magic");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Standing on stone surfaces will increase your movement speed.")
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Athletics")
    public static final ResourceKey<DragonAbility> CAVE_ATHLETICS = DragonAbilities.key("cave_athletics");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ You can increase your resistance to rain, snow and snowfall by upgrading this ability\n",
            "■ Water, potions and snowballs are still dangerous"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Contrast Shower")
    public static final ResourceKey<DragonAbility> CONTRAST_SHOWER = DragonAbilities.key("contrast_shower");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Your target has a chance to receive the §c«Burned»§r effect from your attacks.\n",
            "The effect deals damage when the target moves.\n",
            "The faster the movement, the more damage is done.\n",
            "■ Creatures with fire resistance are immune to this effect."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Burn")
    public static final ResourceKey<DragonAbility> BURN = DragonAbilities.key("burn");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Cave dragons can mine stone blocks and various ores without tools. This ability gets stronger as you grow.\n"
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Claws and Teeth")
    public static final ResourceKey<DragonAbility> CAVE_CLAWS_AND_TEETH = DragonAbilities.key("cave_claws_and_teeth");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Dragons use §2levitation§r to fly, but are rarely born with that ability. Only one dragon in this world can share their power of flight with you.\n",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Wings")
    public static final ResourceKey<DragonAbility> CAVE_WINGS = DragonAbilities.key("cave_wings");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ You can spin through the air and in lava, boosting your speed.\n",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Spin")
    public static final ResourceKey<DragonAbility> CAVE_SPIN = DragonAbilities.key("cave_spin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Cave dragons have a netherite skeleton, and are made mostly of lava.\n",
            "■ They have innate §2immunity to fire§r."
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Fire Immunity")
    public static final ResourceKey<DragonAbility> FIRE_IMMUNITY = DragonAbilities.key("fire_immunity");

    @Translation(type = Translation.Type.ABILITY_EFFECT, comments = "Fire Immunity")
    public static final ResourceLocation FIRE_IMMUNITY_EFFECT = DragonSurvival.res("fire_immunity.fire_immunity");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Cave dragons can swim in lava, but still need to hold their breath when swimming in it.\n",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Lava Swimming")
    public static final ResourceKey<DragonAbility> LAVA_SWIMMING = DragonAbilities.key("lava_swimming");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
        registerPassiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(NETHER_BREATH, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_CHANNELED,
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        true,
                        Optional.of(new Activation.Sound(Optional.of(DSSounds.FIRE_BREATH_START.get()), Optional.empty(), Optional.of(DSSounds.FIRE_BREATH_LOOP.get()), Optional.of(DSSounds.FIRE_BREATH_END.get()))),
                        Optional.of(new Activation.Animations(
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("breath", AnimationLayer.BREATH, 5, false, false)),
                                Optional.empty()
                        ))
                ),
                Optional.of(new LevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().eyeInFluid(NeoForgeMod.WATER_TYPE).build()).build()).build()),
                List.of(new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.FIRE_BREATH), LevelBasedValue.perLevel(3)),
                                        new IgniteEffect(LevelBasedValue.perLevel(Functions.secondsToTicks(5))),
                                        PotionEffect.single(LevelBasedValue.constant(0), LevelBasedValue.constant(Functions.secondsToTicks(10)), LevelBasedValue.constant(0.3f), DSEffects.BURN).getFirst()
                                ),
                                AbilityTargeting.EntityTargetingMode.TARGET_ENEMIES
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(List.of(new FireEffect(LevelBasedValue.constant(0.05f)))), LevelBasedValue.constant(1)), LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.04f,
                                        0.02f,
                                        new SmallFireParticleOption(37, true),
                                        new LargeFireParticleOption(37, false)
                                )),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALL
                        ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/nether_breath_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/nether_breath_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/nether_breath_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/nether_breath_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/nether_breath_4"), 4)
                ))
        ));

        context.register(FIRE_BALL, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(7))),
                        true,
                        Optional.of(new Activation.Sound(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(SoundEvents.FIRECHARGE_USE))),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation("breath", AnimationLayer.BREATH, 5, false, false))),
                                Optional.empty(),
                                Optional.empty()
                        ))
                ),
                Optional.of(new LevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 20f, 40f, 45f), LevelBasedValue.perLevel(15)))),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().eyeInFluid(NeoForgeMod.WATER_TYPE).build()).build()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        Condition.thisEntity(EntityCondition.isLiving()).build(),
                        List.of(new ProjectileEffect(
                                context.lookup(ProjectileData.REGISTRY).getOrThrow(Projectiles.FIREBALL),
                                TargetDirection.lookingAt(),
                                LevelBasedValue.constant(1),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(1)
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALL
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/fireball_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/fireball_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/fireball_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/fireball_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/fireball_4"), 4)
                ))
        ));

        context.register(STURDY_SKIN, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        false,
                        Optional.of(new Activation.Sound(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(SoundEvents.UI_TOAST_IN))),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation(SimpleAbilityAnimation.CAST_MASS_BUFF, AnimationLayer.BASE, 2, true, true))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation(SimpleAbilityAnimation.MASS_BUFF, AnimationLayer.BASE, 0, true, true))
                        ))
                ),
                Optional.of(new LevelUpgrade(3, LevelBasedValue.lookup(List.of(0f, 15f, 35f), LevelBasedValue.perLevel(15)))),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(false)).build()).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        PotionEffect.single(LevelBasedValue.constant(0), LevelBasedValue.perLevel(Functions.secondsToTicks(60)), DSEffects.STURDY_SKIN),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), LevelBasedValue.constant(5)), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/sturdy_skin_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/sturdy_skin_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/sturdy_skin_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/sturdy_skin_3"), 3)
                ))
        ));

        context.register(LAVA_VISION, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        false,
                        Optional.of(new Activation.Sound(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(SoundEvents.UI_TOAST_IN))),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation("cast_self_buff", AnimationLayer.BASE, 2, true, false))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("self_buff", AnimationLayer.BASE, 0, true, false))
                        ))
                ),
                Optional.of(new LevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 45f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(false)).build()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        PotionEffect.single(LevelBasedValue.constant(0), LevelBasedValue.perLevel(Functions.secondsToTicks(30)), DSEffects.LAVA_VISION),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/lava_vision_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/lava_vision_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/lava_vision_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/lava_vision_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/lava_vision_4"), 4)
                ))
        ));
    }

    private static void registerPassiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(CAVE_MAGIC, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(10, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.single(new ModifierWithDuration(
                                        DragonSurvival.res("cave_magic"),
                                        ModifierWithDuration.DEFAULT_MODIFIER_ICON,
                                        List.of(new Modifier(DSAttributes.MANA, LevelBasedValue.perLevel(1), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                        true
                                )),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                        ), true), LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.REGENERATES_CAVE_DRAGON_MANA))
                                        .or(Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.REGENERATES_CAVE_DRAGON_MANA))).build(),
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
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_5"), 5),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_6"), 6),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_7"), 7),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_8"), 8),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_9"), 9),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_magic_10"), 10)
                ))
        ));

        context.register(CAVE_ATHLETICS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(5, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.SPEEDS_UP_CAVE_DRAGON)).build(),
                        PotionEffect.single(LevelBasedValue.perLevel(1), LevelBasedValue.perLevel(Functions.secondsToTicks(5)), MobEffects.MOVEMENT_SPEED),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), false), LevelBasedValue.constant(Functions.secondsToTicks(1)))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_athletics_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_athletics_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_athletics_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_athletics_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_athletics_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_athletics_5"), 5)
                ))
        ));

        context.register(CONTRAST_SHOWER, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(5, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.single(new ModifierWithDuration(
                                DragonSurvival.res("contrast_shower"),
                                ModifierWithDuration.DEFAULT_MODIFIER_ICON,
                                List.of(new Modifier(DSAttributes.PENALTY_RESISTANCE_TIME, LevelBasedValue.perLevel(Functions.secondsToTicks(30)), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                true
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/contrast_shower_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/contrast_shower_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/contrast_shower_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/contrast_shower_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/contrast_shower_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/contrast_shower_5"), 5)
                ))
        ));

        context.register(BURN, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperienceUpgrade(4, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new OnAttackEffect(PotionData.of(LevelBasedValue.constant(0), LevelBasedValue.perLevel(Functions.secondsToTicks(5)), LevelBasedValue.perLevel(0.15f), DSEffects.BURN))),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALL
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/burn_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/burn_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/burn_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/burn_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/burn_4"), 4)
                ))
        ));

        context.register(CAVE_CLAWS_AND_TEETH, new DragonAbility(
                Activation.passive(),
                Optional.of(new SizeUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 40f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        HarvestBonusEffect.single(new HarvestBonus(
                                DragonSurvival.res("cave_claws_and_teeth"),
                                Optional.of(context.lookup(Registries.BLOCK).getOrThrow(DSBlockTags.CAVE_DRAGON_HARVESTABLE)),
                                LevelBasedValue.perLevel(1, 0.5f),
                                LevelBasedValue.perLevel(0.5f),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_claws_and_teeth_4"), 4)
                ))
        ));

        context.register(CAVE_WINGS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ItemUpgrade(List.of(HolderSet.direct(DSItems.WING_GRANT_ITEM)), HolderSet.empty())),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().markedByEnderDragon(true).build()).build()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new FlightEffect(1)),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_wings_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_wings_1"), 1)
                ))
        ));

        context.register(CAVE_SPIN, new DragonAbility(
                Activation.passive(),
                Optional.of(new ItemUpgrade(List.of(HolderSet.direct(DSItems.SPIN_GRANT_ITEM)), HolderSet.empty())),
                Optional.of(Condition.thisEntity(EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().markedByEnderDragon(true).build()).build()).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new SpinEffect(1, Optional.of(NeoForgeMod.LAVA_TYPE))),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALLIES
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_wings_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_wings_1"), 1)
                ))
        ));

        context.register(FIRE_IMMUNITY, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        DamageModificationEffect.single(new DamageModification(
                                FIRE_IMMUNITY_EFFECT,
                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_FIRE),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)
                        )),
                        AbilityTargeting.EntityTargetingMode.TARGET_ALL
                ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_dragon_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/cave_dragon_1"), 1)
                ))
        ));

        context.register(LAVA_SWIMMING, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new SwimEffect(LevelBasedValue.perLevel(Functions.secondsToTicks(180), Functions.secondsToTicks(60)), NeoForgeMod.LAVA_TYPE)),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALL
                        ), true), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/lava_swimming_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/cave/lava_swimming_1"), 1)
                ))
        ));
    }
}
