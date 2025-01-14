package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.GlowEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import net.minecraft.ChatFormatting;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final LevelBasedValue INFINITE_DURATION = LevelBasedValue.constant(DurationInstance.INFINITE_DURATION);

    // TODO :: remove test abilities
    //  or keep them as non-assigned abilities as example? (can be assigned through commands)

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "Applies a changing glow color")
    @Translation(type = Translation.Type.ABILITY, comments = "Glow Test")
    public static final ResourceKey<DragonAbility> GLOW_TEST = DragonAbilities.key("glow_test");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        context.register(GLOW_TEST, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                new GlowEffect(List.of(
                                        new Glow(DragonSurvival.res("gold"), TextColor.fromLegacyFormat(ChatFormatting.GOLD), INFINITE_DURATION),
                                        new Glow(DragonSurvival.res("dark_purple"), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE), INFINITE_DURATION),
                                        new Glow(DragonSurvival.res("green"), TextColor.fromLegacyFormat(ChatFormatting.GREEN), INFINITE_DURATION),
                                        new Glow(DragonSurvival.res("red"), TextColor.fromLegacyFormat(ChatFormatting.RED), INFINITE_DURATION),
                                        new Glow(DragonSurvival.res("blue"), TextColor.fromLegacyFormat(ChatFormatting.BLUE), INFINITE_DURATION)
                                ))),
                        TargetingMode.ALLIES_AND_SELF
                ), LevelBasedValue.constant(10)), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("glow_test"), 0)
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
