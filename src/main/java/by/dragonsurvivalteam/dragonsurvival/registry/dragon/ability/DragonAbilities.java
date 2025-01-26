package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.ItemCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.MatchItem;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BlockConversionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.GlowEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ModifierEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DiscTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final LevelBasedValue INFINITE_DURATION = LevelBasedValue.constant(DurationInstance.INFINITE_DURATION);

    // TODO :: move these into some example datapack
    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Applies a changing glow color")
    @Translation(type = Translation.Type.ABILITY, comments = "Glow Test")
    public static final ResourceKey<DragonAbility> TEST_GLOW = DragonAbilities.key("test_glow");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Frost-walker like effect")
    @Translation(type = Translation.Type.ABILITY, comments = "Frost Walker Test")
    public static final ResourceKey<DragonAbility> TEST_FROST_WALKER = DragonAbilities.key("test_frost_walker");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Summons some entities")
    @Translation(type = Translation.Type.ABILITY, comments = "Summon Test")
    public static final ResourceKey<DragonAbility> TEST_SUMMON = DragonAbilities.key("test_summon");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        context.register(TEST_GLOW, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.of(Condition.thisEntity(EntityCondition.isNearbyTo(16, EntityType.BEE)).invert().build()),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                        new GlowEffect(List.of(
                                                Glow.create((DragonSurvival.res("gold")), TextColor.fromLegacyFormat(ChatFormatting.GOLD)),
                                                Glow.create((DragonSurvival.res("dark_purple")), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE)),
                                                Glow.create((DragonSurvival.res("green")), TextColor.fromLegacyFormat(ChatFormatting.GREEN)),
                                                Glow.create((DragonSurvival.res("red")), TextColor.fromLegacyFormat(ChatFormatting.RED)),
                                                Glow.create((DragonSurvival.res("blue")), TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                                        ))
                                ), TargetingMode.ALLIES_AND_SELF
                        ), LevelBasedValue.constant(10)), LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(TEST_FROST_WALKER, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).or(Condition.tool(ItemCondition.is(Items.DIAMOND)).invert()).build()),
                List.of(
                        new ActionContainer(new DiscTarget(AbilityTargeting.block(
                                List.of(
                                        new BlockConversionEffect(
                                                List.of(new BlockConversionEffect.BlockConversionData(
                                                        BlockPredicate.allOf(
                                                                BlockPredicate.matchesTag(new Vec3i(0, 1, 0), BlockTags.AIR),
                                                                BlockPredicate.matchesBlocks(Blocks.WATER),
                                                                BlockPredicate.matchesFluids(Fluids.WATER),
                                                                BlockPredicate.unobstructed()
                                                        ),
                                                        WeightedRandomList.create(
                                                                new BlockConversionEffect.BlockTo(Blocks.FROSTED_ICE.defaultBlockState(), 1)
                                                        ))
                                                ),
                                                LevelBasedValue.constant(1.0f))
                                )
                        ), new LevelBasedValue.Clamped(LevelBasedValue.perLevel(3.0F, 1.0F), 0.0F, 16.0F), LevelBasedValue.constant(0.1f), true),
                                LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(TEST_SUMMON, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(3))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))),
                        false,
                        Activation.Sound.end(SoundEvents.UI_TOAST_IN),
                        Optional.of(new Activation.Animations(
                                Optional.of(Either.right(new SimpleAbilityAnimation(SimpleAbilityAnimation.CAST_MASS_BUFF, AnimationLayer.BASE, 2, true, true))),
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation(SimpleAbilityAnimation.MASS_BUFF, AnimationLayer.BASE, 0, true, true))
                        ))
                ),
                Optional.empty(),
                Optional.of(MatchItem.build(ItemCondition.is(Items.MAP), MatchItem.Slot.OFFHAND).invert().build()),
                List.of(
//                        new ActionContainer(new LookingAtTarget(AbilityTargeting.block(List.of(
//                                new SummonEntityEffect(
//                                        DurationInstanceBase.create(DragonSurvival.res("summon_test")).duration(LevelBasedValue.constant(Functions.secondsToTicks(60))).hidden().build(),
//                                        Either.right(context.lookup(Registries.ENTITY_TYPE).getOrThrow(DSEntityTypeTags.HUNTER_FACTION)),
//                                        LevelBasedValue.constant(5),
//                                        List.of(),
//                                        true
//                                )
//                        )), LevelBasedValue.constant(16)), LevelBasedValue.constant(1)),
                        new ActionContainer(new AreaTarget(AbilityTargeting.block(List.of(
                                new SummonEntityEffect(
                                        DurationInstanceBase.create(DragonSurvival.res("summon_test")).duration(LevelBasedValue.constant(Functions.secondsToTicks(60))).hidden().build(),
                                        Either.right(context.lookup(Registries.ENTITY_TYPE).getOrThrow(DSEntityTypeTags.HUNTER_FACTION)),
                                        LevelBasedValue.constant(5),
                                        List.of(),
                                        true
                                )
                        )), LevelBasedValue.constant(12)), LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(ModifierEffect.only(new ModifierWithDuration(
                                DurationInstanceBase.create(DragonSurvival.res("summon_test")).duration(LevelBasedValue.constant(Functions.secondsToTicks(60))).build(),
                                List.of(Modifier.constant(Attributes.ARMOR, 3, AttributeModifier.Operation.ADD_VALUE))
                        )), TargetingMode.ALLIES_AND_SELF)), LevelBasedValue.constant(1))
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
