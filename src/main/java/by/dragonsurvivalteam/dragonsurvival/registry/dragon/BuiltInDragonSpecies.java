package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonAbilityTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonPenaltyTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class BuiltInDragonSpecies { // TODO :: add `_dragon` to species
    @Translation(type = Translation.Type.DRAGON_SPECIES_ALTAR_DESCRIPTION, comments = {
            "§7■ §6Cave Dragons§r§7 are the spawn of §ffire§r§7. They have little in common with living creatures. Their skeleton is made of §fnetherite§r§7, blood is §flava§r§7, and skin is §frocks§r§7.\n",
            "§2■ Features:§f§r§7 fire resistance, pickaxe claws, lava magic, faster movement on stone and hot blocks.",
            "§4■ Weakness:§r water.",
            "§6■ Diet:§r %s"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES_INVENTORY_DESCRIPTION, comments = {
            "§7■ §6Cave Dragon§r§7 are the spawn of §ffire§r§7. You have little in common with living creatures. Your skeleton is made of §fnetherite§r§7, your blood is §flava§r§7, and your skin is §frocks§r§7.\n",
            "§7■ To “live” you must §ffeed§r§7 on energy to keep you warm. §cWater§r§7 as a chemical element is not compatible with you.",
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Cave Dragon")
    public static final ResourceKey<DragonSpecies> CAVE = key("cave");

    @Translation(type = Translation.Type.DRAGON_SPECIES_ALTAR_DESCRIPTION, comments = {
            "§7■ §6Forest Dragons§r§7 is a §fdiamond§r§7 elementals covered in a multitude of §fplants§r§7. They are excellent §fhunters§r§7 and §ffarmers§r§7.\n",
            "§2■ Features:§f§r§7 soft fall, axe claws, drain and growth magic, faster movement on wooden and grass blocks.",
            "§4■ Weakness:§r§7 dark caves.",
            "§6■ Diet:§r %s"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES_INVENTORY_DESCRIPTION, comments = {
            "§7■ §6Forest Dragon§r§7 is a §fdiamond§r§7 elemental covered in a multitude of §fplants§r§7. You are excellent §fhunter§r§7 and §ffarmer§r§7.\n",
            "§7■ §fRaw meat§r§7, §fpoisonous potatoes§r§7, and certain types of §fthorny shrubs§r§7 are your favorite foods and fertilizer for your plants that hate the §cdarkness§r§7 of caves."
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Forest Dragon")
    public static final ResourceKey<DragonSpecies> FOREST = key("forest");

    @Translation(type = Translation.Type.DRAGON_SPECIES_ALTAR_DESCRIPTION, comments = {
            "§7■ §6Sea Dragons§r§7 are fragile but ferocious §fcreatures§r§7. Their skeletons are made of §fgold§r§7 and conducts §felectricity§r§7 well.\n",
            "§2■ Features:§f§r§7 underwater breathing, shovel claws, electric magic, faster movement on wet and beach blocks.",
            "§4■ Weakness:§r§7 dehydration.",
            "§6■ Diet:§r %s"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES_INVENTORY_DESCRIPTION, comments = {
            "§7■ §6Sea Dragons§r§7 are fragile but ferocious §fcreatures§r§7. Their skeletons are made of §fgold§r§7 and conducts §felectricity§r§7 well.\n",
            "§7■ You have ideally mastered §faquaculture§r§7 and love §fseafood§r§7. You would live forever in the §fwater§r§7, but for the sake of resources you have to leave this cozy world and be exposed to §cdryness§r§7."
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Sea Dragon")
    public static final ResourceKey<DragonSpecies> SEA = key("sea");

    public static void registerTypes(final BootstrapContext<DragonSpecies> context) {
        context.register(CAVE, new DragonSpecies(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                context.lookup(DragonAbility.REGISTRY).getOrThrow(DSDragonAbilityTags.CAVE),
                context.lookup(DragonPenalty.REGISTRY).getOrThrow(DSDragonPenaltyTags.CAVE),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/custom/food_icons/cave_food_icons.png"),
                        new MiscDragonTextures.ManaSprites(
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/full.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/reserved.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/recovery.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/empty.png")
                        ),
                        DragonSurvival.res("textures/gui/custom/altar/cave/altar_icon.png"),
                        DragonSurvival.res("textures/gui/custom/casting_bars/cave/cast_bar.png"),
                        DragonSurvival.res("textures/gui/custom/help_button/cave_help_button.png"),
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

        context.register(SEA, new DragonSpecies(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                context.lookup(DragonAbility.REGISTRY).getOrThrow(DSDragonAbilityTags.SEA),
                context.lookup(DragonPenalty.REGISTRY).getOrThrow(DSDragonPenaltyTags.SEA),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/custom/food_icons/sea_food_icons.png"),
                        new MiscDragonTextures.ManaSprites(
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/full.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/reserved.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/recovery.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/empty.png")
                        ),
                        DragonSurvival.res("textures/gui/custom/altar/sea/altar_icon.png"),
                        DragonSurvival.res("textures/gui/custom/casting_bars/sea/cast_bar.png"),
                        DragonSurvival.res("textures/gui/custom/help_button/sea_help_button.png"),
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

        context.register(FOREST, new DragonSpecies(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                context.lookup(DragonAbility.REGISTRY).getOrThrow(DSDragonAbilityTags.FOREST),
                context.lookup(DragonPenalty.REGISTRY).getOrThrow(DSDragonPenaltyTags.FOREST),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/custom/food_icons/forest_food_icons.png"),
                        new MiscDragonTextures.ManaSprites(
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/full.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/reserved.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/recovery.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/empty.png")
                        ),
                        DragonSurvival.res("textures/gui/custom/altar/forest/altar_icon.png"),
                        DragonSurvival.res("textures/gui/custom/casting_bars/forest/cast_bar.png"),
                        DragonSurvival.res("textures/gui/custom/help_button/forest_help_button.png"),
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

    public static ResourceKey<DragonSpecies> key(final ResourceLocation location) {
        return ResourceKey.create(DragonSpecies.REGISTRY, location);
    }

    private static ResourceKey<DragonSpecies> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
