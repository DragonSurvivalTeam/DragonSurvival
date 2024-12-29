package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalties;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class DragonTypes {
    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION, comments = {
            "§c■ Cave dragon.§r",
            "§2■ Features:§f§r fire resistance, pickaxe claws, fire magic, faster movement on stone and magma blocks.",
            "§4■ Weakness:§r water.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Cave Dragon")
    public static final ResourceKey<DragonType> CAVE = key("cave");

    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION, comments = {
            "§a■ Forest dragon.§r",
            "§2■ Features:§f§r soft fall, axe claws, poison magic, faster movement on wooden and grass blocks.",
            "§4■ Weakness:§r dark caves.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Forest Dragon")
    public static final ResourceKey<DragonType> FOREST = key("forest");

    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION, comments = {
            "§3■ Sea dragon.§r",
            "§2■ Features:§f§r underwater breathing, shovel claws, electric magic, faster movement on ice and beach blocks.",
            "§4■ Weakness:§r dehydration.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Sea Dragon")
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
                        // FIXME :: just for test
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.SUMMON_TEST),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_ATHLETICS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.BURN),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CONTRAST_SHOWER),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.FIRE_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.LAVA_SWIMMING),
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
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.ITEM_BLACKLIST)
                ),
                List.of(),
                List.of(
                        DietEntry.from(ItemTags.FISHES, new FoodProperties(2, 1, true, 1, Optional.empty(), List.of())),
                        DietEntry.from(ItemTags.MEAT, new FoodProperties(8, 3, true, 1, Optional.empty(), List.of())),
                        DietEntry.from(Tags.Items.ORES, new FoodProperties(12, 5, false, 3, Optional.empty(), List.of())),
                        DietEntry.from(Tags.Items.FOODS)
                ),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/food_icons/cave_food_icons.png"),
                        DragonSurvival.res("textures/gui/mana_icons/cave_mana_icons.png"),
                        DragonSurvival.res("textures/gui/custom/altar/cave/altar_icon.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_0.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_1.png"),
                        DragonSurvival.res("textures/gui/casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("textures/gui/help_button/cave_help_button.png"),
                        DragonSurvival.res("textures/gui/growth/circle_cave.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/newborn_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/newborn_stage_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/young_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/young_stage_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/adult_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/adult_stage_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/ancient_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/ancient_stage_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/cave/left_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/cave/left_arrow_main.png")
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/cave/right_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/cave/right_arrow_main.png")
                        ),
                        new MiscDragonTextures.FillIcon(
                                DragonSurvival.res("textures/gui/custom/stage/cave/point_main.png"),
                                DragonSurvival.res("textures/gui/custom/stage/cave/point_hover.png")
                        ),
                        new MiscDragonTextures.FoodTooltip(MiscDragonTextures.FoodTooltip.DEFAULT_FOOD_TOOLTIP_FONT, "\\uEA02", "\\uEA05", Optional.empty()),
                        TextColor.fromRgb(0xE84141),
                        TextColor.fromRgb(0x730B0B)
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
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SPECTRAL_IMPACT),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.HYDRATION),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.ELECTRIC_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.AMPHIBIOUS)
                ),
                HolderSet.direct(
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.THIN_SKIN),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.ITEM_BLACKLIST)
                ),
                List.of(
                        // We need to give the sea dragon some starting resistance time so they don't immediately dehydrate
                        new Modifier(
                                DSAttributes.PENALTY_RESISTANCE_TIME,
                                LevelBasedValue.constant(Functions.secondsToTicks(60)),
                                AttributeModifier.Operation.ADD_VALUE,
                                Optional.empty()
                        )
                ),
                List.of(),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/food_icons/cave_food_icons.png"),
                        DragonSurvival.res("textures/gui/mana_icons/cave_mana_icons.png"),
                        DragonSurvival.res("textures/gui/custom/altar/sea/altar_icon.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_0.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/cave_source_of_magic_1.png"),
                        DragonSurvival.res("textures/gui/casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("textures/gui/help_button/cave_help_button.png"),
                        DragonSurvival.res("textures/gui/growth/circle_cave.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/newborn_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/newborn_stage_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/young_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/young_stage_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/adult_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/adult_stage_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/ancient_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/ancient_stage_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/sea/left_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/sea/left_arrow_main.png")
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/sea/right_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/sea/right_arrow_main.png")
                        ),
                        new MiscDragonTextures.FillIcon(
                                DragonSurvival.res("textures/gui/custom/stage/sea/point_main.png"),
                                DragonSurvival.res("textures/gui/custom/stage/sea/point_hover.png")
                        ),
                        new MiscDragonTextures.FoodTooltip(MiscDragonTextures.FoodTooltip.DEFAULT_FOOD_TOOLTIP_FONT, "\\uEA03", "\\uEA06", Optional.empty()),
                        TextColor.fromRgb(0x2DA5E0),
                        TextColor.fromRgb(0x1D7099)
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
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.HUNTER),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_ATHLETICS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.CLIFFHANGER),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.LIGHT_IN_DARKNESS)
                ),
                HolderSet.direct(
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.FEAR_OF_DARKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.ITEM_BLACKLIST)
                ),
                List.of(),
                List.of(),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/food_icons/forest_food_icons.png"),
                        DragonSurvival.res("textures/gui/mana_icons/forest_mana_icons.png"),
                        DragonSurvival.res("textures/gui/custom/altar/forest/altar_icon.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/forest_source_of_magic_0.png"),
                        DragonSurvival.res("textures/gui/source_of_magic/forest_source_of_magic_1.png"),
                        DragonSurvival.res("textures/gui/casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("textures/gui/help_button/cave_help_button.png"),
                        DragonSurvival.res("textures/gui/growth/circle_cave.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/newborn_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/newborn_stage_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/young_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/young_stage_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/adult_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/adult_stage_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/ancient_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/ancient_stage_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/forest/left_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/forest/left_arrow_main.png")
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/forest/right_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/forest/right_arrow_main.png")
                        ),
                        new MiscDragonTextures.FillIcon(
                                DragonSurvival.res("textures/gui/custom/stage/forest/point_main.png"),
                                DragonSurvival.res("textures/gui/custom/stage/forest/point_hover.png")
                        ),
                        new MiscDragonTextures.FoodTooltip(MiscDragonTextures.FoodTooltip.DEFAULT_FOOD_TOOLTIP_FONT, "\\uEA01", "\\uEA04", Optional.empty()),
                        TextColor.fromRgb(0x41CC48),
                        TextColor.fromRgb(0x117816)
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
