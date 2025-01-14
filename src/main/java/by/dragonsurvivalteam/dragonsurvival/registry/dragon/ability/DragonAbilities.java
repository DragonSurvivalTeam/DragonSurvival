package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.GlowEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final LevelBasedValue INFINITE_DURATION = LevelBasedValue.constant(DurationInstance.INFINITE_DURATION);

    // TODO :: remove test abilities
    //  or keep them as non-assigned abilities as example? (can be assigned through commands)

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Applies a changing glow color")
    @Translation(type = Translation.Type.ABILITY, comments = "Glow Test")
    public static final ResourceKey<DragonAbility> TEST_GLOW = DragonAbilities.key("test_glow");

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
                                                new Glow(DragonSurvival.res("gold"), TextColor.fromLegacyFormat(ChatFormatting.GOLD), INFINITE_DURATION),
                                                new Glow(DragonSurvival.res("dark_purple"), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE), INFINITE_DURATION),
                                                new Glow(DragonSurvival.res("green"), TextColor.fromLegacyFormat(ChatFormatting.GREEN), INFINITE_DURATION),
                                                new Glow(DragonSurvival.res("red"), TextColor.fromLegacyFormat(ChatFormatting.RED), INFINITE_DURATION),
                                                new Glow(DragonSurvival.res("blue"), TextColor.fromLegacyFormat(ChatFormatting.BLUE), INFINITE_DURATION)
                                        ))),
                                TargetingMode.ALLIES_AND_SELF
                        ), true), LevelBasedValue.constant(1)),
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                        new GlowEffect(List.of(
                                                new Glow(DragonSurvival.res("gold"), TextColor.fromLegacyFormat(ChatFormatting.GOLD), LevelBasedValue.constant(Functions.secondsToTicks(10))),
                                                new Glow(DragonSurvival.res("dark_purple"), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE), LevelBasedValue.constant(Functions.secondsToTicks(10))),
                                                new Glow(DragonSurvival.res("green"), TextColor.fromLegacyFormat(ChatFormatting.GREEN), LevelBasedValue.constant(Functions.secondsToTicks(10))),
                                                new Glow(DragonSurvival.res("red"), TextColor.fromLegacyFormat(ChatFormatting.RED), LevelBasedValue.constant(Functions.secondsToTicks(10))),
                                                new Glow(DragonSurvival.res("blue"), TextColor.fromLegacyFormat(ChatFormatting.BLUE), LevelBasedValue.constant(Functions.secondsToTicks(10)))
                                        ))),
                                TargetingMode.ALLIES_AND_SELF
                        ), LevelBasedValue.constant(10)), LevelBasedValue.constant(1))
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
