package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.*;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.BlockCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.ItemCondition;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargePoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallPoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AreaCloudEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BlockBreakEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BlockConversionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BonemealEffect;
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
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.DragonSizeUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceLevelUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;
import java.util.Optional;

public class ForestDragonAbilities {
    // --- Active --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Elemental breath: a toxic gas that creates a §c«Drain»§r area of effect, which is deadly for creatures, but helps plants grow faster.\n",
            "■ Range depends on the age of the dragon. Cannot be used while affected by §c«Stress»§r.",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Poison Breath")
    public static final ResourceKey<DragonAbility> POISON_BREATH = DragonAbilities.key("poison_breath");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Shoot out sharp §cdarts§r, which fly a large distance to pierce your target. Less effective underwater.")
    @Translation(type = Translation.Type.ABILITY, comments = "Spike")
    public static final ResourceKey<DragonAbility> SPIKE = DragonAbilities.key("spike");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Provides §2Haste§r to all nearby creatures, increasing your block harvesting speed.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Inspiration")
    public static final ResourceKey<DragonAbility> INSPIRATION = DragonAbilities.key("inspiration");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Activates the §2Hunter§r effect, which allows you to become invisible on grassy ground. Your first melee strike will remove this effect and cause a critical hit with a  %-based damage bonus.\n",
            "■ Effect does not stack. Cannot be used in flight. Will be removed early if you take damage, or attack a target.",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Hunter")
    public static final ResourceKey<DragonAbility> HUNTER = DragonAbilities.key("hunter");

    // --- Passive --- //

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Upgrading this ability increases your maximum mana pool. Additionally, mana regeneration will be sped up while standing on grass or under direct sunlight.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Magic")
    public static final ResourceKey<DragonAbility> FOREST_MAGIC = DragonAbilities.key("forest_magic");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Standing on grassy or wooden surfaces will increase your movement speed.")
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Athletics")
    public static final ResourceKey<DragonAbility> FOREST_ATHLETICS = DragonAbilities.key("forest_athletics");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Allows you to stay longer in dark areas.")
    @Translation(type = Translation.Type.ABILITY, comments = "Light the Dark")
    public static final ResourceKey<DragonAbility> LIGHT_IN_DARKNESS = DragonAbilities.key("light_in_darkness");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Your landing becomes much softer.")
    @Translation(type = Translation.Type.ABILITY, comments = "Cliffhanger")
    public static final ResourceKey<DragonAbility> CLIFFHANGER = DragonAbilities.key("cliffhanger");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Forest dragons can chop trees without tools. This ability gets stronger as you grow.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Claws and Teeth")
    public static final ResourceKey<DragonAbility> FOREST_CLAWS_AND_TEETH = DragonAbilities.key("forest_claws_and_teeth");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Dragons use §2levitation§r to fly, but are rarely born with that ability.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Wings")
    public static final ResourceKey<DragonAbility> FOREST_WINGS = DragonAbilities.key("forest_wings");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ You can spin through the air, boosting your speed. Head to §2the End§r §7to learn this skill. You can find this knowledge in The End.\n")
    @Translation(type = Translation.Type.ABILITY, comments = "Cave Spin")
    public static final ResourceKey<DragonAbility> FOREST_SPIN = DragonAbilities.key("forest_spin");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Forest dragons have a diamond skeleton, and are composed mostly of predatory plants. Their diet includes raw meat and sweet berries, and most animals fear them.\n",
            "■ They have innate §2immunity to thorn bushes and cacti§r§7. They feel best on the surface of the Overworld.",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Forest Immunity")
    public static final ResourceKey<DragonAbility> FOREST_IMMUNITY = DragonAbilities.key("forest_immunity");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
        registerPassiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(POISON_BREATH, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_CHANNELED,
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        true,
                        Activation.Sound.of(DSSounds.FOREST_BREATH_START.get(), null, DSSounds.FOREST_BREATH_LOOP.get(), DSSounds.FOREST_BREATH_END.get()),
                        Optional.of(new Activation.Animations(
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("breath", AnimationLayer.BREATH, 5, false, false)),
                                Optional.empty()
                        ))
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))),
                // Disable when affected by the 'STRESS' effect
                Optional.of(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.STRESS)).build()),
                List.of(
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isLiving()).build(),
                                List.of(
                                        new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.POISON_BREATH), LevelBasedValue.perLevel(2)),
                                        PotionEffect.only(LevelBasedValue.constant(0), LevelBasedValue.constant(Functions.secondsToTicks(10)), LevelBasedValue.constant(0.3f), DSEffects.DRAIN).getFirst()
                                ),
                                TargetingMode.NON_ALLIES
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.entity(
                                Condition.thisEntity(EntityCondition.isItem()).build(),
                                List.of(new ItemConversionEffect(
                                        List.of(new ItemConversionEffect.ItemConversionData(ItemCondition.is(Items.POTATO), WeightedRandomList.create(ItemConversionEffect.ItemTo.of(Items.POISONOUS_POTATO)))),
                                        LevelBasedValue.constant(0.5f)
                                )),
                                TargetingMode.ALL
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(AbilityTargeting.block(
                                List.of(
                                        new BonemealEffect(LevelBasedValue.constant(2), LevelBasedValue.perLevel(0.01f)),
                                        new BlockConversionEffect(List.of(new BlockConversionEffect.BlockConversionData(
                                                BlockCondition.blocks(Blocks.DIRT, Blocks.COARSE_DIRT),
                                                SimpleWeightedRandomList.create(
                                                        new BlockConversionEffect.BlockTo(Blocks.GRASS_BLOCK.defaultBlockState(), 25),
                                                        new BlockConversionEffect.BlockTo(Blocks.PODZOL.defaultBlockState(), 5),
                                                        new BlockConversionEffect.BlockTo(Blocks.MYCELIUM.defaultBlockState(), 1),
                                                        new BlockConversionEffect.BlockTo(Blocks.COARSE_DIRT.defaultBlockState(), 3)
                                                ))
                                        ), LevelBasedValue.constant(0.2f)),
                                        new BlockBreakEffect(BlockCondition.blocks(Blocks.POTATOES), LevelBasedValue.constant(0.2f)),
                                        new AreaCloudEffect(
                                                PotionData.of(LevelBasedValue.constant(0), LevelBasedValue.constant(Functions.secondsToTicks(30)), DSEffects.DRAIN),
                                                LevelBasedValue.constant(Functions.secondsToTicks(2)),
                                                0.3,
                                                new LargePoisonParticleOption(37, false)
                                        )
                                )
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(new BreathParticlesEffect(
                                        0.04f,
                                        0.02f,
                                        new SmallPoisonParticleOption(37, true),
                                        new LargePoisonParticleOption(37, false)
                                )),
                                TargetingMode.ALL
                        ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_4"), 4)
                ))
        ));

        context.register(SPIKE, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(3))),
                        true,
                        Activation.Sound.end(SoundEvents.ARROW_SHOOT),
                        Optional.empty()
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
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(
                        List.of(
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_0"), 0),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_1"), 1),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_2"), 2),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_3"), 3),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_4"), 4)
                        )
                )
        ));

        context.register(INSPIRATION, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        false,
                        Activation.Sound.end(SoundEvents.UI_TOAST_IN),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation(SimpleAbilityAnimation.CAST_MASS_BUFF, AnimationLayer.BASE, 2, true, true))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation(SimpleAbilityAnimation.MASS_BUFF, AnimationLayer.BASE, 0, true, true))
                        ))
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(0f, 15f, 35f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                        PotionEffect.only(LevelBasedValue.perLevel(1), LevelBasedValue.constant(Functions.secondsToTicks(200)), MobEffects.DIG_SPEED),
                        TargetingMode.ALLIES_AND_SELF
                ), LevelBasedValue.constant(5)), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/inspiration_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/inspiration_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/inspiration_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/inspiration_3"), 3)
                ))
        ));

        context.register(HUNTER, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        false,
                        Activation.Sound.end(SoundEvents.UI_TOAST_IN),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation(SimpleAbilityAnimation.CAST_SELF_BUFF, AnimationLayer.BASE, 2, true, true))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation(SimpleAbilityAnimation.SELF_BUFF, AnimationLayer.BASE, 0, true, true))
                        ))
                ),
                Optional.of(new ExperienceLevelUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 35f, 55f), LevelBasedValue.perLevel(15)))),
                // Disable when not on ground
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        PotionEffect.only(LevelBasedValue.perLevel(1), LevelBasedValue.perLevel(Functions.secondsToTicks(30)), DSEffects.HUNTER),
                        TargetingMode.ALLIES_AND_SELF
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/hunter_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/hunter_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/hunter_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/hunter_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/hunter_4"), 4)
                ))
        ));
    }

    private static void registerPassiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(FOREST_MAGIC, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(10, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                ModifierEffect.only(new ModifierWithDuration(
                                        DragonSurvival.res("forest_magic"),
                                        List.of(new Modifier(DSAttributes.MANA, LevelBasedValue.perLevel(1), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                        Optional.empty(),
                                        true
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        ), true), LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                // Enable when on (or within) said block tag or when under sunlight with a strength of at least 10
                                Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_GRASSY))
                                        .or(Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_GRASSY)))
                                        .or(Condition.thisEntity(EntityCondition.isInSunlight(10))).build(),
                                ModifierEffect.only(new ModifierWithDuration(
                                        DragonSurvival.res("good_mana_condition"),
                                        List.of(new Modifier(DSAttributes.MANA_REGENERATION, LevelBasedValue.perLevel(1), AttributeModifier.Operation.ADD_MULTIPLIED_BASE, Optional.empty())),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                        Optional.empty(),
                                        true
                                )),
                                TargetingMode.ALLIES_AND_SELF
                        ), true), LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_5"), 5),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_6"), 6),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_7"), 7),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_8"), 8),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_9"), 9),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_magic_10"), 10)
                ))
        ));

        context.register(FOREST_ATHLETICS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(5, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        // Enable when on said block tag
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.SPEEDS_UP_FOREST_DRAGON)).build(),
                        PotionEffect.only(LevelBasedValue.perLevel(1), LevelBasedValue.perLevel(Functions.secondsToTicks(5)), MobEffects.MOVEMENT_SPEED),
                        TargetingMode.ALLIES_AND_SELF
                ), false), LevelBasedValue.constant(Functions.secondsToTicks(1)))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_athletics_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_athletics_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_athletics_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_athletics_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_athletics_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_athletics_5"), 5)
                ))
        ));

        context.register(LIGHT_IN_DARKNESS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(8, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DragonSurvival.res("light_in_darkness"),
                                List.of(new Modifier(DSAttributes.PENALTY_RESISTANCE_TIME, LevelBasedValue.perLevel(Functions.secondsToTicks(10)), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                Optional.empty(),
                                true
                        )),
                        TargetingMode.ALLIES_AND_SELF
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_5"), 5),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_6"), 6),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_7"), 7),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/light_in_darkness_8"), 8)
                ))
        ));

        context.register(CLIFFHANGER, new DragonAbility(
                Activation.passive(),
                Optional.of(new ExperiencePointsUpgrade(6, LevelBasedValue.perLevel(15))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        ModifierEffect.only(new ModifierWithDuration(
                                DragonSurvival.res("cliffhanger"),
                                List.of(new Modifier(Attributes.SAFE_FALL_DISTANCE, LevelBasedValue.perLevel(5, 1), AttributeModifier.Operation.ADD_VALUE, Optional.empty())),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                Optional.empty(),
                                true
                        )),
                        TargetingMode.ALLIES_AND_SELF
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/cliffhanger_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/cliffhanger_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/cliffhanger_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/cliffhanger_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/cliffhanger_4"), 4),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/cliffhanger_5"), 5),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/cliffhanger_6"), 6)
                ))
        ));

        context.register(FOREST_CLAWS_AND_TEETH, new DragonAbility(
                Activation.passive(),
                Optional.of(new DragonSizeUpgrade(4, LevelBasedValue.lookup(List.of(0f, 25f, 40f, 60f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        HarvestBonusEffect.only(new HarvestBonus(
                                DragonSurvival.res("forest_claws_and_teeth"),
                                Optional.of(context.lookup(Registries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_AXE)),
                                LevelBasedValue.perLevel(1, 0.5f),
                                LevelBasedValue.perLevel(0.5f),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                Optional.empty(),
                                false
                        )),
                        TargetingMode.ALLIES_AND_SELF
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_claws_and_teeth_4"), 4)
                ))
        ));

        context.register(FOREST_WINGS, new DragonAbility(
                Activation.passive(),
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.flightWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new FlightEffect(1)),
                        TargetingMode.ALLIES_AND_SELF
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_wings_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_wings_1"), 1)
                ))
        ));

        context.register(FOREST_SPIN, new DragonAbility(
                Activation.passive(),
                Optional.of(new ConditionUpgrade(List.of(Condition.thisEntity(EntityCondition.spinWasGranted(true)).build()), false)),
                // Disable when marked by the ender dragon
                Optional.of(Condition.thisEntity(EntityCondition.isMarked(true)).build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        List.of(new SpinEffect(1, Optional.of(NeoForgeMod.WATER_TYPE))),
                        TargetingMode.ALLIES_AND_SELF
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_spin_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_spin_1"), 1)
                ))
        ));

        context.register(FOREST_IMMUNITY, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                        DamageModificationEffect.only(new DamageModification(
                                DragonSurvival.res("forest_immunity"),
                                HolderSet.direct(
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.SWEET_BERRY_BUSH),
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.CACTUS),
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.DRAIN),
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.POISON_BREATH)
                                ),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                Optional.empty(),
                                false
                        )),
                        TargetingMode.ALL
                ), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_dragon_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/forest_dragon_1"), 1)
                ))
        ));
    }
}
