package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final ResourceLocation GOOD_MANA_CONDITION = DragonSurvival.res("good_mana_condition");

    @Translation(type = Translation.Type.MODIFIER, comments = "Wings")
    public static final ResourceLocation WINGS_MODIFIER = DragonSurvival.res("wings");

    private static final ResourceLocation WINGS_ICON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/modifiers/cave_dragon_wings.png");
    private static final ModifierWithDuration FLIGHT_MODIFIER = new ModifierWithDuration(WINGS_MODIFIER, WINGS_ICON, List.of(), LevelBasedValue.constant(DurationInstance.INFINITE_DURATION), false);
    public static final ModifierWithDuration.Instance FLIGHT_INSTANCE = new ModifierWithDuration.Instance(FLIGHT_MODIFIER, new ClientEffectProvider.ClientData(WINGS_ICON, /* TODO */ Component.empty(), Optional.empty()), 0, DurationInstance.INFINITE_DURATION, new HashMap<>());

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);
    }

    public static ResourceKey<DragonAbility> key(final ResourceLocation location) {
        return ResourceKey.create(DragonAbility.REGISTRY, location);
    }

    public static ResourceKey<DragonAbility> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
