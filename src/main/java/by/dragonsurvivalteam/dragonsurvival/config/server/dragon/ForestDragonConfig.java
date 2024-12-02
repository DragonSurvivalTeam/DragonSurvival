//package by.dragonsurvivalteam.dragonsurvival.config.server.dragon;
//
//import by.dragonsurvivalteam.dragonsurvival.config.ConfigUtils;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.Validation;
//import by.dragonsurvivalteam.dragonsurvival.config.types.BlockStateConfig;
//import by.dragonsurvivalteam.dragonsurvival.config.types.FoodConfigCollector;
//import by.dragonsurvivalteam.dragonsurvival.config.types.ItemHurtConfig;
//import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
//import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
//import by.dragonsurvivalteam.dragonsurvival.util.Functions;
//import net.minecraft.world.item.Items;
//import net.neoforged.neoforge.common.Tags;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class ForestDragonConfig {
//    // --- Magic --- //
//
//    @Translation(key = "forest_abilities", type = Translation.Type.CONFIGURATION, comments = "Enable / Disable forest dragon abilities")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "magic"}, key = "forest_abilities")
//    public static Boolean areAbilitiesEnabled = true;
//
//    @Translation(key = "forest_conditional_mana_blocks", type = Translation.Type.CONFIGURATION, comments = "Blocks that restore mana for forest dragons when under certain conditions (block states) - Formatting: namespace:path:key=value,key=value (prefix namespace with # for tags)")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "magic"}, key = "forest_conditional_mana_blocks")
//    public static List<BlockStateConfig> forestConditionalManaBlocks = List.of();
//
//    // --- Bonus --- //
//
//    @ConfigRange(min = 0.0, max = 100.0)
//    @Translation(key = "forest_fall_reduction", type = Translation.Type.CONFIGURATION, comments = "By how much (in blocks) the fall distance is reduced (resulting in reduced damage when falling) - disabled if set to 0")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "bonuses"}, key = "forest_fall_reduction")
//    public static Double fallReduction = 5.0;
//
//    @Translation(key = "forest_bush_immunity", type = Translation.Type.CONFIGURATION, comments = "Forest dragons will be immune to sweet berry bush damage if enabled")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "bonuses"}, key = "forest_bush_immunity")
//    public static Boolean bushImmunity = true;
//
//    @Translation(key = "forest_cactus_immunity", type = Translation.Type.CONFIGURATION, comments = "Forest dragons will be immune to cactus damage if enabled")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "bonuses"}, key = "forest_cactus_immunity")
//    public static Boolean cactusImmunity = true;
//
//    // --- Penalties --- //
//
//    @ConfigRange(min = 0, max = 10_000)
//    @Translation(key = "forest_stress_ticks", type = Translation.Type.CONFIGURATION, comments = "The amount of ticks (20 ticks = 1 second) can pass before the forest dragon is affected by the stress effect - stress effect is disabled if set to 0")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "penalties"}, key = "forest_stress_ticks")
//    public static Integer stressTicks = Functions.secondsToTicks(5);
//
//    @ConfigRange(min = 2, max = 100_000)
//    @Translation(key = "forest_stress_effect_duration", type = Translation.Type.CONFIGURATION, comments = "How many seconds the stress effect lasts for")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "penalties"}, key = "forest_stress_effect_duration")
//    public static Integer stressEffectDuration = 10;
//
//    @ConfigRange(min = 0.1, max = 4.0)
//    @Translation(key = "forest_stress_exhaustion_rate", type = Translation.Type.CONFIGURATION, comments = "The amount of exhaustion applied every 10 tick (0.5 seconds) during the stress effect")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "penalties"}, key = "forest_stress_exhaustion_rate")
//    public static Double stressExhaustion = 1.0;
//
//    // --- Food --- //
//
//    @Translation(key = "forest_hurtful_items", type = Translation.Type.CONFIGURATION, comments = "Items which will cause damage to forest dragons when consumed - Formatting: namespace:path:damage (prefix namespace with # for tags)")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "food"}, key = "forest_hurtful_items", validation = Validation.RESOURCE_LOCATION_NUMBER)
//    public static List<ItemHurtConfig> hurtfulItems = List.of();
//
//    @Translation(key = "forest_foods", type = Translation.Type.CONFIGURATION, comments = {
//            "Determines which items a forest dragon can eat - the item doesn't need to be a food item (e.g. you could add an iron block here",
//            "Formatting: namespace:path:nutrition:saturation (prefix namespace with # for tags)",
//            "Nutrition (whole number) and saturation (can be specified in decimals) are optional - if they're missing the items original values will be used (or 1:0)"
//    })
//    @ConfigOption(side = ConfigSide.SERVER, category = {"forest_dragon", "food"}, key = "forest_foods")
//    public static List<FoodConfigCollector> validFood = Arrays.asList(
//            FoodConfigCollector.of(ConfigUtils.location(Tags.Items.FOODS_RAW_MEAT), 6, 4),
//            FoodConfigCollector.of(ConfigUtils.location(Tags.Items.FOODS_BERRY), 2, 1),
//            FoodConfigCollector.of(ConfigUtils.location(Tags.Items.MUSHROOMS), 1, 1),
//            FoodConfigCollector.of(ConfigUtils.location(Items.ROTTEN_FLESH), 2, 4),
//            FoodConfigCollector.of(ConfigUtils.location(Items.SPIDER_EYE), 6, 8),
//            FoodConfigCollector.of(ConfigUtils.location(Items.RABBIT), 7, 8),
//            FoodConfigCollector.of(ConfigUtils.location(Items.POISONOUS_POTATO), 7, 8),
//            FoodConfigCollector.of(ConfigUtils.location(Items.CHORUS_FRUIT), 9, 8),
//            FoodConfigCollector.of(ConfigUtils.location(Items.BROWN_MUSHROOM), 2, 1),
//            FoodConfigCollector.of(ConfigUtils.location(Items.RED_MUSHROOM), 2, 3),
//            FoodConfigCollector.of(ConfigUtils.location(Items.HONEY_BOTTLE), 6, 1),
//            FoodConfigCollector.of(ConfigUtils.location(Items.WARPED_FUNGUS), 3, 3),
//            FoodConfigCollector.of(ConfigUtils.location(Items.CRIMSON_FUNGUS), 3, 3),
//            FoodConfigCollector.of(ConfigUtils.location(DSItems.FOREST_DRAGON_TREAT), 4, 8),
//            FoodConfigCollector.of(ConfigUtils.location(DSItems.MEAT_CHORUS_MIX), 12, 8),
//            FoodConfigCollector.of(ConfigUtils.location(DSItems.MEAT_WILD_BERRIES), 12, 10),
//            FoodConfigCollector.of(ConfigUtils.location(DSItems.SMELLY_MEAT_PORRIDGE), 6, 10),
//            FoodConfigCollector.of(ConfigUtils.location(DSItems.DIAMOND_CHORUS), 15, 12),
//            FoodConfigCollector.of(ConfigUtils.location(DSItems.LUMINOUS_OINTMENT), 5, 3),
//            FoodConfigCollector.of(ConfigUtils.location(DSItems.SWEET_SOUR_RABBIT), 10, 6),
//            FoodConfigCollector.of("additionaldragons:cursed_marrow"),
//            FoodConfigCollector.of("aquaculture:turtle_soup", 8, 8),
//            FoodConfigCollector.of("netherdepthsupgrade:wither_bonefish", 4, 6),
//            FoodConfigCollector.of("netherdepthsupgrade:bonefish", 4, 6),
//            FoodConfigCollector.of("phantasm:chorus_fruit_salad", 10, 10),
//            FoodConfigCollector.of("aoa3:fiery_chops", 6, 7),
//            FoodConfigCollector.of("aoa3:raw_chimera_chop", 6, 7),
//            FoodConfigCollector.of("aoa3:raw_furlion_chop", 6, 7),
//            FoodConfigCollector.of("aoa3:raw_halycon_beef", 7, 8),
//            FoodConfigCollector.of("aoa3:raw_charger_shank", 6, 7),
//            FoodConfigCollector.of("aoa3:trilliad_leaves", 8, 11),
//            // TODO :: check for old, clean stuff up
//            FoodConfigCollector.of("pamhc2foodextended:rawtofabbititem"),
//            FoodConfigCollector.of("pamhc2foodextended:rawtofickenitem"),
//            FoodConfigCollector.of("quark:golden_frog_leg:12:14"),
//            FoodConfigCollector.of("pamhc2foodextended:rawtofuttonitem"),
//            FoodConfigCollector.of("alexsmobs:kangaroo_meat:5:6"),
//            FoodConfigCollector.of("alexsmobs:moose_ribs:6:8"),
//            FoodConfigCollector.of("simplefarming:raw_horse_meat:5:6"),
//            FoodConfigCollector.of("simplefarming:raw_bacon:3:3"),
//            FoodConfigCollector.of("simplefarming:raw_chicken_wings:2:3"),
//            FoodConfigCollector.of("simplefarming:raw_sausage:3:4"),
//            FoodConfigCollector.of("xenoclustwo:raw_tortice:7:8"),
//            FoodConfigCollector.of("unnamedanimalmod:musk_ox_shank:7:8"),
//            FoodConfigCollector.of("unnamedanimalmod:frog_legs:5:6"),
//            FoodConfigCollector.of("unnamedanimalmod:mangrove_fruit:4:7"),
//            FoodConfigCollector.of("betteranimalsplus:venisonraw:7:6"),
//            FoodConfigCollector.of("born_in_chaos_v1:corpse_maggot:1:1"),
//            FoodConfigCollector.of("born_in_chaos_v1:monster_flesh:1:1"),
//            FoodConfigCollector.of("betteranimalsplus:pheasantraw:7:5"),
//            FoodConfigCollector.of("betteranimalsplus:turkey_leg_raw:4:5"),
//            FoodConfigCollector.of("infernalexp:raw_hogchop:6:7"),
//            FoodConfigCollector.of("infernalexp:cured_jerky:10:7"),
//            FoodConfigCollector.of("rats:raw_rat:4:5"),
//            FoodConfigCollector.of("aquaculture:frog:4:5"),
//            FoodConfigCollector.of("aquaculture:frog_legs_raw:4:4"),
//            FoodConfigCollector.of("aquaculture:box_turtle:4:5"),
//            FoodConfigCollector.of("aquaculture:arrau_turtle:4:5"),
//            FoodConfigCollector.of("aquaculture:starshell_turtle:4:5"),
//            FoodConfigCollector.of("undergarden:raw_gloomper_leg:4:5"),
//            FoodConfigCollector.of("undergarden:raw_dweller_meat:6:7"),
//            FoodConfigCollector.of("farmersdelight:chicken_cuts:3:3"),
//            FoodConfigCollector.of("farmersdelight:bacon:3:3"),
//            FoodConfigCollector.of("farmersdelight:ham:9:10"),
//            FoodConfigCollector.of("farmersdelight:minced_beef:5:3"),
//            FoodConfigCollector.of("farmersdelight:mutton_chops:5:3"),
//            FoodConfigCollector.of("abnormals_delight:duck_fillet:2:3"),
//            FoodConfigCollector.of("abnormals_delight:venison_shanks:7:3"),
//            FoodConfigCollector.of("autumnity:foul_berries:2:4"),
//            FoodConfigCollector.of("autumnity:turkey:7:8"),
//            FoodConfigCollector.of("autumnity:turkey_piece:2:4"),
//            FoodConfigCollector.of("autumnity:foul_soup:12:8"),
//            FoodConfigCollector.of("endergetic:bolloom_fruit:3:4"),
//            FoodConfigCollector.of("quark:frog_leg:4:5"),
//            FoodConfigCollector.of("nethers_delight:hoglin_loin:8:6"),
//            FoodConfigCollector.of("nethers_delight:raw_stuffed_hoglin:18:10"),
//            FoodConfigCollector.of("xreliquary:zombie_heart:4:7"),
//            FoodConfigCollector.of("xreliquary:bat_wing:2:2"),
//            FoodConfigCollector.of("eidolon:zombie_heart:7:7"),
//            FoodConfigCollector.of("forbidden_arcanus:bat_wing:5:2"),
//            FoodConfigCollector.of("twilightforest:raw_venison:7:7"),
//            FoodConfigCollector.of("twilightforest:raw_meef:9:5"),
//            FoodConfigCollector.of("twilightforest:hydra_chop"),
//            FoodConfigCollector.of("cyclic:chorus_flight"),
//            FoodConfigCollector.of("cyclic:chorus_spectral"),
//            FoodConfigCollector.of("cyclic:toxic_carrot:15:15"),
//            FoodConfigCollector.of("artifacts:everlasting_beef"),
//            FoodConfigCollector.of("byg:soul_shroom:9:5"),
//            FoodConfigCollector.of("byg:death_cap:9:8"),
//            FoodConfigCollector.of("minecolonies:chorus_bread"),
//            FoodConfigCollector.of("wyrmroost:raw_lowtier_meat:3:2"),
//            FoodConfigCollector.of("wyrmroost:raw_common_meat:5:3"),
//            FoodConfigCollector.of("wyrmroost:raw_apex_meat:8:6"),
//            FoodConfigCollector.of("wyrmroost:raw_behemoth_meat:11:12"),
//            FoodConfigCollector.of("wyrmroost:desert_wyrm:4:3"),
//            FoodConfigCollector.of("eanimod:rawchicken_darkbig:9:5"),
//            FoodConfigCollector.of("eanimod:rawchicken_dark:5:4"),
//            FoodConfigCollector.of("eanimod:rawchicken_darksmall:3:2"),
//            FoodConfigCollector.of("eanimod:rawchicken_pale:5:3"),
//            FoodConfigCollector.of("eanimod:rawchicken_palesmall:4:3"),
//            FoodConfigCollector.of("eanimod:rawrabbit_small:4:4"),
//            FoodConfigCollector.of("environmental:duck:4:3"),
//            FoodConfigCollector.of("environmental:venison:7:7"),
//            FoodConfigCollector.of("cnb:lizard_item_jungle:4:4"),
//            FoodConfigCollector.of("cnb:lizard_item_mushroom:4:4"),
//            FoodConfigCollector.of("cnb:lizard_item_jungle_2:4:4"),
//            FoodConfigCollector.of("cnb:lizard_item_desert_2:4:4"),
//            FoodConfigCollector.of("cnb:lizard_egg:5:2"),
//            FoodConfigCollector.of("cnb:lizard_item_desert:4:4"),
//            FoodConfigCollector.of("snowpig:frozen_porkchop:7:3"),
//            FoodConfigCollector.of("snowpig:frozen_ham:5:7"),
//            FoodConfigCollector.of("naturalist:venison:7:6"),
//            FoodConfigCollector.of("leescreatures:raw_boarlin:6:6"),
//            FoodConfigCollector.of("mysticalworld:venison:5:5"),
//            FoodConfigCollector.of("toadterror:toad_chops:8:7"),
//            FoodConfigCollector.of("prehistoricfauna:raw_large_thyreophoran_meat:7:6"),
//            FoodConfigCollector.of("prehistoricfauna:raw_large_marginocephalian_meat:8:6"),
//            FoodConfigCollector.of("prehistoricfauna:raw_small_ornithischian_meat:4:3"),
//            FoodConfigCollector.of("prehistoricfauna:raw_large_sauropod_meat:11:9"),
//            FoodConfigCollector.of("prehistoricfauna:raw_small_sauropod_meat:4:4"),
//            FoodConfigCollector.of("prehistoricfauna:raw_large_theropod_meat:7:7"),
//            FoodConfigCollector.of("prehistoricfauna:raw_small_theropod_meat:4:4"),
//            FoodConfigCollector.of("prehistoricfauna:raw_small_archosauromorph_meat:3:3"),
//            FoodConfigCollector.of("prehistoricfauna:raw_large_archosauromorph_meat:6:5"),
//            FoodConfigCollector.of("prehistoricfauna:raw_small_reptile_meat:4:3"),
//            FoodConfigCollector.of("prehistoricfauna:raw_large_synapsid_meat:5:6"),
//            FoodConfigCollector.of("ends_delight:dragon_leg:15:15"),
//            FoodConfigCollector.of("ends_delight:raw_dragon_meat:10:10"),
//            FoodConfigCollector.of("ends_delight:raw_dragon_meat_cuts:5:2"),
//            FoodConfigCollector.of("ends_delight:dragon_breath_and_chorus_soup:15:15"),
//            FoodConfigCollector.of("ends_delight:ender_sauce:8:15"),
//            FoodConfigCollector.of("ends_delight:raw_ender_mite_meat:1:1"),
//            FoodConfigCollector.of("ends_delight:non_hatchable_dragon_egg:8:5"),
//            FoodConfigCollector.of("ends_delight:shulker_meat:7:4"),
//            FoodConfigCollector.of("unusualend:chorus_juice:2:2"),
//            FoodConfigCollector.of("ends_delight:liquid_dragon_egg:3:3"),
//            FoodConfigCollector.of("ends_delight:shulker_meat_slice:4:1"),
//            FoodConfigCollector.of("unusualend:ender_firefly_egg:2:3"),
//            FoodConfigCollector.of("unusualend:chorus_petal:1:1"),
//            FoodConfigCollector.of("unusualend:chorus_pie:3:4"),
//            FoodConfigCollector.of("unusualend:ender_stew:6:1"),
//            FoodConfigCollector.of("unusualprehistory:kentro_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:hwacha_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:ulugh_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:antarcto_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:austro_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:pachy_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:raptor_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:trike_eggs:4:4"),
//            FoodConfigCollector.of("unusualprehistory:rex_eggs:6:3"),
//            FoodConfigCollector.of("unusualprehistory:coty_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:meaty_buffet:12:15"),
//            FoodConfigCollector.of("unusualprehistory:majunga_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:anuro_eggs:3:3"),
//            FoodConfigCollector.of("unusualprehistory:raw_austro:6:3"),
//            FoodConfigCollector.of("unusualprehistory:raw_coty:5:3"),
//            FoodConfigCollector.of("nourished_nether:raw_hoglin:8:8"),
//            FoodConfigCollector.of("gothic:meat:7:8"),
//            FoodConfigCollector.of("gothic:bug_meat:2:2"),
//            FoodConfigCollector.of("gothic:scavenger_egg:3:3"),
//            FoodConfigCollector.of("gothic:snapperweed:1:1"),
//            FoodConfigCollector.of("gothic:blue_elder:1:1"),
//            FoodConfigCollector.of("unusualend:wandering_stew:1:1"),
//            FoodConfigCollector.of("nethersdelight:raw_stuffed_hoglin:15:15"),
//            FoodConfigCollector.of("nethersdelight:hoglin_ear:1:1"),
//            FoodConfigCollector.of("nethersdelight:ground_strider:6:2"),
//            FoodConfigCollector.of("nethersdelight:strider_slice:8:8"),
//            FoodConfigCollector.of("nethersdelight:hoglin_ear:1:1"),
//            FoodConfigCollector.of("nethersdelight:hoglin_loin:6:6"),
//            FoodConfigCollector.of("nethersdelight:propelpearl:1:1"),
//            FoodConfigCollector.of("orcz:squig_eye:6:6"),
//            FoodConfigCollector.of("orcz:orceye:4:4"),
//            FoodConfigCollector.of("goated:chevon:6:6"),
//            FoodConfigCollector.of("rottencreatures:frozen_rotten_flesh:4:4"),
//            FoodConfigCollector.of("regions_unexplored:tall_green_bioshroom:3:3"),
//            FoodConfigCollector.of("regions_unexplored:green_bioshroom:3:3"),
//            FoodConfigCollector.of("regions_unexplored:blue_bioshroom:3:3"),
//            FoodConfigCollector.of("regions_unexplored:tall_blue_bioshroom:3:3"),
//            FoodConfigCollector.of("regions_unexplored:tall_pink_bioshroom:3:3"),
//            FoodConfigCollector.of("regions_unexplored:pink_bioshroom:3:3"),
//            FoodConfigCollector.of("regions_unexplored:yellow_bioshroom:3:3"),
//            FoodConfigCollector.of("regions_unexplored:tall_yellow_bioshroom:3:3"),
//            FoodConfigCollector.of("quark:glow_shroom:3:3"),
//            FoodConfigCollector.of("gothic:cave_mushrooms:3:3"),
//            FoodConfigCollector.of("gothic:tall_mushrooms:3:3"),
//            FoodConfigCollector.of("gothic:miners_food_wild:3:3"),
//            FoodConfigCollector.of("gothic:black_mushroom:3:3"),
//            FoodConfigCollector.of("phantasm:putac_shroom:3:3"),
//            FoodConfigCollector.of("farmersdelight:red_mushroom_colony:10:3"),
//            FoodConfigCollector.of("farmersdelight:brown_mushroom_colony:10:3"),
//            FoodConfigCollector.of("gothic:miners_food:3:3"),
//            FoodConfigCollector.of("gothic:black_mushroom_item:3:3"),
//            FoodConfigCollector.of("gothic:blue_elder:3:3"),
//            FoodConfigCollector.of("frozen_delight:truffle_slice:4:8"),
//            FoodConfigCollector.of("frozenup:truffle:8:4"),
//            FoodConfigCollector.of("frozen_delight:mushroom_mix:16:3"),
//            FoodConfigCollector.of("nourished_nether:nether_fungus_stew:7:3"),
//            FoodConfigCollector.of("nethersdelight:warped_fungus_colony:10:3"),
//            FoodConfigCollector.of("nethersdelight:crimson_fungus_colony:10:3"),
//            FoodConfigCollector.of("unusualend:chorus_fungus:3:3"),
//            FoodConfigCollector.of("orcz:orcshroom:3:3"),
//            FoodConfigCollector.of("orcz:blue_orcshroom:3:3"),
//            FoodConfigCollector.of("waterstrainer:worm:1:1"),
//            FoodConfigCollector.of("undergarden:veil_mushroom:1:1"),
//            FoodConfigCollector.of("undergarden:indigo_mushroom:1:1"),
//            FoodConfigCollector.of("undergarden:smogstem_sapling:1:1"),
//            FoodConfigCollector.of("undergarden:blood_mushroom:1:1"),
//            FoodConfigCollector.of("undergarden:ink_mushroom:1:1"),
//            FoodConfigCollector.of("undergarden:blisterberry:1:1"),
//            FoodConfigCollector.of("undergarden:rotten_blisterberry:1:1"),
//            FoodConfigCollector.of("undergarden:ink_mushroom:1:1"),
//            FoodConfigCollector.of("undergarden:bloody_stew:1:1"),
//            FoodConfigCollector.of("undergarden:inky_stew:1:1"),
//            FoodConfigCollector.of("undergarden:veiled_stew:1:1"),
//            FoodConfigCollector.of("undergarden:indigo_stew:1:1"),
//            FoodConfigCollector.of("butchersdelight:deadchiken:6:4"),
//            FoodConfigCollector.of("butchersdelight:deadstrider:6:4"),
//            FoodConfigCollector.of("butchersdelight:deadrabbitbrown:6:4"),
//            FoodConfigCollector.of("butchersdelight:deadllama:6:4"),
//            FoodConfigCollector.of("butchersdelight:deadhoglin:6:4"),
//            FoodConfigCollector.of("butchersdelight:deadgoat:6:4"),
//            FoodConfigCollector.of("butchersdelight:deadsheep:6:4"),
//            FoodConfigCollector.of("butchersdelight:deadpig:6:4"),
//            FoodConfigCollector.of("butchersdelight:dead_cow:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:llama_loin:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:llamma_ribs:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:llama_leg:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:goat_loin:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:goatrack:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:goat_shank:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:porkribs:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:porkloin:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:ham:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:sheeploin:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:sheeprack:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:sheepshank:6:4"),
//            FoodConfigCollector.of("butchersdelightfoods:beeftenderloin:6:4")
//    );
//}
