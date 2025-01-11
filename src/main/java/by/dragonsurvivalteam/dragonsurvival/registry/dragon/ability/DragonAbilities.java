package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.GlowEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    // FIXME :: test
    public static final ResourceKey<DragonAbility> TEST = DragonAbilities.key("test");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        context.register(TEST, new DragonAbility(
                Activation.passive(),
                Optional.empty(),
                Optional.empty(),
                List.of(new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                new GlowEffect(List.of(
                                        new Glow(DragonSurvival.res("one"), TextColor.fromLegacyFormat(ChatFormatting.GOLD), LevelBasedValue.constant(Functions.secondsToTicks(120))),
                                        new Glow(DragonSurvival.res("two"), TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE), LevelBasedValue.constant(Functions.secondsToTicks(120))),
                                        new Glow(DragonSurvival.res("three"), TextColor.fromLegacyFormat(ChatFormatting.GREEN), LevelBasedValue.constant(Functions.secondsToTicks(120))),
                                        new Glow(DragonSurvival.res("four"), TextColor.fromLegacyFormat(ChatFormatting.RED), LevelBasedValue.constant(Functions.secondsToTicks(120))),
                                        new Glow(DragonSurvival.res("five"), TextColor.fromLegacyFormat(ChatFormatting.BLUE), LevelBasedValue.constant(Functions.secondsToTicks(120)))
                                ))),
                        TargetingMode.ALLIES_AND_SELF
                ), LevelBasedValue.constant(10)), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("test"), 0)
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
