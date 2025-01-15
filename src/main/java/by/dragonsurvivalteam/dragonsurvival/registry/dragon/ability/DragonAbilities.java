package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BlockConversionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.GlowEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.*;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final LevelBasedValue INFINITE_DURATION = LevelBasedValue.constant(DurationInstance.INFINITE_DURATION);

    // TODO :: remove test abilities
    //  or keep them as non-assigned abilities as example? (can be assigned through commands)

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Applies a changing glow color")
    @Translation(type = Translation.Type.ABILITY, comments = "Glow Test")
    public static final ResourceKey<DragonAbility> TEST_GLOW = DragonAbilities.key("test_glow");

    @Translation(type =  Translation.Type.ABILITY_DESCRIPTION, comments = "Frost-walker like effect")
    @Translation(type = Translation.Type.ABILITY, comments = "Frost Walker")
    public static final ResourceKey<DragonAbility> FROST_WALKER = DragonAbilities.key("frost_walker");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        context.register(TEST_GLOW, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.of(Condition.thisEntity(EntityCondition.isNearbyTo(16, EntityType.BEE)).invert().build()),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                                        new GlowEffect(List.of(
                                                Glow.create((DragonSurvival.res("gold")), TextColor.fromLegacyFormat(ChatFormatting.GOLD)),
                                                Glow.create((DragonSurvival.res("dark_purple")), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE)),
                                                Glow.create((DragonSurvival.res("green")), TextColor.fromLegacyFormat(ChatFormatting.GREEN)),
                                                Glow.create((DragonSurvival.res("red")), TextColor.fromLegacyFormat(ChatFormatting.RED)),
                                                Glow.create((DragonSurvival.res("blue")), TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                                        ))),
                                TargetingMode.ALLIES_AND_SELF
                        ), true), LevelBasedValue.constant(1)),
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                        new GlowEffect(List.of(
                                                Glow.create((DragonSurvival.res("gold")), LevelBasedValue.constant(Functions.secondsToTicks(10)), TextColor.fromLegacyFormat(ChatFormatting.GOLD)),
                                                Glow.create((DragonSurvival.res("dark_purple")), LevelBasedValue.constant(Functions.secondsToTicks(10)), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE)),
                                                Glow.create((DragonSurvival.res("green")), LevelBasedValue.constant(Functions.secondsToTicks(10)), TextColor.fromLegacyFormat(ChatFormatting.GREEN)),
                                                Glow.create((DragonSurvival.res("red")), LevelBasedValue.constant(Functions.secondsToTicks(10)), TextColor.fromLegacyFormat(ChatFormatting.RED)),
                                                Glow.create((DragonSurvival.res("blue")), LevelBasedValue.constant(Functions.secondsToTicks(10)), TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                                        ))),
                                TargetingMode.ALLIES_AND_SELF
                        ), LevelBasedValue.constant(10)), LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("test_glow"), 0)
                ))
        ));

        context.register(FROST_WALKER, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.of(Condition.thisEntity(EntityCondition.isOnGround(false)).build()),
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
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("test_glow"), 0)
                ))
        ));
    }

    public static ResourceKey<DragonAbility> key(final ResourceLocation location) {
        return ResourceKey.create(DragonAbility.REGISTRY, location);
    }

    public static ResourceKey<DragonAbility> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
