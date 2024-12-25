package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalties;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.food.FoodProperties;

import java.util.List;
import java.util.Optional;

public class DragonTypes {
    @Translation(type = Translation.Type.DRAGON_TYPE_DESCRIPTION, comments = {
            "§c■ Cave dragon.§r",
            "§2■ Features:§f§r fire resistance, pickaxe claws, fire magic, faster movement on stone and magma blocks.",
            "§4■ Weakness:§r water.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_TYPE, comments = "Cave Dragon")
    public static final ResourceKey<DragonType> CAVE = key("cave");

    @Translation(type = Translation.Type.DRAGON_TYPE_DESCRIPTION, comments = {
            "§a■ Forest dragon.§r",
            "§2■ Features:§f§r soft fall, axe claws, poison magic, faster movement on wooden and grass blocks.",
            "§4■ Weakness:§r dark caves.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_TYPE, comments = "Forest Dragon")
    public static final ResourceKey<DragonType> FOREST = key("forest");

    @Translation(type = Translation.Type.DRAGON_TYPE_DESCRIPTION, comments = {
            "§3■ Sea dragon.§r",
            "§2■ Features:§f§r underwater breathing, shovel claws, electric magic, faster movement on ice and beach blocks.",
            "§4■ Weakness:§r dehydration.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_TYPE, comments = "Sea Dragon")
    public static final ResourceKey<DragonType> SEA = key("sea");

    public static void registerTypes(final BootstrapContext<DragonType> context) {
        context.register(CAVE, new DragonType(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                HolderSet.direct(
                        // Active
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.NETHER_BREATH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.FIRE_BALL),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.LAVA_VISION),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.TOUGH_SKIN),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_ATHLETICS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.BURN),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CONTRAST_SHOWER),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.FIRE_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.EMPTY_ABILITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.EMPTY_ABILITY_2),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.EMPTY_ABILITY_3),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.EMPTY_ABILITY_4),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.EMPTY_ABILITY_5),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.EMPTY_ABILITY_6),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.EMPTY_ABILITY_7)
                ),
                HolderSet.direct(
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.SNOW_AND_RAIN_WEAKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.WATER_WEAKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.LAVA_SWIMMING),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.ITEM_BLACKLIST)
                ),
                List.of(),
                List.of(DietEntry.from("#minecraft:doors", new FoodProperties(2, 1, true, 1, Optional.empty(), List.of()))),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/food_icons/cave_food_icons.png"),
                        DragonSurvival.res("textures/gui/mana_icons/cave_mana_icons.png"),
                        DragonSurvival.res("textures/gui/dragon_altar/cave_altar_icon.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_0.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_1.png"),
                        DragonSurvival.res("textures/gui/casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("textures/gui/help_button/cave_help_button.png"),
                        DragonSurvival.res("textures/gui/growth/circle_cave.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/stage/cave/newborn_hover.png"),
                                        DragonSurvival.res("textures/gui/stage/cave/newborn_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/stage/cave/young_hover.png"),
                                        DragonSurvival.res("textures/gui/stage/cave/young_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/stage/cave/adult_hover.png"),
                                        DragonSurvival.res("textures/gui/stage/cave/adult_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/stage/cave/ancient_hover.png"),
                                        DragonSurvival.res("textures/gui/stage/cave/ancient_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new ColorRGBA(16711680),
                        new ColorRGBA(16711680)
                )
        ));

        context.register(SEA, new DragonType(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                HolderSet.direct(
                        // Active
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.BALL_LIGHTNING),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.STORM_BREATH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_EYES),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SOUL_REVELATION),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_ATHLETICS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SPECTRAL_IMPACT)
                ),
                HolderSet.empty(),
                List.of(),
                List.of(),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/food_icons/cave_food_icons.png"),
                        DragonSurvival.res("textures/gui/mana_icons/cave_mana_icons.png"),
                        DragonSurvival.res("textures/gui/dragon_altar/sea_altar_icon.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_0.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_1.png"),
                        DragonSurvival.res("textures/gui/casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("textures/gui/help_button/cave_help_button.png"),
                        DragonSurvival.res("textures/gui/growth/circle_cave.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/sea/newborn_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/sea/newborn_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/sea/young_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/sea/young_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/sea/adult_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/sea/adult_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/sea/ancient_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/sea/ancient_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new ColorRGBA(16711680),
                        new ColorRGBA(16711680)
                )
        ));

        context.register(FOREST, new DragonType(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                HolderSet.direct(
                        // Active
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.SPIKE),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.POISON_BREATH),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_ATHLETICS)
                ),
                HolderSet.empty(),
                List.of(),
                List.of(),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/food_icons/forest_food_icons.png"),
                        DragonSurvival.res("textures/gui/mana_icons/forest_mana_icons.png"),
                        DragonSurvival.res("textures/gui/dragon_altar/forest_altar_icon.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/forest_source_of_magic_0.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/forest_source_of_magic_1.png"),
                        DragonSurvival.res("textures/gui/casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("textures/gui/help_button/cave_help_button.png"),
                        DragonSurvival.res("textures/gui/growth/circle_cave.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/forest/newborn_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/forest/newborn_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/forest/young_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/forest/young_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/forest/adult_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/forest/adult_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/growth/forest/ancient_hover.png"),
                                        DragonSurvival.res("textures/gui/growth/forest/ancient_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new ColorRGBA(16711680),
                        new ColorRGBA(16711680)
                )
        ));
    }

    public static ResourceKey<DragonType> key(final ResourceLocation location) {
        return ResourceKey.create(DragonType.REGISTRY, location);
    }

    private static ResourceKey<DragonType> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
