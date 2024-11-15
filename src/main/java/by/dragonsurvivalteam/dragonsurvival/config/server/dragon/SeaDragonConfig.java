package by.dragonsurvivalteam.dragonsurvival.config.server.dragon;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.config.obj.Validation;
import by.dragonsurvivalteam.dragonsurvival.config.types.BlockStateConfig;
import by.dragonsurvivalteam.dragonsurvival.config.types.FoodConfigCollector;
import by.dragonsurvivalteam.dragonsurvival.config.types.ItemHurtConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;

import java.util.Arrays;
import java.util.List;

public class SeaDragonConfig {
    // --- Magic --- //

    @Translation(key = "sea_abilities", type = Translation.Type.CONFIGURATION, comments = "Enable / Disable sea dragon abilities")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic"}, key = "sea_abilities")
    public static Boolean seaDragonAbilities = true;

    @Translation(key = "sea_swimming_bonus", type = Translation.Type.CONFIGURATION, comments = "If enabled sea dragon gain a water swim speed bonus and will not drown in water")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "bonuses"}, key = "sea_swimming_bonus")
    public static Boolean seaSwimmingBonuses = true;

    @Translation(key = "sea_conditional_mana_blocks", type = Translation.Type.CONFIGURATION, comments = "Blocks that restore mana for sea dragons when under certain conditions (block states) - Formatting: namespace:path:key=value,key=value (prefix namespace with # for tags)")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic"}, key = "sea_conditional_mana_blocks")
    public static List<BlockStateConfig> seaConditionalManaBlocks = List.of();

    // --- Penalties --- //

    @ConfigRange(min = 0, max = 100_000)
    @Translation(key = "sea_ticks_without_water", type = Translation.Type.CONFIGURATION, comments = "Amount of ticks (20 ticks = 1 second) a sea dragon can be out of water before taking dehydration damage - disabled if set to 0")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "penalties"}, key = "sea_ticks_without_water")
    public static Integer seaTicksWithoutWater = Functions.secondsToTicks(50);

    @Translation(key = "sea_ticks_based_on_temperature", type = Translation.Type.CONFIGURATION, comments = "If enabled the sea dragon will lose more water in hot biomes and less during the night")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "penalties"}, key = "sea_ticks_based_on_temperature")
    public static Boolean seaTicksBasedOnTemperature = true;

    @ConfigRange(min = 0.5, max = 100.0)
    @Translation(key = "sea_hydration_damage", type = Translation.Type.CONFIGURATION, comments = "The amount of damage taken every 40 ticks (2 seconds) if dehydrated, or 20 ticks (1 second) if fully dehydrated")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "penalties"}, key = "sea_hydration_damage")
    public static Double seaDehydrationDamage = 1.0;

    @Translation(key = "sea_allow_water_bottles", type = Translation.Type.CONFIGURATION, comments = "Using water bottles will restore hydration if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "penalties"}, key = "sea_allow_water_bottles")
    public static Boolean seaAllowWaterBottles = true;

    @ConfigRange(min = 0, max = 100_000)
    @Translation(key = "sea_ticks_without_water_restored", type = Translation.Type.CONFIGURATION, comments = "How many ticks (20 ticks = 1 second) hydration items restore when used - disabled if set to 0")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "penalties"}, key = "sea_ticks_without_water_restored")
    public static Integer seaTicksWithoutWaterRestored = Functions.secondsToTicks(250);

    // --- Food --- //

    @Translation(key = "sea_hurtful_items", type = Translation.Type.CONFIGURATION, comments = "Items which will cause damage to sea dragons when consumed - Formatting: namespace:path:damage (prefix namespace with # for tags)")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "food"}, key = "sea_hurtful_items", validation = Validation.RESOURCE_LOCATION_NUMBER)
    public static List<ItemHurtConfig> seaDragonHurtfulItems = List.of();

    @Translation(key = "sea_foods", type = Translation.Type.CONFIGURATION, comments = {
            "Determines which items a sea dragon can eat - the item doesn't need to be a food item (e.g. you could add an iron block here",
            "Formatting: namespace:path:nutrition:saturation (prefix namespace with # for tags)",
            "Nutrition and saturation (can be specified in decimals) are optional - if they're missing the items original values will be used (or 1:0)"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "food"}, key = "sea_foods")
    public static List<FoodConfigCollector> seaDragonFoods = Arrays.asList(
            // TODO :: check for old, clean stuff up
            FoodConfigCollector.of("c:foods/raw_fish:6:4"),
            FoodConfigCollector.of("minecraft:kelp:1:1"),
            FoodConfigCollector.of("minecraft:pufferfish:8:8"),
            FoodConfigCollector.of("dragonsurvival:sea_dragon_treat:4:8"),
            FoodConfigCollector.of("dragonsurvival:seasoned_fish:12:10"),
            FoodConfigCollector.of("dragonsurvival:golden_coral_pufferfish:12:14"),
            FoodConfigCollector.of("dragonsurvival:frozen_raw_fish:2:1"),
            FoodConfigCollector.of("dragonsurvival:golden_turtle_egg:15:12"),
            FoodConfigCollector.of("additionaldragons:slippery_sushi:10:8"),
            FoodConfigCollector.of("aoa3:raw_candlefish:9:9"),
            FoodConfigCollector.of("aoa3:raw_crimson_skipper:8:8"),
            FoodConfigCollector.of("aoa3:raw_fingerfish:4:4"),
            FoodConfigCollector.of("aoa3:raw_pearl_stripefish:5:4"),
            FoodConfigCollector.of("aoa3:raw_limefish:5:5"),
            FoodConfigCollector.of("aoa3:raw_sailback:6:5"),
            FoodConfigCollector.of("netherdepthsupgrade:soulsucker:6:7"),
            FoodConfigCollector.of("netherdepthsupgrade:obsidianfish:6:7"),
            FoodConfigCollector.of("netherdepthsupgrade:lava_pufferfish:8:7"),
            FoodConfigCollector.of("netherdepthsupgrade:searing_cod:6:7"),
            FoodConfigCollector.of("netherdepthsupgrade:glowdine:6:7"),
            FoodConfigCollector.of("netherdepthsupgrade:warped_kelp:2:2"),
            FoodConfigCollector.of("netherdepthsupgrade:lava_pufferfish_slice:2:2"),
            FoodConfigCollector.of("netherdepthsupgrade:glowdine_slice:2:2"),
            FoodConfigCollector.of("netherdepthsupgrade:soulsucker_slice:2:2"),
            FoodConfigCollector.of("netherdepthsupgrade:obsidianfish_slice:2:2"),
            FoodConfigCollector.of("netherdepthsupgrade:searing_cod_slice:2:2"),
            FoodConfigCollector.of("crittersandcompanions:clam:10:3"),
            FoodConfigCollector.of("aoa3:raw_golden_gullfish:10:2"),
            FoodConfigCollector.of("aoa3:raw_turquoise_stripefish:7:6"),
            FoodConfigCollector.of("aoa3:raw_violet_skipper:7:7"),
            FoodConfigCollector.of("aoa3:raw_rocketfish:4:10"),
            FoodConfigCollector.of("aoa3:raw_crimson_stripefish:8:7"),
            FoodConfigCollector.of("aoa3:raw_sapphire_strider:9:8"),
            FoodConfigCollector.of("aoa3:raw_dark_hatchetfish:9:9"),
            FoodConfigCollector.of("aoa3:raw_ironback:10:9"),
            FoodConfigCollector.of("aoa3:raw_rainbowfish:11:11"),
            FoodConfigCollector.of("aoa3:raw_razorfish:12:14"),
            FoodConfigCollector.of("alexsmobs:lobster_tail:4:5"),
            FoodConfigCollector.of("alexsmobs:blobfish:8:9"),
            FoodConfigCollector.of("oddwatermobs:raw_ghost_shark:8:8"),
            FoodConfigCollector.of("oddwatermobs:raw_isopod:4:2"),
            FoodConfigCollector.of("oddwatermobs:raw_mudskipper:6:7"),
            FoodConfigCollector.of("oddwatermobs:raw_coelacanth:9:10"),
            FoodConfigCollector.of("oddwatermobs:raw_anglerfish:6:6"),
            FoodConfigCollector.of("oddwatermobs:deep_sea_fish:4:2"),
            FoodConfigCollector.of("oddwatermobs:crab_leg:5:6"),
            FoodConfigCollector.of("simplefarming:raw_calamari:5:6"),
            FoodConfigCollector.of("unnamedanimalmod:elephantnose_fish:5:6"),
            FoodConfigCollector.of("unnamedanimalmod:flashlight_fish:5:6"),
            FoodConfigCollector.of("born_in_chaos_v1:sea_terror_eye:10:4"),
            FoodConfigCollector.of("born_in_chaos_v1:rotten_fish:4:2"),
            FoodConfigCollector.of("unnamedanimalmod:rocket_killifish:5:6"),
            FoodConfigCollector.of("unnamedanimalmod:leafy_seadragon:5:6"),
            FoodConfigCollector.of("unnamedanimalmod:elephantnose_fish:5:6"),
            FoodConfigCollector.of("betteranimalsplus:eel_meat_raw:5:6"),
            FoodConfigCollector.of("betteranimalsplus:calamari_raw:4:5"),
            FoodConfigCollector.of("betteranimalsplus:crab_meat_raw:4:4"),
            FoodConfigCollector.of("aquaculture:fish_fillet_raw:2:2"),
            FoodConfigCollector.of("aquaculture:goldfish:8:4"),
            FoodConfigCollector.of("aquaculture:algae:3:2"),
            FoodConfigCollector.of("betterendforge:end_fish_raw:6:7"),
            FoodConfigCollector.of("betterendforge:hydralux_petal:3:3"),
            FoodConfigCollector.of("betterendforge:charnia_green:2:2"),
            FoodConfigCollector.of("shroomed:raw_shroomfin:5:6"),
            FoodConfigCollector.of("undergarden:raw_gwibling:5:6"),
            FoodConfigCollector.of("bettas:betta_fish:4:5"),
            FoodConfigCollector.of("quark:crab_leg:4:4"),
            FoodConfigCollector.of("pamhc2foodextended:rawtofishitem"),
            FoodConfigCollector.of("fins:banded_redback_shrimp:6:1"),
            FoodConfigCollector.of("fins:night_light_squid:6:2"),
            FoodConfigCollector.of("fins:night_light_squid_tentacle:6:2"),
            FoodConfigCollector.of("fins:emerald_spindly_gem_crab:7:2"),
            FoodConfigCollector.of("fins:amber_spindly_gem_crab:7:2"),
            FoodConfigCollector.of("fins:rubby_spindly_gem_crab:7:2"),
            FoodConfigCollector.of("fins:sapphire_spindly_gem_crab:7:2"),
            FoodConfigCollector.of("fins:pearl_spindly_gem_crab:7:2"),
            FoodConfigCollector.of("fins:papa_wee:6:2"),
            FoodConfigCollector.of("fins:bugmeat:4:2"),
            FoodConfigCollector.of("fins:raw_golden_river_ray_wing:6:2"),
            FoodConfigCollector.of("fins:red_bull_crab_claw:4:4"),
            FoodConfigCollector.of("fins:white_bull_crab_claw:4:4"),
            FoodConfigCollector.of("fins:wherble_fin:1:1"),
            FoodConfigCollector.of("forbidden_arcanus:tentacle:5:2"),
            FoodConfigCollector.of("pneumaticcraft:raw_salmon_tempura:6:10"),
            FoodConfigCollector.of("rats:ratfish:4:2"),
            FoodConfigCollector.of("upgrade_aquatic:purple_pickerelweed:2:2"),
            FoodConfigCollector.of("upgrade_aquatic:blue_pickerelweed:2:2"),
            FoodConfigCollector.of("upgrade_aquatic:polar_kelp:2:2"),
            FoodConfigCollector.of("upgrade_aquatic:tongue_kelp:2:2"),
            FoodConfigCollector.of("upgrade_aquatic:thorny_kelp:2:2"),
            FoodConfigCollector.of("upgrade_aquatic:ochre_kelp:2:2"),
            FoodConfigCollector.of("upgrade_aquatic:lionfish:8:9"),
            FoodConfigCollector.of("aquaculture:sushi:6:5"),
            FoodConfigCollector.of("freshwarriors:fresh_soup:15:10"),
            FoodConfigCollector.of("freshwarriors:beluga_caviar:10:3"),
            FoodConfigCollector.of("freshwarriors:piranha:4:1"),
            FoodConfigCollector.of("freshwarriors:tilapia:4:1"),
            FoodConfigCollector.of("freshwarriors:stuffed_piranha:4:1"),
            FoodConfigCollector.of("freshwarriors:tigerfish:5:5"),
            FoodConfigCollector.of("freshwarriors:toe_biter_leg:3:3"),
            FoodConfigCollector.of("mysticalworld:raw_squid:6:5"),
            FoodConfigCollector.of("aquafina:fresh_soup:10:10"),
            FoodConfigCollector.of("aquafina:beluga_caviar:10:3"),
            FoodConfigCollector.of("aquafina:raw_piranha:4:1"),
            FoodConfigCollector.of("aquafina:raw_tilapia:4:1"),
            FoodConfigCollector.of("aquafina:stuffed_piranha:4:1"),
            FoodConfigCollector.of("aquafina:tigerfish:5:5"),
            FoodConfigCollector.of("aquafina:toe_biter_leg:3:3"),
            FoodConfigCollector.of("aquafina:raw_angelfish:4:1"),
            FoodConfigCollector.of("aquafina:raw_football_fish:4:1"),
            FoodConfigCollector.of("aquafina:raw_foxface_fish:4:1"),
            FoodConfigCollector.of("aquafina:raw_royal_gramma:4:1"),
            FoodConfigCollector.of("aquafina:raw_starfish:4:1"),
            FoodConfigCollector.of("aquafina:spider_crab_leg:4:1"),
            FoodConfigCollector.of("aquafina:raw_stingray_slice:4:1"),
            FoodConfigCollector.of("prehistoricfauna:raw_ceratodus:5:5"),
            FoodConfigCollector.of("prehistoricfauna:raw_cyclurus:4:4"),
            FoodConfigCollector.of("prehistoricfauna:raw_potamoceratodus:5:5"),
            FoodConfigCollector.of("prehistoricfauna:raw_myledaphus:4:4"),
            FoodConfigCollector.of("prehistoricfauna:raw_gar:4:4"),
            FoodConfigCollector.of("prehistoricfauna:raw_oyster:4:3"),
            FoodConfigCollector.of("prehistoric_delight:prehistoric_fillet:3:3"),
            FoodConfigCollector.of("seadwellers:rainbow_trout:10:10"),
            FoodConfigCollector.of("crittersandcompanions:koi_fish:5:5"),
            FoodConfigCollector.of("aquamirae:elodea:3:3"),
            FoodConfigCollector.of("croptopia:clam:3:3"),
            FoodConfigCollector.of("croptopia:calamari:2:3"),
            FoodConfigCollector.of("croptopia:anchovy:3:2"),
            FoodConfigCollector.of("croptopia:crab:6:8"),
            FoodConfigCollector.of("croptopia:glowing_calamari:4:5"),
            FoodConfigCollector.of("croptopia:oyster:2:4"),
            FoodConfigCollector.of("croptopia:roe:1:2"),
            FoodConfigCollector.of("croptopia:shrimp:2:2"),
            FoodConfigCollector.of("croptopia:tuna:6:4"),
            FoodConfigCollector.of("aquamirae:spinefish:4:4"),
            FoodConfigCollector.of("alexsmobs:flying_fish:6:4"),
            FoodConfigCollector.of("netherdepthsupgrade:eyeball:3:3"),
            FoodConfigCollector.of("netherdepthsupgrade:eyeball_fish:3:3"),
            FoodConfigCollector.of("oceansdelight:guardian:4:3"),
            FoodConfigCollector.of("oceansdelight:guardian_tail:1:3"),
            FoodConfigCollector.of("oceansdelight:cut_tentacles:3:1"),
            FoodConfigCollector.of("oceansdelight:tentacles:3:4"),
            FoodConfigCollector.of("oceansdelight:tentacle_on_a_stick:3:4"),
            FoodConfigCollector.of("oceansdelight:fugu_slice:5:4"),
            FoodConfigCollector.of("oceansdelight:elder_guardian_slice:8:6"),
            FoodConfigCollector.of("oceansdelight:elder_guardian_slab:15:15"),
            FoodConfigCollector.of("upgrade_aquatic:elder_eye:15:15"),
            FoodConfigCollector.of("unusualprehistory:golden_scau:15:15"),
            FoodConfigCollector.of("unusualprehistory:raw_scau:4:3"),
            FoodConfigCollector.of("unusualprehistory:raw_stetha:4:3"),
            FoodConfigCollector.of("unusualprehistory:stetha_eggs:4:3"),
            FoodConfigCollector.of("unusualprehistory:beelze_eggs:4:3"),
            FoodConfigCollector.of("unusualprehistory:scau_eggs:4:3"),
            FoodConfigCollector.of("unusualprehistory:ammon_eggs:4:3"),
            FoodConfigCollector.of("unusualprehistory:dunk_eggs:4:3"),
            FoodConfigCollector.of("netherdepthsupgrade:crimson_seagrass:2:2"),
            FoodConfigCollector.of("netherdepthsupgrade:crimson_kelp:2:2"),
            FoodConfigCollector.of("netherdepthsupgrade:warped_seagrass:2:2"),
            FoodConfigCollector.of("undergarden:glitterkelp:2:2"),
            FoodConfigCollector.of("enlightened_end:raw_stalker:10:4")
    );
}
