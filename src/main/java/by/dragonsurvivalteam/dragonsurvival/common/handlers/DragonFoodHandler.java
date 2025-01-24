package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ToolTipHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigType;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DragonFoodHandler {
	// Food general
	@ConfigOption(side = ConfigSide.SERVER, category = "food", key = "requireDragonFood", comment = "Force dragons to eat a unique diet for their type.")
	public static Boolean requireDragonFood = true;

	@ConfigOption(side = ConfigSide.SERVER, category = "food", key = "disable_dragon_food_handling", comment = "Disables the custom dragon food handling.")
	public static boolean disableDragonFoodHandling;

	// Dragon Food List
	@ConfigType(Item.class)
	@ConfigOption(side = ConfigSide.SERVER, category = {"food", "cave_dragon"}, key = "caveDragon", comment = {"Dragon food formatting: mod_id:item_id:nutrition:saturation", "Nutrition / saturation values are optional as the human values will be used if missing.", "Saturation can be defined with decimals (e.g. 0.3)"})
	public static List<String> caveDragonFoods = Arrays.asList("minecraft:coals:1:1", "minecraft:charcoal:1:2", "dragonsurvival:charged_coal:6:1", "dragonsurvival:charred_meat:8:10", "dragonsurvival:cave_dragon_treat:4:8", "dragonsurvival:charred_seafood:7:11", "dragonsurvival:charred_vegetable:8:9", "dragonsurvival:charred_mushroom:9:9", "dragonsurvival:charged_soup:15:15", "dragonsurvival:hot_dragon_rod:4:15", "dragonsurvival:explosive_copper:6:4", "dragonsurvival:double_quartz:8:6", "dragonsurvival:quartz_explosive_copper:12:18", "netherdepthsupgrade:blazefish:6:7", "netherdepthsupgrade:cooked_magmacubefish_slice:2:2", "netherdepthsupgrade:blazefish_slice:2:2", "infernalexp:molten_gold_cluster:2:6", "netherdepthsupgrade:magmacubefish:6:7", "desolation:cinder_fruit:6:7", "desolation:powered_cinder_fruit:8:12", "desolation:activatedcharcoal:2:2", "desolation:infused_powder:10:10", "desolation:primed_ash:7:8", "undergarden:ditchbulb:5:6", "xreliquary:molten_core:1:1", "silents_mechanisms:coal_generator_fuels:1:1", "mekanism:dust_charcoal:1:1", "mekanism:dust_coal:1:1", "rats:nether_cheese", "potionsmaster:charcoal_powder:1:1", "potionsmaster:coal_powder:1:1", "potionsmaster:activated_charcoal:2:2", "thermal:coal_coke:1:1", "infernalexp:glowcoal:2:3", "resourcefulbees:coal_honeycomb:5:5", "resourcefulbees:netherite_honeycomb:5:5", "lazierae2:coal_dust:1:1", "silents_mechanisms:coal_dust:1:1", "potionsmaster:calcinatedcoal_powder:1:1", "thermal:basalz_rod:2:4", "thermal:basalz_powder:1:2", "druidcraft:fiery_glass:2:2", "create:blaze_cake:10:10", "create:creative_blaze_cake:50:50", "upgradednetherite:fire_essence:12:4", "netherdepthsupgrade:fortress_grouper:3:1", "nethersdelight:nether_skewer:6:8");

	@ConfigType(Item.class)
	@ConfigOption(side = ConfigSide.SERVER, category = {"food", "forest_dragon"}, key = "forestDragon", comment = {"Dragon food formatting: mod_id:item_id:nutrition:saturation", "Nutrition / saturation values are optional as the human values will be used if missing.", "Saturation can be defined with decimals (e.g. 0.3)"})
	public static List<String> forestDragonFoods = Arrays.asList("forge:raw_meats:6:4", "minecraft:glow_berries:2:1", "minecraft:sweet_berries:1:1", "minecraft:rotten_flesh:2:4", "minecraft:spider_eye:6:8", "minecraft:rabbit:7:8", "minecraft:poisonous_potato:7:8", "minecraft:chorus_fruit:9:8", "minecraft:brown_mushroom:2:1", "minecraft:red_mushroom:2:3", "minecraft:honey_bottle:6:1", "minecraft:warped_fungus:3:3", "minecraft:crimson_fungus:3:3", "dragonsurvival:forest_dragon_treat:4:8", "dragonsurvival:meat_chorus_mix:12:8", "additionaldragons:cursed_marrow", "nocubes_better_frogs:raw_frog_leg:4:4", "infernalexp:raw_hogchop:6:6", "phantasm:chorus_fruit_salad:10:10", "dragonsurvival:meat_wild_berries:12:10", "dragonsurvival:smelly_meat_porridge:6:10", "dragonsurvival:diamond_chorus:15:12", "dragonsurvival:luminous_ointment:5:3", "dragonsurvival:sweet_sour_rabbit:10:6", "chinchillas:chinchilla_meat:6:8", "aquaculture:turtle_soup:8:8", "netherdepthsupgrade:wither_bonefish:4:6", "netherdepthsupgrade:bonefish:4:6", "infernalexp:raw_hogchop:8:8", "aoa3:fiery_chops:6:7", "aoa3:raw_chimera_chop:6:7", "aoa3:raw_furlion_chop:6:7", "aoa3:raw_halycon_beef:7:8", "aoa3:raw_charger_shank:6:7", "aoa3:trilliad_leaves:8:11", "pamhc2foodextended:rawtofabbititem", "pamhc2foodextended:rawtofickenitem", "quark:golden_frog_leg:12:14", "pamhc2foodextended:rawtofuttonitem", "alexsmobs:kangaroo_meat:5:6", "alexsmobs:moose_ribs:6:8", "simplefarming:raw_horse_meat:5:6", "simplefarming:raw_bacon:3:3", "simplefarming:raw_chicken_wings:2:3", "simplefarming:raw_sausage:3:4", "xenoclustwo:raw_tortice:7:8", "unnamedanimalmod:musk_ox_shank:7:8", "unnamedanimalmod:frog_legs:5:6", "unnamedanimalmod:mangrove_fruit:4:7", "betteranimalsplus:venisonraw:7:6", "born_in_chaos_v1:corpse_maggot:1:1", "born_in_chaos_v1:monster_flesh:1:1", "betteranimalsplus:pheasantraw:7:5", "betteranimalsplus:turkey_leg_raw:4:5", "infernalexp:raw_hogchop:6:7", "infernalexp:cured_jerky:10:7", "rats:raw_rat:4:5", "aquaculture:frog:4:5", "aquaculture:frog_legs_raw:4:4", "aquaculture:box_turtle:4:5", "aquaculture:arrau_turtle:4:5", "aquaculture:starshell_turtle:4:5", "undergarden:raw_gloomper_leg:4:5", "undergarden:raw_dweller_meat:6:7", "farmersdelight:chicken_cuts:3:3", "farmersdelight:bacon:3:3", "farmersdelight:ham:9:10", "farmersdelight:minced_beef:5:3", "farmersdelight:mutton_chops:5:3", "abnormals_delight:duck_fillet:2:3", "abnormals_delight:venison_shanks:7:3", "autumnity:foul_berries:2:4", "autumnity:turkey:7:8", "autumnity:turkey_piece:2:4", "autumnity:foul_soup:12:8", "endergetic:bolloom_fruit:3:4", "quark:frog_leg:4:5", "nethers_delight:hoglin_loin:8:6", "nethers_delight:raw_stuffed_hoglin:18:10", "xreliquary:zombie_heart:4:7", "xreliquary:bat_wing:2:2", "eidolon:zombie_heart:7:7", "forbidden_arcanus:bat_wing:5:2", "twilightforest:raw_venison:7:7", "twilightforest:raw_meef:9:5", "twilightforest:hydra_chop", "cyclic:chorus_flight", "cyclic:chorus_spectral", "cyclic:toxic_carrot:15:15", "artifacts:everlasting_beef", "resourcefulbees:rainbow_honey_bottle", "resourcefulbees:diamond_honeycomb:5:5", "byg:soul_shroom:9:5", "byg:death_cap:9:8", "minecolonies:chorus_bread", "wyrmroost:raw_lowtier_meat:3:2", "wyrmroost:raw_common_meat:5:3", "wyrmroost:raw_apex_meat:8:6", "wyrmroost:raw_behemoth_meat:11:12", "wyrmroost:desert_wyrm:4:3", "eanimod:rawchicken_darkbig:9:5", "eanimod:rawchicken_dark:5:4", "eanimod:rawchicken_darksmall:3:2", "eanimod:rawchicken_pale:5:3", "eanimod:rawchicken_palesmall:4:3", "eanimod:rawrabbit_small:4:4", "environmental:duck:4:3", "environmental:venison:7:7", "cnb:lizard_item_jungle:4:4", "cnb:lizard_item_mushroom:4:4", "cnb:lizard_item_jungle_2:4:4", "cnb:lizard_item_desert_2:4:4", "cnb:lizard_egg:5:2", "cnb:lizard_item_desert:4:4", "snowpig:frozen_porkchop:7:3", "snowpig:frozen_ham:5:7", "untamedwilds:spawn_snake:4:4", "untamedwilds:snake_green_mamba:4:4", "untamedwilds:snake_rattlesnake:4:4", "untamedwilds:snake_emerald:4:4", "untamedwilds:snake_carpet_python:4:4", "untamedwilds:snake_corn:4:4", "untamedwilds:snake_gray_kingsnake:4:4", "untamedwilds:snake_coral:4:4", "untamedwilds:snake_ball_python:4:4", "untamedwilds:snake_black_mamba:4:4", "untamedwilds:snake_western_rattlesnake:4:4", "untamedwilds:snake_taipan:4:4", "untamedwilds:snake_adder:4:4", "untamedwilds:snake_rice_paddy:4:4", "untamedwilds:snake_coral_blue:4:4", "untamedwilds:snake_cave_racer:4:4", "untamedwilds:snake_swamp_moccasin:4:4", "untamedwilds:softshell_turtle_pig_nose:4:4", "untamedwilds:softshell_turtle_flapshell:4:4", "untamedwilds:softshell_turtle_chinese:4:4", "untamedwilds:tortoise_asian_box:4:4", "untamedwilds:tortoise_gopher:4:4", "untamedwilds:tortoise_leopard:4:4", "untamedwilds:spawn_softshell_turtle:4:4", "untamedwilds:softshell_turtle_nile:4:4", "untamedwilds:softshell_turtle_spiny:4:4", "untamedwilds:tortoise_sulcata:4:4", "untamedwilds:tortoise_star:4:4", "untamedwilds:spawn_tortoise:4:4", "naturalist:venison:7:6", "leescreatures:raw_boarlin:6:6", "mysticalworld:venison:5:5", "toadterror:toad_chops:8:7", "prehistoricfauna:raw_large_thyreophoran_meat:7:6", "prehistoricfauna:raw_large_marginocephalian_meat:8:6", "prehistoricfauna:raw_small_ornithischian_meat:4:3", "prehistoricfauna:raw_large_sauropod_meat:11:9", "prehistoricfauna:raw_small_sauropod_meat:4:4", "prehistoricfauna:raw_large_theropod_meat:7:7", "prehistoricfauna:raw_small_theropod_meat:4:4", "prehistoricfauna:raw_small_archosauromorph_meat:3:3", "prehistoricfauna:raw_large_archosauromorph_meat:6:5", "prehistoricfauna:raw_small_reptile_meat:4:3", "prehistoricfauna:raw_large_synapsid_meat:5:6", "ends_delight:dragon_leg:15:15", "ends_delight:raw_dragon_meat:10:10", "ends_delight:raw_dragon_meat_cuts:5:2", "ends_delight:dragon_breath_and_chorus_soup:15:15", "ends_delight:ender_sauce:8:15", "ends_delight:raw_ender_mite_meat:1:1", "ends_delight:non_hatchable_dragon_egg:8:5", "ends_delight:shulker_meat:7:4", "unusualend:chorus_juice:2:2", "ends_delight:liquid_dragon_egg:3:3", "ends_delight:shulker_meat_slice:4:1", "unusualend:ender_firefly_egg:2:3", "unusualend:chorus_petal:1:1", "unusualend:chorus_pie:3:4", "unusualend:ender_stew:6:1", "unusualprehistory:kentro_eggs:3:3", "unusualprehistory:hwacha_eggs:3:3", "unusualprehistory:ulugh_eggs:3:3", "unusualprehistory:antarcto_eggs:3:3", "unusualprehistory:austro_eggs:3:3", "unusualprehistory:pachy_eggs:3:3", "unusualprehistory:raptor_eggs:3:3", "unusualprehistory:trike_eggs:4:4", "unusualprehistory:rex_eggs:6:3", "unusualprehistory:coty_eggs:3:3", "unusualprehistory:meaty_buffet:12:15", "unusualprehistory:majunga_eggs:3:3", "unusualprehistory:anuro_eggs:3:3", "unusualprehistory:raw_austro:6:3", "unusualprehistory:raw_coty:5:3", "nourished_nether:raw_hoglin:8:8", "gothic:meat:7:8", "gothic:bug_meat:2:2", "gothic:scavenger_egg:3:3", "gothic:snapperweed:1:1", "gothic:blue_elder:1:1", "unusualend:wandering_stew:1:1", "nethersdelight:raw_stuffed_hoglin:15:15", "nethersdelight:hoglin_ear:1:1", "nethersdelight:ground_strider:6:2", "nethersdelight:strider_slice:8:8", "nethersdelight:hoglin_ear:1:1", "nethersdelight:hoglin_loin:6:6", "nethersdelight:propelpearl:1:1", "orcz:squig_eye:6:6", "orcz:orceye:4:4", "goated:chevon:6:6", "rottencreatures:frozen_rotten_flesh:4:4", "regions_unexplored:tall_green_bioshroom:3:3", "regions_unexplored:green_bioshroom:3:3", "regions_unexplored:blue_bioshroom:3:3", "regions_unexplored:tall_blue_bioshroom:3:3", "regions_unexplored:tall_pink_bioshroom:3:3", "regions_unexplored:pink_bioshroom:3:3", "regions_unexplored:yellow_bioshroom:3:3", "regions_unexplored:tall_yellow_bioshroom:3:3", "quark:glow_shroom:3:3", "gothic:cave_mushrooms:3:3", "gothic:tall_mushrooms:3:3", "gothic:miners_food_wild:3:3", "gothic:black_mushroom:3:3", "phantasm:putac_shroom:3:3", "farmersdelight:red_mushroom_colony:10:3", "farmersdelight:brown_mushroom_colony:10:3", "gothic:miners_food:3:3", "gothic:black_mushroom_item:3:3", "gothic:blue_elder:3:3", "frozen_delight:truffle_slice:4:8", "frozenup:truffle:8:4", "frozen_delight:mushroom_mix:16:3", "nourished_nether:nether_fungus_stew:7:3", "nethersdelight:warped_fungus_colony:10:3", "nethersdelight:crimson_fungus_colony:10:3", "unusualend:chorus_fungus:3:3", "orcz:orcshroom:3:3", "orcz:blue_orcshroom:3:3", "waterstrainer:worm:1:1", "undergarden:veil_mushroom:1:1", "undergarden:indigo_mushroom:1:1", "undergarden:smogstem_sapling:1:1", "undergarden:blood_mushroom:1:1", "undergarden:ink_mushroom:1:1", "undergarden:blisterberry:1:1", "undergarden:rotten_blisterberry:1:1", "undergarden:ink_mushroom:1:1", "undergarden:bloody_stew:1:1", "undergarden:inky_stew:1:1", "undergarden:veiled_stew:1:1", "undergarden:indigo_stew:1:1", "butchersdelight:deadchiken:6:4", "butchersdelight:deadstrider:6:4", "butchersdelight:deadrabbitbrown:6:4", "butchersdelight:deadllama:6:4", "butchersdelight:deadhoglin:6:4", "butchersdelight:deadgoat:6:4", "butchersdelight:deadsheep:6:4", "butchersdelight:deadpig:6:4", "butchersdelight:dead_cow:6:4", "butchersdelightfoods:llama_loin:6:4", "butchersdelightfoods:llamma_ribs:6:4", "butchersdelightfoods:llama_leg:6:4", "butchersdelightfoods:goat_loin:6:4", "butchersdelightfoods:goatrack:6:4", "butchersdelightfoods:goat_shank:6:4", "butchersdelightfoods:porkribs:6:4", "butchersdelightfoods:porkloin:6:4", "butchersdelightfoods:ham:6:4", "butchersdelightfoods:sheeploin:6:4", "butchersdelightfoods:sheeprack:6:4", "butchersdelightfoods:sheepshank:6:4", "butchersdelightfoods:beeftenderloin:6:4");

	@ConfigType(Item.class)
	@ConfigOption(side = ConfigSide.SERVER, category = {"food", "sea_dragon"}, key = "seaDragon", comment = {"Dragon food formatting: mod_id:item_id:nutrition:saturation", "Nutrition / saturation values are optional as the human values will be used if missing.", "Saturation can be defined with decimals (e.g. 0.3)"})
	public static List<String> seaDragonFoods = Arrays.asList("forge:raw_fishes:6:4", "minecraft:kelp:1:1", "minecraft:pufferfish:8:8", "dragonsurvival:sea_dragon_treat:4:8", "dragonsurvival:seasoned_fish:12:10", "dragonsurvival:golden_coral_pufferfish:12:14", "dragonsurvival:frozen_raw_fish:2:1", "dragonsurvival:golden_turtle_egg:15:12", "additionaldragons:slippery_sushi:10:8", "aoa3:raw_candlefish:9:9", "aoa3:raw_crimson_skipper:8:8", "aoa3:raw_fingerfish:4:4", "aoa3:raw_pearl_stripefish:5:4", "aoa3:raw_limefish:5:5", "aoa3:raw_sailback:6:5", "netherdepthsupgrade:soulsucker:6:7", "netherdepthsupgrade:obsidianfish:6:7", "netherdepthsupgrade:lava_pufferfish:8:7", "netherdepthsupgrade:searing_cod:6:7", "netherdepthsupgrade:glowdine:6:7", "netherdepthsupgrade:warped_kelp:2:2", "netherdepthsupgrade:lava_pufferfish_slice:2:2", "netherdepthsupgrade:glowdine_slice:2:2", "netherdepthsupgrade:soulsucker_slice:2:2", "netherdepthsupgrade:obsidianfish_slice:2:2", "netherdepthsupgrade:searing_cod_slice:2:2", "crittersandcompanions:clam:10:3", "aoa3:raw_golden_gullfish:10:2", "aoa3:raw_turquoise_stripefish:7:6", "aoa3:raw_violet_skipper:7:7", "aoa3:raw_rocketfish:4:10", "aoa3:raw_crimson_stripefish:8:7", "aoa3:raw_sapphire_strider:9:8", "aoa3:raw_dark_hatchetfish:9:9", "aoa3:raw_ironback:10:9", "aoa3:raw_rainbowfish:11:11", "aoa3:raw_razorfish:12:14", "alexsmobs:lobster_tail:4:5", "alexsmobs:blobfish:8:9", "oddwatermobs:raw_ghost_shark:8:8", "oddwatermobs:raw_isopod:4:2", "oddwatermobs:raw_mudskipper:6:7", "oddwatermobs:raw_coelacanth:9:10", "oddwatermobs:raw_anglerfish:6:6", "oddwatermobs:deep_sea_fish:4:2", "oddwatermobs:crab_leg:5:6", "simplefarming:raw_calamari:5:6", "unnamedanimalmod:elephantnose_fish:5:6", "unnamedanimalmod:flashlight_fish:5:6", "born_in_chaos_v1:sea_terror_eye:10:4", "born_in_chaos_v1:rotten_fish:4:2", "unnamedanimalmod:rocket_killifish:5:6", "unnamedanimalmod:leafy_seadragon:5:6", "unnamedanimalmod:elephantnose_fish:5:6", "betteranimalsplus:eel_meat_raw:5:6", "betteranimalsplus:calamari_raw:4:5", "betteranimalsplus:crab_meat_raw:4:4", "aquaculture:fish_fillet_raw:2:2", "aquaculture:goldfish:8:4", "aquaculture:algae:3:2", "betterendforge:end_fish_raw:6:7", "betterendforge:hydralux_petal:3:3", "betterendforge:charnia_green:2:2", "shroomed:raw_shroomfin:5:6", "undergarden:raw_gwibling:5:6", "bettas:betta_fish:4:5", "quark:crab_leg:4:4", "pamhc2foodextended:rawtofishitem", "fins:banded_redback_shrimp:6:1", "fins:night_light_squid:6:2", "fins:night_light_squid_tentacle:6:2", "fins:emerald_spindly_gem_crab:7:2", "fins:amber_spindly_gem_crab:7:2", "fins:rubby_spindly_gem_crab:7:2", "fins:sapphire_spindly_gem_crab:7:2", "fins:pearl_spindly_gem_crab:7:2", "fins:papa_wee:6:2", "fins:bugmeat:4:2", "fins:raw_golden_river_ray_wing:6:2", "fins:red_bull_crab_claw:4:4", "fins:white_bull_crab_claw:4:4", "fins:wherble_fin:1:1", "forbidden_arcanus:tentacle:5:2", "pneumaticcraft:raw_salmon_tempura:6:10", "rats:ratfish:4:2", "upgrade_aquatic:purple_pickerelweed:2:2", "upgrade_aquatic:blue_pickerelweed:2:2", "upgrade_aquatic:polar_kelp:2:2", "upgrade_aquatic:tongue_kelp:2:2", "upgrade_aquatic:thorny_kelp:2:2", "upgrade_aquatic:ochre_kelp:2:2", "upgrade_aquatic:lionfish:8:9", "aquaculture:sushi:6:5", "freshwarriors:fresh_soup:15:10", "freshwarriors:beluga_caviar:10:3", "freshwarriors:piranha:4:1", "freshwarriors:tilapia:4:1", "freshwarriors:stuffed_piranha:4:1", "freshwarriors:tigerfish:5:5", "freshwarriors:toe_biter_leg:3:3", "untamedwilds:egg_arowana:4:4", "untamedwilds:egg_trevally_jack:4:4", "untamedwilds:egg_trevally:4:4", "untamedwilds:egg_giant_salamander:6:4", "untamedwilds:egg_giant_salamander_hellbender:6:4", "untamedwilds:egg_giant_salamander_japanese:6:4", "untamedwilds:giant_clam:4:4", "untamedwilds:giant_clam_derasa:4:4", "untamedwilds:giant_clam_maxima:4:4", "untamedwilds:giant_clam_squamosa:4:4", "untamedwilds:egg_trevally_giant:6:4", "untamedwilds:egg_trevally:6:4", "untamedwilds:egg_trevally_bigeye:6:4", "untamedwilds:egg_sunfish:6:4", "untamedwilds:egg_sunfish_sunfish:6:4", "untamedwilds:egg_giant_clam_squamosa:6:4", "untamedwilds:egg_giant_clam_gigas:6:4", "untamedwilds:egg_giant_clam_derasa:6:4", "untamedwilds:egg_giant_clam:6:4", "untamedwilds:egg_football_fish:6:4", "untamedwilds:egg_arowana:6:4", "untamedwilds:egg_arowana_jardini:6:4", "untamedwilds:egg_arowana_green:6:4", "mysticalworld:raw_squid:6:5", "aquafina:fresh_soup:10:10", "aquafina:beluga_caviar:10:3", "aquafina:raw_piranha:4:1", "aquafina:raw_tilapia:4:1", "aquafina:stuffed_piranha:4:1", "aquafina:tigerfish:5:5", "aquafina:toe_biter_leg:3:3", "aquafina:raw_angelfish:4:1", "aquafina:raw_football_fish:4:1", "aquafina:raw_foxface_fish:4:1", "aquafina:raw_royal_gramma:4:1", "aquafina:raw_starfish:4:1", "aquafina:spider_crab_leg:4:1", "aquafina:raw_stingray_slice:4:1", "prehistoricfauna:raw_ceratodus:5:5", "prehistoricfauna:raw_cyclurus:4:4", "prehistoricfauna:raw_potamoceratodus:5:5", "prehistoricfauna:raw_myledaphus:4:4", "prehistoricfauna:raw_gar:4:4", "prehistoricfauna:raw_oyster:4:3", "prehistoric_delight:prehistoric_fillet:3:3", "seadwellers:rainbow_trout:10:10", "crittersandcompanions:koi_fish:5:5", "aquamirae:elodea:3:3", "croptopia:clam:3:3", "croptopia:calamari:2:3", "croptopia:anchovy:3:2", "croptopia:crab:6:8", "croptopia:glowing_calamari:4:5", "croptopia:oyster:2:4", "croptopia:roe:1:2", "croptopia:shrimp:2:2", "croptopia:tuna:6:4", "aquamirae:spinefish:4:4", "alexsmobs:flying_fish:6:4", "untamedwilds:egg_triggerfish:2:4", "untamedwilds:egg_catfish:2:4", "netherdepthsupgrade:eyeball:3:3", "netherdepthsupgrade:eyeball_fish:3:3", "oceansdelight:guardian:4:3", "oceansdelight:guardian_tail:1:3", "oceansdelight:cut_tentacles:3:1", "oceansdelight:tentacles:3:4", "oceansdelight:tentacle_on_a_stick:3:4", "oceansdelight:fugu_slice:5:4", "oceansdelight:elder_guardian_slice:8:6", "oceansdelight:elder_guardian_slab:15:15", "upgrade_aquatic:elder_eye:15:15", "unusualprehistory:golden_scau:15:15", "unusualprehistory:raw_scau:4:3", "unusualprehistory:raw_stetha:4:3", "unusualprehistory:stetha_eggs:4:3", "unusualprehistory:beelze_eggs:4:3", "unusualprehistory:scau_eggs:4:3", "unusualprehistory:ammon_eggs:4:3", "unusualprehistory:dunk_eggs:4:3", "netherdepthsupgrade:crimson_seagrass:2:2", "netherdepthsupgrade:crimson_kelp:2:2", "netherdepthsupgrade:warped_seagrass:2:2", "undergarden:glitterkelp:2:2", "enlightened_end:raw_stalker:10:4");

	@ConfigType(Item.class)
	@ConfigOption(side = ConfigSide.SERVER, key = "keepEffects", category = "food", comment = "Food items which should keep their effects even if they're not a valid food for the dragon (foodHungerEffect will be disabled for these items as well)")
	public static List<String> keepEffects = List.of("gothic:elixir_of_speed", "gothic:elixir_of_health", "gothic:elixir_of_mental_cleansing");

	// Tooltip maps
	public static CopyOnWriteArrayList<Item> CAVE_DRAGON_FOOD;
	public static CopyOnWriteArrayList<Item> FOREST_DRAGON_FOOD;
	public static CopyOnWriteArrayList<Item> SEA_DRAGON_FOOD;
	public static int rightHeight = 0;


	private static final ResourceLocation FOOD_ICONS = new ResourceLocation(DragonSurvivalMod.MODID + ":textures/gui/dragon_hud.png");
	private static final RandomSource RANDOM = RandomSource.create();

	private static ConcurrentHashMap<String, Map<Item, FoodProperties>> DRAGON_FOODS;

	/** Rebuild the food map if any sort of server config changes */
	@SubscribeEvent
	public static void onConfigLoad(final ModConfigEvent event) {
		if (event.getConfig().getType() == Type.SERVER) {
			rebuildFoodMap();
		}
	}

	public static void rebuildFoodMap() {
		DragonSurvivalMod.LOGGER.debug("Rebuilding food map...");

		ConcurrentHashMap<String, ConcurrentHashMap<Item, FoodProperties>> map = new ConcurrentHashMap<>();
		map.put(DragonTypes.CAVE.getTypeName(), buildDragonFoodMap(DragonTypes.CAVE));
		map.put(DragonTypes.FOREST.getTypeName(), buildDragonFoodMap(DragonTypes.FOREST));
		map.put(DragonTypes.SEA.getTypeName(), buildDragonFoodMap(DragonTypes.SEA));

        clearTooltipMaps();

		DRAGON_FOODS = new ConcurrentHashMap<>(map);
	}

    public static void clearTooltipMaps() {
        if (CAVE_DRAGON_FOOD != null) {
            CAVE_DRAGON_FOOD.clear();
        }

        if (FOREST_DRAGON_FOOD != null) {
            FOREST_DRAGON_FOOD.clear();
        }

        if (SEA_DRAGON_FOOD != null) {
            SEA_DRAGON_FOOD.clear();
        }
    }

	private static ConcurrentHashMap<Item, FoodProperties> buildDragonFoodMap(final AbstractDragonType type) {
		ConcurrentHashMap<Item, FoodProperties> map = new ConcurrentHashMap<>();
		String[] foodConfiguration = new String[0];

		if (DragonUtils.isDragonType(type, DragonTypes.CAVE)) {
			foodConfiguration = caveDragonFoods.toArray(new String[0]);
		} else if (DragonUtils.isDragonType(type, DragonTypes.FOREST)) {
			foodConfiguration = forestDragonFoods.toArray(new String[0]);
		} else if (DragonUtils.isDragonType(type, DragonTypes.SEA)) {
			foodConfiguration = seaDragonFoods.toArray(new String[0]);
		}

		foodConfiguration = Stream.of(foodConfiguration).sorted(Comparator.reverseOrder()).toArray(String[]::new);

		for (String entry : foodConfiguration) {
			// TODO :: item: / tag: might have been used previously but current entries do not use this format
			if (entry.startsWith("item:")) {
				entry = entry.substring("item:".length());
			}

			if (entry.startsWith("tag:")) {
				entry = entry.substring("tag:".length());
			}

			String[] configuration = entry.split(":");
			ResourceLocation resourceLocation = new ResourceLocation(configuration[0], configuration[1]);
			TagKey<Item> tagKey = TagKey.create(ForgeRegistries.Keys.ITEMS, resourceLocation);

			// Add all food items from the tag
			if (ForgeRegistries.ITEMS.tags().isKnownTagName(tagKey)) {
				ForgeRegistries.ITEMS.tags().getTag(tagKey).forEach(item -> {
					FoodProperties foodProperties = calculateFoodProperties(configuration, item, type);

					if (foodProperties != null) {
						map.put(item, foodProperties);
					}
				});
			}

			// Add food item
			if (ForgeRegistries.ITEMS.containsKey(resourceLocation)) {
				Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);

				if (item != null && item != Items.AIR) {
					FoodProperties foodProperties = calculateFoodProperties(configuration, item, type);

					if (foodProperties != null) {
						map.put(item, foodProperties);
					}
				}
			}
		}

		return new ConcurrentHashMap<>(map);
	}

	private static FoodProperties calculateFoodProperties(final String[] configuration, final Item item, final AbstractDragonType type) {
		// Use configured food properties (if present), otherwise use the properties of the item (if present) otherwise use a default value of 1
		FoodProperties properties = item.getFoodProperties(item.getDefaultInstance(), null);

		try {
			int nutrition = configuration.length == 4 ? Integer.parseInt(configuration[2].strip()) : properties != null ? properties.getNutrition() : 1;
			float saturation = configuration.length == 4 ? Float.parseFloat(configuration[3].strip()) : properties != null ? (properties.getNutrition() * properties.getSaturationModifier() * 2) : 0;

			return calculateDragonFoodProperties(item, type, nutrition, saturation, true, properties);
		} catch (NumberFormatException nfe) {
			DragonSurvivalMod.LOGGER.error(String.format("Invalid food configuration for %s, using default values.", item));
			return calculateDragonFoodProperties(item, type, 1, 0, true, properties);
		}
	}

	private static @Nullable FoodProperties calculateDragonFoodProperties(final Item item, final AbstractDragonType type, int nutrition, float saturation, boolean isDragonFood, final FoodProperties original) {
		if (item == null) {
			return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation / (float) nutrition / 2.0F).build();
		}

		if (type == null) {
			return original;
		}

		FoodProperties.Builder builder = new FoodProperties.Builder();
		boolean shouldKeepEffects = keepEffects.contains(ResourceHelper.getKey(item).toString());

		// Copy the configurations and effects from the initial food properties
		if (original != null) {
			if (original.isMeat()) {
				builder.meat();
			}

			if (original.canAlwaysEat()) {
				builder.alwaysEat();
			}

			if (original.isFastFood()) {
				builder.fast();
			}

			for (Pair<MobEffectInstance, Float> effect : original.getEffects()) {
				if (effect == null || effect.getFirst() == null) {
					continue;
				}

				// Hunger and Poison effects will be skipped
				if ((shouldKeepEffects || isDragonFood) && effect.getFirst().getEffect().isBeneficial()) {
					builder.effect(effect::getFirst, effect.getSecond());
				}
			}
		}

		if (saturation == 0 || nutrition == 0) {
			builder.nutrition(nutrition).saturationMod(0);
		} else {
			builder.nutrition(nutrition).saturationMod(saturation / (float) nutrition / 2.0F);
		}

		return builder.build();
	}

	public static CopyOnWriteArrayList<Item> getEdibleFoods(final AbstractDragonType type) {
		if (type == null) {
			return new CopyOnWriteArrayList<>();
		}

        if (DragonUtils.isDragonType(type, DragonTypes.FOREST) && FOREST_DRAGON_FOOD != null && !FOREST_DRAGON_FOOD.isEmpty()) {
            return FOREST_DRAGON_FOOD;
        } else if (DragonUtils.isDragonType(type, DragonTypes.SEA) && SEA_DRAGON_FOOD != null && !SEA_DRAGON_FOOD.isEmpty()) {
            return SEA_DRAGON_FOOD;
        } else if (DragonUtils.isDragonType(type, DragonTypes.CAVE) && CAVE_DRAGON_FOOD != null && !CAVE_DRAGON_FOOD.isEmpty()) {
            return CAVE_DRAGON_FOOD;
        }

        if (DRAGON_FOODS == null) {
			rebuildFoodMap();
		}

		CopyOnWriteArrayList<Item> foods = new CopyOnWriteArrayList<>();

		for (Item item : DRAGON_FOODS.get(type.getTypeName()).keySet()) {
			final FoodProperties foodProperties = DRAGON_FOODS.get(type.getTypeName()).get(item);
			boolean isSafe = true;

			if (foodProperties != null) {
				for (Pair<MobEffectInstance, Float> effect : foodProperties.getEffects()) {
					if (effect == null || effect.getFirst() == null) {
						continue;
					}

					MobEffect mobEffect = effect.getFirst().getEffect();
					// Because we decided to leave confusion on pufferfish
					if (ToolTipHandler.hideUnsafeFood && !mobEffect.isBeneficial() && mobEffect != MobEffects.CONFUSION) {
						isSafe = false;
						break;
					}
				}

				if (isSafe && (foodProperties.getNutrition() > 0 || foodProperties.getSaturationModifier() > 0)) {
					foods.add(item);
				}
			}
		}

		if (Objects.equals(type, DragonTypes.FOREST) && FOREST_DRAGON_FOOD == null) {
			FOREST_DRAGON_FOOD = foods;
		} else if (Objects.equals(type, DragonTypes.CAVE) && CAVE_DRAGON_FOOD == null) {
			CAVE_DRAGON_FOOD = foods;
		} else if (Objects.equals(type, DragonTypes.SEA) && SEA_DRAGON_FOOD == null) {
			SEA_DRAGON_FOOD = foods;
		}

		return foods;
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean renderFoodBar(final ForgeGui forgeGUI, final GuiGraphics guiGraphics, int width, int height) {
		Player localPlayer = ClientProxy.getLocalPlayer();

		if (localPlayer == null || !forgeGUI.shouldDrawSurvivalElements()) {
			return false;
		}

		DragonStateHandler handler = DragonStateProvider.getHandler(localPlayer);

		if (handler == null || !handler.isDragon()) {
			return false;
		}

		Minecraft.getInstance().getProfiler().push("food");
		RenderSystem.enableBlend();

		rightHeight = forgeGUI.rightHeight;
		forgeGUI.rightHeight += 10;

		final int left = width / 2 + 91;
		final int top = height - rightHeight;
		rightHeight += 10;
		final FoodData food = localPlayer.getFoodData();
		final int type = DragonUtils.isDragonType(handler, DragonTypes.FOREST) ? 0 : DragonUtils.isDragonType(handler, DragonTypes.CAVE) ? 9 : 18;
		final boolean hunger = localPlayer.hasEffect(MobEffects.HUNGER);

		for (int i = 0; i < 10; i++) {
			int icon = i * 2 + 1; // there can be 10 icons (food level maximum is 20)
			int y = top;

			if (food.getSaturationLevel() <= 0 && localPlayer.tickCount % (food.getFoodLevel() * 3 + 1) == 0) {
				// Animate the food icons (moving up / down)
				y = top + RANDOM.nextInt(3) - 1;
			}

			guiGraphics.blit(FOOD_ICONS, left - i * 8 - 9, y, hunger ? 117 : 0, type, 9, 9);

			if (icon < food.getFoodLevel()) {
				guiGraphics.blit(FOOD_ICONS, left - i * 8 - 9, y, hunger ? 72 : 36, type, 9, 9);
			} else if (icon == food.getFoodLevel()) {
				guiGraphics.blit(FOOD_ICONS, left - i * 8 - 9, y, hunger ? 81 : 45, type, 9, 9);
			}
		}

		RenderSystem.disableBlend();
		Minecraft.getInstance().getProfiler().pop();

		return true;
	}

	@SubscribeEvent
	public void onItemUseStart(final LivingEntityUseItemEvent.Start event) {
		if (disableDragonFoodHandling) {
			return;
		}

		DragonStateProvider.getCap(event.getEntity()).ifPresent(handler -> {
			if (handler.isDragon()) {
				event.setDuration(getUseDuration(event.getItem(), handler.getType()));
			}
		});
	}

	@SubscribeEvent
	public void onItemRightClick(final PlayerInteractEvent.RightClickItem event) {
		if (disableDragonFoodHandling) {
			return;
		}

		DragonStateProvider.getCap(event.getEntity()).ifPresent(handler -> {
			if (handler.isDragon()) {
				if (event.getSide() == LogicalSide.SERVER) {
					ServerPlayer player = (ServerPlayer) event.getEntity();
					ServerLevel level = player.serverLevel();
					InteractionHand hand = event.getHand();
					ItemStack itemStackInHand = player.getItemInHand(event.getHand());

					if (isEdible(itemStackInHand, handler.getType())) {
						int stackCount = itemStackInHand.getCount();
						int damageValue = itemStackInHand.getDamageValue();

						InteractionResultHolder<ItemStack> result = itemStackInHand.use(level, player, hand);
						ItemStack resultItemStack = result.getObject();

						if (resultItemStack == itemStackInHand && resultItemStack.getCount() == stackCount && getUseDuration(resultItemStack, handler.getType()) <= 0 && resultItemStack.getDamageValue() == damageValue) {
							// After using the item nothing happened
							event.setCancellationResult(result.getResult());
						} else if (result.getResult() == InteractionResult.FAIL && getUseDuration(resultItemStack, handler.getType()) > 0 && !player.isUsingItem()) {
							// Player cannot use the item
							event.setCancellationResult(result.getResult());
							event.setCanceled(true);
						} else {
							player.setItemInHand(hand, resultItemStack);

							if (player.isCreative()) {
								resultItemStack.setCount(stackCount);

								if (resultItemStack.isDamageableItem() && resultItemStack.getDamageValue() != damageValue) {
									resultItemStack.setDamageValue(damageValue);
								}
							}

							if (resultItemStack.isEmpty()) {
								player.setItemInHand(hand, ItemStack.EMPTY);
							}

							event.setCancellationResult(result.getResult());
							event.setCanceled(true);
						}
					}
				}
			}
		});
	}

	public static int getUseDuration(final ItemStack stack, final AbstractDragonType type) {
		if (isEdible(stack.getItem(), type)) {
			FoodProperties properties = getFoodProperties(stack, type);
			return properties != null && properties.isFastFood() ? 16 : 32;
		} else {
			return stack.getUseDuration();
		}
	}

	public static boolean isEdible(@NotNull final Item item, final AbstractDragonType type) {
		if (requireDragonFood && type != null && DRAGON_FOODS != null && DRAGON_FOODS.get(type.getTypeName()).containsKey(item)) {
			return true;
		}

		return item.getDefaultInstance().isEdible();
	}

	public static boolean isEdible(@NotNull final ItemStack stack, final AbstractDragonType type) {
		if (requireDragonFood && type != null && DRAGON_FOODS != null && DRAGON_FOODS.get(type.getTypeName()).containsKey(stack.getItem())) {
			return true;
		}

		return stack.isEdible();
	}

	public static @Nullable FoodProperties getFoodProperties(@NotNull final ItemStack stack, @Nullable final AbstractDragonType type) {
		return getFoodProperties(stack, type, null);
	}

	public static @Nullable FoodProperties getFoodProperties(@NotNull final ItemStack stack, @Nullable final AbstractDragonType type, @Nullable final LivingEntity entity) {
		if (type != null) {
			FoodProperties dragonProperties = DRAGON_FOODS.get(type.getTypeName()).get(stack.getItem());

			if (dragonProperties != null) {
				return dragonProperties;
			}
		}

		FoodProperties properties = stack.getFoodProperties(entity);

		if (requireDragonFood && type != null && properties != null) {
			return getBadFoodProperties();
		}

		return properties;
	}

	private static FoodProperties getBadFoodProperties() {
		FoodProperties.Builder builder = new FoodProperties.Builder();
		builder.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F);
		builder.effect(() -> new MobEffectInstance(MobEffects.POISON, 600, 0), 0.5F);
		builder.nutrition(1);
		return builder.build();
	}
}