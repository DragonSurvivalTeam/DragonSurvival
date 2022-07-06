package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigType;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ServerConfig{
	ServerConfig(Builder builder){
		ConfigHandler.addConfigs(builder, Dist.DEDICATED_SERVER);
	}

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "harvestableStarBlock", comment = "Whether silk touch hoes can be used to harvest Predator Stars." )
	public static Boolean mineStarBlock = false;

	@ConfigRange( min = 0, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "altarUsageCooldown", comment = "How long of a cooldown in seconds the altar has after each use." )
	public static Integer altarUsageCooldown = 0;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "altarCraftable", comment = "Whether dragon altars are craftable or not. When disabled you can only use the command or creative mode to become a dragon." )
	public static Boolean altarCraftable = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "keepClawItems", comment = "Whether to keep items in the claw slots on death otherwise they will drop on death." )
	public static Boolean keepClawItems = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "syncClawRender", comment = "If players are allowed to hide their claws and teeth from other players. If it is important to you to see your opponent's weapon during pvp, set false." )
	public static Boolean syncClawRender = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "canMoveInEmote", comment = "If players are allowed to move while performing emotes" )
	public static Boolean canMoveInEmote = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "canMoveWhileCasting", comment = "If you should be able to move while casting certain skills or if player movement can be prevented." )
	public static Boolean canMoveWhileCasting = false;

	@ConfigRange( min = 0.0, max = 1000.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "maxSizeVari", comment = "The maximum size variation in percentage" )
	public static Double maxSizeVari = 10.0;

	@ConfigRange( min = -1000.0, max = 0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "minSizeVari", comment = "The minimum size variation in percentage" )
	public static Double minSizeVari = -10.0;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "useModifiedHitboxes", comment = "Should the mod use the new modified hitboxes for dragon plauers?" )
	public static Boolean useModifiedHitboxes = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "startWithDragonChoice", comment = "Should the dragon altar interface be opened when the player first joins the world?" )
	public static Boolean startWithDragonChoice = false;

	// Growth
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "sizeChangesHitbox", comment = "Whether the dragon size determines its hitbox size. The bigger the dragon, the bigger the hitbox. If false standard player's hitbox be used." )
	public static Boolean sizeChangesHitbox = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "largerDragonHitbox", comment = "Whether the dragon hitbox grows past a human hitbox." )
	public static Boolean hitboxGrowsPastHuman = true;

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "growNewborn", comment = "List of items to grow newborn dragon. Format: item/modid:id" )
	public static List<String> growNewborn = List.of("dragonsurvival:heart_element", "dragonsurvival:weak_dragon_heart", "dragonsurvival:elder_dragon_heart");

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "growYoung", comment = "List of items to grow young dragon. Format: item/modid:id" )
	public static List<String> growYoung = List.of("dragonsurvival:weak_dragon_heart", "dragonsurvival:elder_dragon_heart");

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "growAdult", comment = "List of items to grow adult dragon. Format: item/modid:id" )
	public static List<String> growAdult = List.of("dragonsurvival:elder_dragon_heart");

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "alternateGrowing", comment = "Defines if dragon should grow without requirement of catalyst items. Your dragon will just grow over time." )
	public static Boolean alternateGrowing = true;

	@ConfigRange( min = 14.0, max = 1000000.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "maxGrowthSize", comment = "Defines the max size your dragon can grow to. Values that are too high can break your game. It is not advisable to set a number higher than 60." )
	public static Double maxGrowthSize = 60.0;

	@ConfigRange( min = 0, max = 1000000.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "reachBonus", comment = "The bonus that is given to dragons at ever 60 size. Human players have 1.0x reach and a size 60 dragon will have 1.5x distance with default value. Only applies to block mining." )
	public static Double reachBonus = 0.5;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "saveGrowthStage", comment = "Should the growth stage of a dragon be saved even when you change. Does not affect the saving progress of magic (use saveAllAbilities). The author does not approve of weredragons, but if you insist..." )
	public static Boolean saveGrowthStage = false;

	@ConfigRange( min = 1, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "minHealth", comment = "Dragon starting health. Minumum health dragons will start off with." )
	public static Integer minHealth = 14;

	@ConfigRange( min = 1, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "maxHealth", comment = "Maximum health dragons can grow to." )
	public static Integer maxHealth = 40;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "newbornGrowthModifier", comment = "A multiplier to change the growth rate from newborn to young. At 1.0 it takes about 3 hours to turn a newborn dragon into a young dragon." )
	public static Double newbornGrowthModifier = 0.3;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "youngGrowthModifier", comment = "A multiplier to change the growth rate from young to adult. At 1.0 it takes about 1 day to turn a young dragon into a adult dragon." )
	public static Double youngGrowthModifier = 0.5;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "adultGrowthModifier", comment = "A multiplier to change the growth rate from adult to a full sized adult. At 1.0 it takes about 3 days to become a dragon of maximum adult size." )
	public static Double adultGrowthModifier = 0.9;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "growth", key = "maxGrowthModifier", comment = "A multiplier to change the growth rate from full sized adult to max size. The change in growth after the maximum adult size is measured in months and years." )
	public static Double maxGrowthModifier = 1.0;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "dragonHeartShardChance", comment = "The chance for dragon heart shards to drop from any mobs with max health between 14-20" )
	public static Double dragonHeartShardChance = 0.01;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "weakDragonHeartChance", comment = "The chance for weak dragon heart to drop from any mobs with max health between 20-50" )
	public static Double weakDragonHeartChance = 0.01;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "elderDragonHeartChance", comment = "The chance for dragon heart to drop from any mobs with max health above 50" )
	public static Double elderDragonHeartChance = 0.1;

	@ConfigType(EntityType.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "dragonHeartEntityList", comment = "Decide which entities can drop dragon hearts" )
	public static List<String> dragonHeartEntityList = List.of();

	@ConfigType(EntityType.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "weakDragonHeartEntityList", comment = "Decide which entities can drop weak dragon hearts" )
	public static List<String> weakDragonHeartEntityList = List.of();

	@ConfigType(EntityType.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "elderDragonHeartEntityList", comment = "Decide which entities can drop elder dragon hearts" )
	public static List<String> elderDragonHeartEntityList = List.of();

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "dragonHeartWhiteList", comment = "Should the dragonHeartEntityList be treated as an allowlist rather than a block list?" )
	public static Boolean dragonHeartWhiteList = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "weakDragonHeartWhiteList", comment = "Should the weakDragonHeartEntityList be treated as an allowlist rather than a block list?" )
	public static Boolean weakDragonHeartWhiteList = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "elderDragonHeartWhiteList", comment = "Should the elderDragonHeartEntityList be treated as an allowlist rather than a block list?" )
	public static Boolean elderDragonHeartWhiteList = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "dragonHeartUseList", comment = "Should the dragonHeartEntityList be used instead of the health requirement?" )
	public static Boolean dragonHeartUseList = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "weakDragonHeartUseList", comment = "Should the weakDragonHeartUseList be used instead of the health requirement?" )
	public static Boolean weakDragonHeartUseList = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "drops", key = "elderDragonHeartUseList", comment = "Should the elderDragonHeartUseList be used instead of the health requirement?" )
	public static Boolean elderDragonHeartUseList = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "treasure", key = "treasureHealthRegen", comment = "Whether sleeping on treasure will recover health or not. " )
	public static Boolean treasureHealthRegen = true;

	@ConfigRange( min = 1, max = 10000000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "treasure", key = "treasureRegenTicks", comment = "The time in ticks it takes to recover 1hp while sleeping on one treasure. A large number of treasures in one place reduces time." )
	public static Integer treasureRegenTicks = 24100;

	@ConfigRange( min = 1, max = 10000000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "treasure", key = "treasureRegenTicksReduce", comment = "The amount of ticks each additional treasure reduces the regen time by" )
	public static Integer treasureRegenTicksReduce = 100;

	@ConfigRange( min = 1, max = 10000000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "treasure", key = "maxTreasures", comment = "The max amount of additional treasure that can be used to reduce the regen time" )
	public static Integer maxTreasures = 240;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "sourceOfMagicInfiniteMagic", comment = "Whether using the source of magic block will grant the infinite magic buff." )
	public static Boolean sourceOfMagicInfiniteMagic = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "damageWrongSourceOfMagic", comment = "Whether using the the source of magic intended for another dragon type will hurt you." )
	public static Boolean damageWrongSourceOfMagic = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "canUseAllSourcesOfMagic", comment = "Whether you are able to use all types of source of magic no matter your dragon type." )
	public static Boolean canUseAllSourcesOfMagic = false;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "elderDragonDustTime", comment = "How long duration of the infinite magic effect using elder dragon dust gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer elderDragonDustTime = 20;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "elderDragonBoneTime", comment = "How long duration of the infinite magic effect using elder dragon bone gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer elderDragonBoneTime = 60;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "weakHeartShardTime", comment = "How long duration of the infinite magic effect using weak heart shard gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer weakHeartShardTime = 110;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "weakDragonHeartTime", comment = "How long duration of the infinite magic effect using weak dragon heart gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer weakDragonHeartTime = 310;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "source_of_magic", key = "elderDragonHeartTime", comment = "How long duration of the infinite magic effect using elder dragon heart gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer elderDragonHeartTime = 1010;

	@ConfigRange( min = 0.1, max = 1 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "maxFlightSpeed", comment = "Maximum acceleration fly speed up and down. Take into account the chunk load speed. A speed of 0.3 is optimal." )
	public static Double maxFlightSpeed = 0.3;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "startWithWings", comment = "Whether dragons born with wings." )
	public static Boolean startWithWings = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "enderDragonGrantsSpin", comment = "Whether you should be able to obtain the spin ability from the ender dragon." )
	public static Boolean enderDragonGrantsSpin = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "allowFlyingWhenTotallyHungry", comment = "Whether dragons can fly when totally hungry. You can't open your wings if you're hungry." )
	public static Boolean allowFlyingWithoutHunger = false;

	@ConfigRange( min = 0, max = 20 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "flightHungerThreshold", comment = "If the player's hunger is below this parameter, he can't open his wings." )
	public static Integer flightHungerThreshold = 6;

	@ConfigRange( min = 0, max = 20 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "flightHungerThreshold", comment = "If the player's hunger is less then or equal to this parameter, the wings will be folded even during flight." )
	public static Integer foldWingsThreshold = 0;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "flyingUsesHunger", comment = "Whether you use up hunger while flying." )
	public static Boolean flyingUsesHunger = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "enableFlightFallDamage", comment = "Whether fall damage in flight is included. If true dragon will take damage from the fall." )
	public static Boolean enableFlightFallDamage = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "lethalFallDamage", comment = "Whether fall damage from flight is lethal, otherwise it will leave you at half a heart" )
	public static Boolean lethalFlight = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "foldWingsOnLand", comment = "Whether your wings will fold automatically when landing. Has protection against accidental triggering, so the wings do not always close. If False you must close the wings manually." )
	public static Boolean foldWingsOnLand = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "alternateFlight", comment = "Whether to use flight similar to creative rather then gliding." )
	public static Boolean creativeFlight = false;

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "wings", key = "flightSpinCooldown", comment = "The cooldown in seconds in between uses of the spin attack in flight" )
	public static Integer flightSpinCooldown = 5;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "healthMod", comment = "Apply a health modifier for dragons. The older the dragon, the more health it has." )
	public static Boolean healthAdjustments = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "bonuses", comment = "Set to false to toggle off all dragon bonuses and play as human." )
	public static Boolean bonuses = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "attackMod", comment = "Apply an attack damage modifier for dragons." )
	public static Boolean attackDamage = true;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "babyAttackMod", comment = "Attack modifier for baby dragons." )
	public static Double babyBonusDamage = 1.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "youngAttackMod", comment = "Attack modifier for young dragons." )
	public static Double youngBonusDamage = 2.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "adultAttackMod", comment = "Attack modifier for adult dragons." )
	public static Double adultBonusDamage = 3.0;

	@ConfigRange( min = 0.0, max = 0.9 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "newbornJump", comment = "Jumping height for a newborn dragon. Default is 1 block." )
	public static Double newbornJump = 0.025;

	@ConfigRange( min = 0.0, max = 0.9 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "youngJump", comment = "Jumping height for a young dragon. Default is 1.5 block." )
	public static Double youngJump = 0.1;

	@ConfigRange( min = 0.0, max = 0.9 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "adultJump", comment = "Jumping height for a adult dragon. Default is 2 block." )
	public static Double adultJump = 0.15;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "clawsAreTools", comment = "Whether dragon claws function as tools." )
	public static Boolean clawsAreTools = true;

	@ConfigRange( min = -1, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "baseHarvestLevel", comment = "The harvest level to apply when dragons breaks a block, regardless of dragon/tool type." )
	public static Integer baseHarvestLevel = 0;

	@ConfigRange( min = -1, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "bonusHarvestLevel", comment = "The harvest level to apply to a dragons specific tool type once unlocked." )
	public static Integer bonusHarvestLevel = 1;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "bonusUnlockedAt", comment = "The stage that dragons unlock the bonus harvest level." )
	public static DragonLevel bonusUnlockedAt = DragonLevel.YOUNG;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "bonuses", key = "speedupEffectLevel", comment = "The speed effect level for dragon block-specific speedups. Set to 0 to disable." )
	public static Integer speedupEffectLevel = 2;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "cave"}, key = "fireImmunity", comment = "Whether cave dragons are immune to fire damage types." )
	public static Boolean caveFireImmunity = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "cave"}, key = "lavaSwimming", comment = "Set to false to disable cave dragon fast lava swimming." )
	public static Boolean caveLavaSwimming = true;

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "cave"}, key = "lavaSwimTicks", comment = "The maximum number of ticks a cave dragon can swim in lava. Set to 0 to allow unlimited air while under lava." )
	public static Integer caveLavaSwimmingTicks = 3600;

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "cave"}, key = "caveSpeedupBlocks", comment = "Blocks cave dragons gain speed when standing above. Formatting: block/modid:id" )
	public static List<String> caveSpeedupBlocks = List.of("minecraft:base_stone_nether", "minecraft:base_stone_overworld", "minecraft:stone_bricks", "minecraft:beacon_base_blocks", "forge:cobblestone", "forge:sandstone", "forge:stone", "forge:ores", "quark:deepslate", "quark:deepslate_bricks", "quark:cobbled_deepslate");


	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "forest"}, key = "fallReduction", comment = "How many blocks of fall damage is mitigated for forest dragons. Set to 0.0 to disable." )
	public static Double forestFallReduction = 5.0;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "forest"}, key = "bushImmunity", comment = "Whether forest dragons are immune to Sweet Berry Bush damage." )
	public static Boolean forestBushImmunity = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "forest"}, key = "cactiImmunity", comment = "Whether forest dragons are immune to Cactus damage." )
	public static Boolean forestCactiImmunity = true;

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "forest"}, key = "forestSpeedupBlocks", comment = "Blocks forest dragons gain speed when standing above. Formatting: block/modid:id" )
	public static List<String> forestSpeedupBlocks = List.of("minecraft:logs", "minecraft:leaves", "minecraft:planks", "forge:dirt");


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "sea"}, key = "waterBonuses", comment = "Whether sea dragons gain bonus swim speed and unlimited air." )
	public static Boolean seaSwimmingBonuses = true;

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"bonuses", "sea"}, key = "seaSpeedupBlocks", comment = "Blocks sea dragons gain speed when standing above. Formatting: block/modid:id" )
	public static List<String> seaSpeedupBlocks = List.of("minecraft:ice", "minecraft:impermeable", "minecraft:sand", "minecraft:coral_blocks", "forge:sand", "minecraft:dirt_path", "minecraft:sandstone", "minecraft:cut_sandstone", "minecraft:chiseled_sandstone", "minecraft:smooth_sandstone", "minecraft:red_sandstone", "minecraft:cut_red_sandstone", "minecraft:chiseled_red_sandstone", "minecraft:smooth_red_sandstone", "minecraft:water");

	//Dragon Penalties
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "penalties", key = "penalties", comment = "Set to false to toggle off all dragon penalties." )
	public static Boolean penalties = true;

	@ConfigType(EntityType.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "penalties", key = "allowedVehicles", comment = "List of rideable entities. Format: modid:id" )
	public static List<String> allowedVehicles = List.of("minecraft:boat");

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "penalties", key = "limitedRiding", comment = "Should dragons be limited by which entities they can ride" )
	public static Boolean ridingBlacklist = true;

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "penalties", key = "blacklistedItems", comment = "List of items that disallowed to be used by dragons. Format: item/modid:id" )
	public static List<String> blacklistedItems = List.of("item:minecraft:bow", "item:spartanshields:shield_basic_nickel", "item:spartanshields:shield_basic_invar", "item:spartanshields:shield_basic_constantan", "item:spartanshields:shield_basic_platinum", "item:spartanshields:shield_mekanism_refined_glowstone", "item:spartanshields:shield_tower_wood", "item:spartanshields:shield_tower_stone", "item:spartanshields:shield_tower_iron", "item:spartanshields:shield_tower_gold", "item:spartanshields:shield_tower_diamond", "item:spartanshields:shield_tower_netherite", "item:spartanshields:shield_tower_obsidian", "item:spartanshields:shield_tower_copper", "item:spartanshields:shield_tower_tin", "item:spartanshields:shield_tower_bronze", "item:spartanshields:shield_tower_steel", "item:spartanshields:shield_tower_silver", "item:spartanshields:shield_tower_lead", "item:spartanshields:shield_tower_nickel", "item:spartanshields:shield_tower_constantan", "item:spartanshields:shield_tower_invar", "item:spartanshields:shield_tower_platinum", "item:spartanshields:shield_tower_electrum", "item:spartanshields:shield_mekanism_powered_ultimate", "item:quark:flamerang", "item:quark:pickarang", "item:spartanshields:shield_botania_manasteel", "item:spartanshields:shield_botania_elementium", "item:spartanshields:shield_mekanism_osmium", "item:spartanshields:shield_mekanism_lapis_lazuli", "item:spartanshields:shield_basic_electrum", "item:spartanshields:shield_mekanism_refined_obsidian", "item:spartanshields:shield_mekanism_powered_basic", "item:spartanshields:shield_mekanism_powered_advanced", "item:spartanshields:shield_mekanism_powered_elite", "item:spartanweaponry:boomerang_steel", "item:spartanweaponry:boomerang_invar", "item:spartanweaponry:boomerang_platinum", "item:spartanweaponry:boomerang_electrum", "item:spartanshields:shield_basic_bronze", "item:spartanshields:shield_basic_tin", "item:spartanshields:shield_basic_copper", "item:spartanshields:shield_basic_obsidian", "item:spartanshields:shield_basic_netherite", "item:spartanshields:shield_basic_diamond", "item:spartanshields:shield_basic_gold", "item:spartanshields:shield_basic_iron", "item:spartanshields:shield_basic_stone", "item:spartanshields:shield_basic_wood", "item:spartanweaponry:boomerang_lead", "item:spartanweaponry:boomerang_nickel", "item:spartanshields:shield_basic_steel", "item:spartanshields:shield_basic_silver", "item:spartanshields:shield_basic_lead", "item:spartanweaponry:boomerang_bronze", "item:spartanweaponry:boomerang_tin", "item:spartanweaponry:boomerang_copper", "item:spartanweaponry:boomerang_netherite", "item:spartanweaponry:boomerang_gold", "item:spartanweaponry:boomerang_iron", "item:spartanweaponry:boomerang_stone", "item:spartanweaponry:heavy_crossbow_bronze", "mowziesmobs:wrought_axe", "item:spartanshields:shield_botania_terrasteel", "item:spartanweaponry:heavy_crossbow_leather", "item:spartanweaponry:heavy_crossbow_iron", "item:spartanweaponry:heavy_crossbow_gold", "item:spartanweaponry:heavy_crossbow_diamond", "item:spartanweaponry:heavy_crossbow_netherite", "item:spartanweaponry:heavy_crossbow_copper", "item:spartanweaponry:heavy_crossbow_tin", "item:spartanweaponry:boomerang_wood", "item:nethers_exoticism:rambutan_shield", "item:minecraft:shield", "item:minecraft:trident", "item:spartanweaponry:heavy_crossbow_lead", "item:spartanweaponry:heavy_crossbow_nickel", "item:spartanweaponry:heavy_crossbow_electrum", "item:spartanweaponry:heavy_crossbow_platinum", "item:spartanweaponry:heavy_crossbow_invar", "item:spartanweaponry:heavy_crossbow_silver", "item:spartanweaponry:heavy_crossbow_steel", "item:spartanweaponry:boomerang_diamond", "item:spartanweaponry:heavy_crossbow_wood", "item:minecraft:crossbow", "item:aquaculture:neptunium_bow", "item:spartanweaponry:longbow_electrum", "item:spartanweaponry:longbow_invar", "item:infernalexp:glowsilk_bow", "item:spartanweaponry:longbow_wood", "item:spartanweaponry:longbow_leather", "item:spartanweaponry:longbow_silver", "item:spartanweaponry:longbow_steel", "item:spartanweaponry:longbow_bronze", "item:spartanweaponry:longbow_tin", "item:spartanweaponry:longbow_copper", "item:spartanweaponry:longbow_netherite", "item:spartanweaponry:longbow_diamond", "item:spartanweaponry:longbow_gold", "item:spartanweaponry:longbow_iron", "item:spartanweaponry:boomerang_diamond", "item:spartanweaponry:boomerang_iron", "item:spartanweaponry:boomerang_wood", "item:spartanweaponry:boomerang_gold", "item:spartanweaponry:boomerang_netherite", "item:spartanweaponry:boomerang_copper", "item:spartanweaponry:boomerang_tin", "item:spartanweaponry:boomerang_bronze", "item:spartanweaponry:boomerang_stone", "item:spartanweaponry:boomerang_platinum", "item:spartanweaponry:boomerang_electrum", "item:spartanweaponry:boomerang_steel", "item:spartanweaponry:boomerang_lead", "item:spartanweaponry:boomerang_invar", "item:spartanweaponry:boomerang_nickel");

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "penalties", key = "blacklistedSlots", comment = "List of slots to handle blacklistedItems option" )
	public static List<Integer> blacklistedSlots = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 45);

	// Cave Dragon Penalties
	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"penalties", "cave"}, key = "waterDamage", comment = "The amount of damage taken per water damage tick (once every 10 ticks). Set to 0.0 to disable water damage." )
	public static Double caveWaterDamage = 1.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category =  {"penalties", "cave"}, key = "rainDamage", comment = "The amount of damage taken per rain damage tick (once every 40 ticks). Set to 0.0 to disable rain damage." )
	public static Double caveRainDamage = 1.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "cave"}, key = "splashDamage", comment = "The amount of damage taken when hit with a snowball or a water bottle. Set to 0.0 to disable splash damage." )
	public static Double caveSplashDamage = 2.0;

	// Forest Dragon Penalties
	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "forest"}, key = "ticksBeforeStressed", comment = "The number of ticks in darkness before the forest dragon gets Stress effect. Set to 0 to disable to stress effect." )
	public static Integer forestStressTicks = 70;

	@ConfigRange( min = 2, max = 100000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "forest"}, key = "stressEffectDuration", comment = "The number of seconds the stress effect lasts for." )
	public static Integer forestStressEffectDuration = 50;

	@ConfigRange( min = 0.1, max = 4.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "forest"}, key = "stressExhaustion", comment = "The amount of exhaustion applied per 10 ticks during the stress effect." )
	public static Double stressExhaustion = 1.0;

	// Sea Dragon Penalties

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "sea"}, key = "ticksWithoutWater", comment = "The number of ticks out of water before the sea dragon will start taking dehydration damage. Set to 0 to disable. Note: This value can stack up to double while dehydrated." )
	public static Integer seaTicksWithoutWater = 1200;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "sea"}, key = "waterConsumptionDependsOnTemperature", comment = "Whether the sea dragon should lose more water in warmer biomes and less during the night." )
	public static Boolean seaTicksBasedOnTemperature = true;

	@ConfigRange( min = 0.5, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "sea"}, key = "dehydrationDamage", comment = "The amount of damage taken per tick while dehydrated (once every 40 ticks unless fully dehydrated, then once every 20 ticks)." )
	public static Double seaDehydrationDamage = 1.0;

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "sea"}, key = "seaHydrationBlocks", comment = "When sea dragons stand on these blocks, hydration is restored. Format: block/modid:id" )
	public static List<String> seaHydrationBlocks = List.of("minecraft:ice", "minecraft:snow", "minecraft:snow_block", "dragonsurvival:sea_source_of_magic");

	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "sea"}, key = "allowWaterBottles", comment = "Set to false to disable sea dragons using vanilla water bottles to avoid dehydration." )
	public static Boolean seaAllowWaterBottles = true;

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "sea"}, key = "waterItemRestorationTicks", comment = "How many ticks do water restoration items restore when used. Set to 0 to disable." )
	public static Integer seaTicksWithoutWaterRestored = 5000;

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category  = {"penalties", "sea"}, key = "seaHydrationItems", comment = "Additional modded USEABLE items that restore water when used (called from LivingEntityUseItemEvent.Finish). Format: item/modid:id" )
	public static List<String> seaAdditionalWaterUseables = List.of("minecraft:enchanted_golden_apple");

	// Ore Loot
	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "ore", key = "humanOreDustChance", comment = "The odds of dust dropping when a human harvests an ore." )
	public static Double humanOreDustChance = 0.1;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "ore", key = "dragonOreDustChance", comment = "The odds of dust dropping when a dragon harvests an ore." )
	public static Double dragonOreDustChance = 0.2;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "ore", key = "humanOreBoneChance", comment = "The odds of a bone dropping when a human harvests an ore." )
	public static Double humanOreBoneChance = 0.0;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "ore", key = "dragonOreBoneChance", comment = "The odds of a bone dropping when a dragon harvests an ore." )
	public static Double dragonOreBoneChance = 0.01;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "ore", key = "oresTag", comment = "The tag that contains all ores that can drop dust/bones when harvested. Will not drop if the ore drops another of the items in this tag. Format: modid:id" )
	public static String oresTag = "forge:ores";

	// Food general
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "dragonFoods", comment = "Force dragons to eat a unique diet for their type." )
	public static Boolean customDragonFoods = true;

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "hurtfulToCaveDragon", comment = "Items which will cause damage to cave dragons when consumed. Formatting: item/modid:itemid:damage" )
	public static List<String> caveDragonHurtfulItems = Arrays.asList("item:minecraft:potion:2", "item:minecraft:water_bottle:2", "item:minecraft:milk_bucket:2");

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "hurtfulToSeaDragon", comment = "Items which will cause damage to sea dragons when consumed. Formatting: item/modid:itemid:damage" )
	public static List<String> seaDragonHurtfulItems = Collections.emptyList();

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "hurtfulToForestDragon", comment = "Items which will cause damage to forest dragons when consumed. Formatting: item/modid:itemid:damage" )
	public static List<String> forestDragonHurtfulItems = Collections.emptyList();

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "chargedSoupBuffDuration", comment = "How long in seconds should the cave fire effect from charged soup last. (Default to 5min) Set to 0 to disable." )
	public static Integer chargedSoupBuffDuration = 300;

	// Dragon Food List
	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "caveDragon", comment = {"Dragon food formatting: item/modid:id:food:saturation", "Dragon food formatting: item/modid:id:food:saturation. Food/saturation values are optional as the human values will be used if missing."} )
	public static List<String> caveDragonFoods = Arrays.asList("minecraft:coals:1:1", "item:minecraft:charcoal:1:2", "item:minecraft:golden_apple", "item:minecraft:enchanted_golden_apple", "item:dragonsurvival:charged_coal:6:1", "item:dragonsurvival:charred_meat:10:12", "item:dragonsurvival:cave_dragon_treat:14:12", "item:dragonsurvival:charred_seafood:8:10", "item:dragonsurvival:charred_vegetable:8:9", "item:dragonsurvival:charred_mushroom:8:5", "item:dragonsurvival:charged_soup:20:15", "item:desolation:cinder_fruit:6:7", "item:desolation:powered_cinder_fruit:8:12", "item:desolation:activatedcharcoal:2:2", "item:desolation:infused_powder:10:10", "item:desolation:primed_ash:7:8", "item:pickletweaks:diamond_apple", "item:pickletweaks:emerald_apple", "item:undergarden:ditchbulb:5,6", "item:xreliquary:molten_core:1:1", "item:silents_mechanisms:coal_generator_fuels:1:1", "item:mekanism:dust_charcoal:1:1", "item:mekanism:dust_coal:1:1", "item:rats:nether_cheese", "item:potionsmaster:charcoal_powder:1:1", "item:potionsmaster:coal_powder:1:1", "item:potionsmaster:activated_charcoal:2:2", "item:thermal:coal_coke:1:1", "item:infernalexp:glowcoal:2:3", "item:resourcefulbees:coal_honeycomb:5:5", "item:resourcefulbees:netherite_honeycomb:5:5", "item:lazierae2:coal_dust:1:1", "item:wyrmroost:jewelled_apple", "item:silents_mechanisms:coal_dust:1:1", "item:potionsmaster:calcinatedcoal_powder:1:1", "item:thermal:basalz_rod:2:4", "item:thermal:basalz_powder:1:2", "item:druidcraft:fiery_glass:2:2");

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "forestDragon", comment = {"Dragon food formatting: item/modid:id:food:saturation", "Dragon food formatting: item/modid:id:food:saturation. Food/saturation values are optional as the human values will be used if missing."} )
	public static List<String> forestDragonFoods = Arrays.asList("forge:raw_meats:5:7", "item:minecraft:sweet_berries:1:1", "item:minecraft:rotten_flesh:2:3", "item:minecraft:spider_eye:7:8", "item:minecraft:rabbit:7:13", "item:minecraft:poisonous_potato:7:10", "item:minecraft:chorus_fruit:9:12", "item:minecraft:golden_apple", "item:minecraft:enchanted_golden_apple", "item:minecraft:honey_bottle", "item:dragonsurvival:forest_dragon_treat:10:12", "item:aoa3:fiery_chops:6:7", "item:aoa3:raw_chimera_chop:6:7", "item:aoa3:raw_furlion_chop:6:7", "item:aoa3:raw_halycon_beef:7:8", "item:aoa3:raw_charger_shank:6:7", "item:aoa3:trilliad_leaves:8:11", "item:aoa3:heart_fruit:9:10", "item:pamhc2foodextended:rawtofabbititem", "item:pamhc2foodextended:rawtofickenitem", "item:pamhc2foodextended:rawtofuttonitem", "item:alexsmobs:kangaroo_meat:5:6", "item:alexsmobs:moose_ribs:6:8", "item:simplefarming:raw_horse_meat:5:6", "item:simplefarming:raw_bacon:3:3", "item:simplefarming:raw_chicken_wings:2:3", "item:simplefarming:raw_sausage:3:4", "item:xenoclustwo:raw_tortice:7:8", "item:unnamedanimalmod:musk_ox_shank:7:8", "item:unnamedanimalmod:frog_legs:5:6", "item:unnamedanimalmod:mangrove_fruit:4:7", "item:betteranimalsplus:venisonraw:5:6", "item:betteranimalsplus:pheasantraw:7:5", "item:betteranimalsplus:turkey_leg_raw:4:5", "item:infernalexp:raw_hogchop:6:7", "item:infernalexp:cured_jerky:10:7", "item:druidcraft:elderberries:3:4", "item:rats:raw_rat:4:5", "item:aquaculture:frog:4:5", "item:aquaculture:frog_legs_raw:4:4", "item:aquaculture:box_turtle:4:5", "item:aquaculture:arrau_turtle:4:5", "item:aquaculture:starshell_turtle:4:5", "item:nethers_exoticism:kiwano:3:4", "item:undergarden:raw_gloomper_leg:4:5", "item:undergarden:raw_dweller_meat:6:7", "item:farmersdelight:chicken_cuts:3:3", "item:farmersdelight:bacon:3:3", "item:farmersdelight:ham:9:10", "item:farmersdelight:minced_beef:5:3", "item:farmersdelight:mutton_chops:5:3", "item:abnormals_delight:duck_fillet:2:3", "item:abnormals_delight:venison_shanks:7:3", "item:pickletweaks:diamond_apple", "item:pickletweaks:emerald_apple", "item:autumnity:foul_berries:2:4", "item:autumnity:turkey:7:8", "item:autumnity:turkey_piece:2:4", "item:autumnity:foul_soup:12:8", "item:endergetic:bolloom_fruit:3:4", "item:quark:frog_leg:4:5", "item:nethers_delight:hoglin_loin:8:6", "item:nethers_delight:raw_stuffed_hoglin:18:10", "item:xreliquary:zombie_heart:4:7", "item:xreliquary:bat_wing:2:2", "item:eidolon:zombie_heart:7:7", "item:forbidden_arcanus:bat_wing:5:2", "item:twilightforest:raw_venison:5:5", "item:twilightforest:raw_meef:9:5", "item:twilightforest:hydra_chop", "item:cyclic:chorus_flight", "item:cyclic:chorus_spectral", "item:cyclic:apple_ender", "item:cyclic:apple_honey", "item:cyclic:apple_chorus", "item:cyclic:apple_bone", "item:cyclic:apple_prismarine", "item:cyclic:apple_lapis", "item:cyclic:apple_iron", "item:cyclic:apple_diamond", "item:cyclic:apple_emerald", "item:cyclic:apple_chocolate", "item:cyclic:toxic_carrot:15:15", "item:artifacts:everlasting_beef", "item:resourcefulbees:rainbow_honey_bottle", "item:resourcefulbees:diamond_honeycomb:5:5", "item:byg:soul_shroom:9:5", "item:byg:death_cap:9:8", "item:byg:holly_berries:2:2", "item:minecolonies:chorus_bread", "item:wyrmroost:jewelled_apple", "item:wyrmroost:raw_lowtier_meat:3:2", "item:wyrmroost:raw_common_meat:5:3", "item:wyrmroost:raw_apex_meat:8:6", "item:wyrmroost:raw_behemoth_meat:11:12", "item:wyrmroost:desert_wyrm:4:3", "item:eanimod:rawchicken_darkbig:9:5", "item:eanimod:rawchicken_dark:5:4", "item:eanimod:rawchicken_darksmall:3:2", "item:eanimod:rawchicken_pale:5:3", "item:eanimod:rawchicken_palesmall:4:3", "item:eanimod:rawrabbit_small:4:4", "item:environmental:duck:4:3", "item:environmental:venison:7:7", "item:cnb:lizard_item_0:4:4", "item:cnb:lizard_item_1:4:4", "item:cnb:lizard_item_2:4:4", "item:cnb:lizard_item_3:4:4", "item:snowpig:frozen_porkchop:7:3", "item:snowpig:frozen_ham:5:7", "item:untamedwilds:snake_grass_snake:4:4", "item:untamedwilds:snake_green_mamba:4:4", "item:untamedwilds:snake_rattlesnake:4:4", "item:untamedwilds:snake_emerald:4:4", "item:untamedwilds:snake_carpet_python:4:4", "item:untamedwilds:snake_corn:4:4", "item:untamedwilds:snake_gray_kingsnake:4:4", "item:untamedwilds:snake_coral:4:4", "item:untamedwilds:snake_ball_python:4:4", "item:untamedwilds:snake_black_mamba:4:4", "item:untamedwilds:snake_western_rattlesnake:4:4", "item:untamedwilds:snake_taipan:4:4", "item:untamedwilds:snake_adder:4:4", "item:untamedwilds:snake_rice_paddy:4:4", "item:untamedwilds:snake_coral_blue:4:4", "item:untamedwilds:snake_cave_racer:4:4", "item:untamedwilds:snake_swamp_moccasin:4:4", "item:untamedwilds:softshell_turtle_pig_nose:4:4", "item:untamedwilds:softshell_turtle_flapshell:4:4", "item:untamedwilds:softshell_turtle_chinese:4:4", "item:untamedwilds:tortoise_asian_box:4:4", "item:untamedwilds:tortoise_gopher:4:4", "item:untamedwilds:tortoise_leopard:4:4", "item:untamedwilds:softshell_turtle_peacock:4:4", "item:untamedwilds:softshell_turtle_nile:4:4", "item:untamedwilds:softshell_turtle_spiny:4:4", "item:untamedwilds:tortoise_sulcata:4:4", "item:untamedwilds:tortoise_star:4:4", "item:untamedwilds:tortoise_marginated:4:4", "item:leescreatures:raw_boarlin:6:6", "item:mysticalworld:venison:5:5", "item:toadterror:toad_chops:8:7", "item:prehistoricfauna:raw_large_thyreophoran_meat:7:6", "item:prehistoricfauna:raw_large_marginocephalian_meat:8:6", "item:prehistoricfauna:raw_small_ornithischian_meat:4:3", "item:prehistoricfauna:raw_large_sauropod_meat:11:9", "item:prehistoricfauna:raw_small_sauropod_meat:4:4", "item:prehistoricfauna:raw_large_theropod_meat:7:7", "item:prehistoricfauna:raw_small_theropod_meat:4:4", "item:prehistoricfauna:raw_small_archosauromorph_meat:3:3", "item:prehistoricfauna:raw_large_archosauromorph_meat:6:5", "item:prehistoricfauna:raw_small_reptile_meat:4:3", "item:prehistoricfauna:raw_large_synapsid_meat:5:6");

	@ConfigType(Item.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "food", key = "seaDragon", comment = {"Dragon food formatting: item/modid:id:food:saturation", "Dragon food formatting: item/modid:id:food:saturation. Food/saturation values are optional as the human values will be used if missing."} )
	public static List<String> seaDragonFoods = Arrays.asList("forge:raw_fishes:6:7", "item:minecraft:dried_kelp:1:1", "item:minecraft:kelp:2:3", "item:minecraft:pufferfish:10:15", "item:minecraft:golden_apple", "item:minecraft:enchanted_golden_apple", "item:minecraft:honey_bottle", "item:dragonsurvival:sea_dragon_treat:10:12", "item:aoa3:raw_candlefish:9:9", "item:aoa3:raw_crimson_skipper:8:8", "item:aoa3:raw_fingerfish:4:4", "item:aoa3:raw_pearl_stripefish:5:4", "item:aoa3:raw_limefish:5:5", "item:aoa3:raw_sailback:6:5", "item:aoa3:raw_golden_gullfish:10:2", "item:aoa3:raw_turquoise_stripefish:7:6", "item:aoa3:raw_violet_skipper:7:7", "item:aoa3:raw_rocketfish:4:10", "item:aoa3:raw_crimson_stripefish:8:7", "item:aoa3:raw_sapphire_strider:9:8", "item:aoa3:raw_dark_hatchetfish:9:9", "item:aoa3:raw_ironback:10:9", "item:aoa3:raw_rainbowfish:11:11", "item:aoa3:raw_razorfish:12:14", "item:quark:golden_frog_leg", "item:alexsmobs:lobster_tail:4:5", "item:alexsmobs:blobfish:8:9", "item:oddwatermobs:raw_ghost_shark:8:8", "item:oddwatermobs:raw_isopod:4:2", "item:oddwatermobs:raw_mudskipper:6:7", "item:oddwatermobs:raw_coelacanth:9:10", "item:oddwatermobs:raw_anglerfish:6:6", "item:oddwatermobs:deep_sea_fish:4:2", "item:oddwatermobs:crab_leg:5:6", "item:simplefarming:raw_calamari:5:6", "item:unnamedanimalmod:elephantnose_fish:5:6", "item:unnamedanimalmod:flashlight_fish:5:6", "item:unnamedanimalmod:rocket_killifish:5:6", "item:unnamedanimalmod:leafy_seadragon:5:6", "item:unnamedanimalmod:elephantnose_fish:5:6", "item:betteranimalsplus:eel_meat_raw:5:6", "item:betteranimalsplus:calamari_raw:4:5", "item:betteranimalsplus:crab_meat_raw:4:4", "item:aquaculture:fish_fillet_raw:2:2", "item:aquaculture:goldfish:8:4", "item:aquaculture:box_turtle:4:5", "item:aquaculture:arrau_turtle:4:5", "item:aquaculture:starshell_turtle:4:5", "item:aquaculture:algae:3:2", "item:betterendforge:end_fish_raw:6:7", "item:betterendforge:hydralux_petal:3:3", "item:betterendforge:charnia_green:2:2", "item:shroomed:raw_shroomfin:5:6", "item:undergarden:raw_gwibling:5:6", "item:pickletweaks:diamond_apple", "item:pickletweaks:emerald_apple", "item:bettas:betta_fish:4:5", "item:quark:crab_leg:4:4", "item:pamhc2foodextended:rawtofishitem", "item:fins:banded_redback_shrimp:6:1", "item:fins:night_light_squid:6:2", "item:fins:night_light_squid_tentacle:6:2", "item:fins:emerald_spindly_gem_crab:7:2", "item:fins:amber_spindly_gem_crab:7:2", "item:fins:rubby_spindly_gem_crab:7:2", "item:fins:sapphire_spindly_gem_crab:7:2", "item:fins:pearl_spindly_gem_crab:7:2", "item:fins:papa_wee:6:2", "item:fins:bugmeat:4:2", "item:fins:raw_golden_river_ray_wing:6:2", "item:fins:red_bull_crab_claw:4:4", "item:fins:white_bull_crab_claw:4:4", "item:fins:wherble_fin:1:1", "item:forbidden_arcanus:tentacle:5:2", "item:pneumaticcraft:raw_salmon_tempura:6:10", "item:rats:ratfish:4:2", "item:cyclic:chorus_flight", "item:cyclic:chorus_spectral", "item:cyclic:apple_ender", "item:cyclic:apple_honey", "item:cyclic:apple_chorus", "item:cyclic:apple_bone", "item:cyclic:apple_prismarine", "item:cyclic:apple_lapis", "item:cyclic:apple_iron", "item:cyclic:apple_diamond", "item:cyclic:apple_emerald", "item:cyclic:apple_chocolate", "item:upgrade_aquatic:purple_pickerelweed:2:2", "item:upgrade_aquatic:blue_pickerelweed:2:2", "item:upgrade_aquatic:polar_kelp:2:2", "item:upgrade_aquatic:tongue_kelp:2:2", "item:upgrade_aquatic:thorny_kelp:2:2", "item:upgrade_aquatic:ochre_kelp:2:2", "item:upgrade_aquatic:lionfish:8:9", "item:resourcefulbees:gold_honeycomb:5:5", "item:resourcefulbees:rainbow_honey_bottle", "item:wyrmroost:jewelled_apple", "item:aquaculture:sushi:6:5", "item:freshwarriors:fresh_soup:15:10", "item:freshwarriors:beluga_caviar:10:3", "item:freshwarriors:piranha:4:1", "item:freshwarriors:tilapia:4:1", "item:freshwarriors:stuffed_piranha:4:1", "item:freshwarriors:tigerfish:5:5", "item:freshwarriors:toe_biter_leg:3:3", "item:untamedwilds:egg_arowana_black:4:4", "item:untamedwilds:egg_trevally_jack:4:4", "item:untamedwilds:egg_trevally_golden:4:4", "item:untamedwilds:egg_giant_salamander_chinese:6:4", "item:untamedwilds:egg_giant_salamander_hellbender:6:4", "item:untamedwilds:egg_giant_salamander_japanese:6:4", "item:untamedwilds:giant_clam_gigas:4:4", "item:untamedwilds:giant_clam_derasa:4:4", "item:untamedwilds:giant_clam_maxima:4:4", "item:untamedwilds:giant_clam_squamosa:4:4", "item:untamedwilds:egg_trevally_giant:6:4", "item:untamedwilds:egg_trevally_bluespotted:6:4", "item:untamedwilds:egg_trevally_bigeye:6:4", "item:untamedwilds:egg_sunfish_southern:6:4", "item:untamedwilds:egg_sunfish_sunfish:6:4", "item:untamedwilds:egg_giant_clam_squamosa:6:4", "item:untamedwilds:egg_giant_clam_gigas:6:4", "item:untamedwilds:egg_giant_clam_derasa:6:4", "item:untamedwilds:egg_giant_clam_maxima:6:4", "item:untamedwilds:egg_football_fish_atlantic:6:4", "item:untamedwilds:egg_arowana_silver:6:4", "item:untamedwilds:egg_arowana_jardini:6:4", "item:untamedwilds:egg_arowana_green:6:4", "item:mysticalworld:raw_squid:6:5", "item:aquafina:fresh_soup:15:10", "item:aquafina:beluga_caviar:10:3", "item:aquafina:raw_piranha:4:1", "item:aquafina:raw_tilapia:4:1", "item:aquafina:stuffed_piranha:4:1", "item:aquafina:tigerfish:5:5", "item:aquafina:toe_biter_leg:3:3", "item:aquafina:raw_angelfish:4:1", "item:aquafina:raw_football_fish:4:1", "item:aquafina:raw_foxface_fish:4:1", "item:aquafina:raw_royal_gramma:4:1", "item:aquafina:raw_starfish:4:1", "item:aquafina:spider_crab_leg:4:1", "item:aquafina:raw_stingray_slice:4:1", "item:prehistoricfauna:raw_ceratodus:5:5", "item:prehistoricfauna:raw_cyclurus:4:4", "item:prehistoricfauna:raw_potamoceratodus:5:5", "item:prehistoricfauna:raw_myledaphus:4:4", "item:prehistoricfauna:raw_gar:4:4", "item:prehistoricfauna:raw_oyster:4:3", "item:prehistoric_delight:prehistoric_fillet:3:3");


	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "mana"}, key = "seaDragonManaBlocks", comment = "Blocks that will restore mana quicker when a sea dragon is standing on it. Formatting: block/modid:blockid" )
	public static List<String> seaDragonManaBlocks = List.of("dragonsurvival:sea_source_of_magic", "minecraft:ice", "minecraft:snow", "minecraft:snow_block", "minecraft:water", "minecraft:wet_sponge", "minecraft:cauldron");

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "mana"}, key = "forestDragonManaBlocks", comment = "Blocks that will restore mana quicker when a forest dragon is standing on it. Formatting: block/modid:blockid" )
	public static List<String> forestDragonManaBlocks = List.of("dragonsurvival:forest_source_of_magic", "minecraft:small_flowers", "minecraft:flowers", "minecraft:tall_flowers");

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "mana"}, key = "caveDragonManaBlocks", comment = "Blocks that will restore mana quicker when a cave dragon is standing on it. Formatting: block/modid:blockid" )
	public static List<String> caveDragonManaBlocks = List.of("dragonsurvival:cave_source_of_magic", "minecraft:campfires", "minecraft:lava", "minecraft:smoker", "minecraft:furnace", "minecraft:magma_block", "minecraft:blast_furnace");


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "dragonAbilities", comment = "Whether dragon abilities should be enabled" )
	public static Boolean dragonAbilities = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "caveDragonAbilities", comment = "Whether cave dragon abilities should be enabled" )
	public static Boolean caveDragonAbilities = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "forestDragonAbilities", comment = "Whether forest dragon abilities should be enabled" )
	public static Boolean forestDragonAbilities = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "seaDragonAbilities", comment = "Whether sea dragon abilities should be enabled" )
	public static Boolean seaDragonAbilities = true;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "noEXPRequirements", comment = "Disable the exp requirements for leveling up active skills" )
	public static Boolean noEXPRequirements = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "consumeEXPAsMana", comment = "Whether to use exp instead of mana if mana is empty" )
	public static Boolean consumeEXPAsMana = true;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "initialPassiveCost", comment = "The initial exp cost for leveling passive skills." )
	public static Integer initialPassiveCost = 2;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "passiveScalingCost", comment = "The multiplier that is used to increase the passive skill costs per level" )
	public static Double passiveScalingCost = 4.0;


	@ConfigRange( min = 1, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "favorableManaRegen", comment = "How fast in seconds should mana be recovered in favorable conditions" )
	public static Integer favorableManaTicks = 5;

	@ConfigRange( min = 1, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "magic", key = "normalManaRegen", comment = "How fast in seconds should mana be recovered in normal conditions" )
	public static Integer normalManaTicks = 15;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities"}, key = "saveAllAbilities", comment = "Whether to save passives skills when changing dragon type" )
	public static Boolean saveAllAbilities = false;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreath", comment = "Whether the forest breath ability should be enabled" )
	public static Boolean forestBreath = true;

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathDamage", comment = "The amount of damage the forest breath ability deals. This value is multiplied by the skill level." )
	public static Double forestBreathDamage = 2.0;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathCooldown", comment = "The cooldown in ticks of the forest breath ability" )
	public static Integer forestBreathCooldown = Functions.secondsToTicks(5);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathCasttime", comment = "The casttime in ticks of the forest breath ability" )
	public static Integer forestBreathCasttime = Functions.secondsToTicks(10);

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathInitialMana", comment = "The mana cost for starting the forest breath ability" )
	public static Integer forestBreathInitialMana = 2;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathOvertimeMana", comment = "The mana cost of sustaining the forest breath ability" )
	public static Integer forestBreathOvertimeMana = 1;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathManaTicks", comment = "How often in ticks, mana is consumed while using forest breath" )
	public static Integer forestBreathManaTicks = Functions.secondsToTicks(2);

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathBlockBreaks", comment = "Blocks that have a chance to be broken by forest breath. Formatting: block/modid:id" )
	public static List<String> forestBreathBlockBreaks = List.of("minecraft:banners");

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathGrowBlacklist", comment = "Blocks that will not be grown by the forest breath. Formatting: block/modid:id" )
	public static List<String> forestBreathGrowBlacklist = List.of();


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "spike"}, key = "spike", comment = "Whether the spike ability should be enabled" )
	public static Boolean spike = true;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "spike"}, key = "spikeCooldown", comment = "The cooldown in ticks of the spike ability" )
	public static Integer spikeCooldown = Functions.secondsToTicks(3);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "spike"}, key = "spikeCasttime", comment = "The cast time in ticks of the spike ability" )
	public static Integer spikeCasttime = 1;

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "spike"}, key = "spikeDamage", comment = "The amount of damage the spike ability deals. This value is multiplied by the skill level." )
	public static Double spikeDamage = 2.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "spike"}, key = "spikeManaCost", comment = "The mana cost for using the spike ability" )
	public static Integer spikeManaCost = 2;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspiration", comment = "Whether the inspiration ability should be enabled" )
	public static Boolean inspiration = true;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationCooldown", comment = "The cooldown in ticks of the inspiration ability" )
	public static Integer inspirationCooldown = Functions.secondsToTicks(90);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationCasttime", comment = "The cast time in ticks of the inspiration ability" )
	public static Integer inspirationCasttime = Functions.secondsToTicks(5);

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationDuration", comment = "The duration in seconds of the inspiration effect given when the ability is used" )
	public static Integer inspirationDuration = 60;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationManaCost", comment = "The mana cost for using the inspiration ability" )
	public static Integer inspirationManaCost = 3;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunter", comment = "Whether the hunter ability should be enabled" )
	public static Boolean hunter = true;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterDuration", comment = "The duration in seconds of the inspiration effect given when the ability is used" )
	public static Integer hunterDuration = 60;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterCooldown", comment = "The cooldown in ticks of the hunter ability" )
	public static Integer hunterCooldown = Functions.secondsToTicks(30);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterCasttime", comment = "The cast time in ticks of the hunter ability" )
	public static Integer hunterCasttime = Functions.secondsToTicks(3);

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterDamageBonus", comment = "The damage bonus the hunter effect gives when invisible. This value is multiplied by the skill level." )
	public static Double hunterDamageBonus = 1.5;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterManaCost", comment = "The mana cost for using the inspiration ability" )
	public static Integer hunterManaCost = 3;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "passives"}, key = "forestMagic", comment = "Whether the forest magic ability should be enabled" )
	public static Boolean forestMagic = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "passives"}, key = "forestAthletics", comment = "Whether the forest athletics ability should be enabled" )
	public static Boolean forestAthletics = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "passives"}, key = "lightInDarkness", comment = "Whether the light in darkness ability should be enabled" )
	public static Boolean lightInDarkness = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "forest_dragon", "passives"}, key = "cliffHanger", comment = "Whether the cliffhanger ability should be enabled" )
	public static Boolean cliffHanger = true;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreath", comment = "Whether the storm breath ability should be enabled" )
	public static Boolean stormBreath = true;

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathDamage", comment = "The amount of damage the storm breath ability deals. This value is multiplied by the skill level." )
	public static Double stormBreathDamage = 1.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathInitialMana", comment = "The mana cost for starting the storm breath ability" )
	public static Integer stormBreathInitialMana = 2;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathCooldown", comment = "The cooldown in ticks of the storm breath ability" )
	public static Integer stormBreathCooldown = Functions.secondsToTicks(10);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathCasttime", comment = "The cast time in ticks of the storm breath ability" )
	public static Integer stormBreathCasttime = 10;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathOvertimeMana", comment = "The mana cost of sustaining the storm breath ability" )
	public static Integer stormBreathOvertimeMana = 1;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathManaTicks", comment = "How often in ticks, mana is consumed while using storm breath" )
	public static Integer stormBreathManaTicks = Functions.secondsToTicks(2);

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathBlockBreaks", comment = "Blocks that have a chance to be broken by storm breath. Formatting: block/modid:id" )
	public static List<String> stormBreathBlockBreaks = List.of("minecraft:impermeable", "minecraft:snow", "minecraft:crops", "minecraft:flowers", "minecraft:banners", "minecraft:lush_plants_replaceable", "minecraft:azalea_log_replaceable", "minecraft:replaceable_plants", "minecraft:leaves");

	@ConfigType(EntityType.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "chargedBlacklist", comment = "List of entities that do not work with the charged effect. Format: modid:id" )
	public static List<String> chargedBlacklist = List.of("upgrade_aquatic:thrasher", "upgrade_aquatic:great_thrasher");

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "stormBreathChainCount", comment = "How many mobs stormbreath is able to chain to at once" )
	public static Integer stormBreathChainCount = 2;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "chargedEffectChainCount", comment = "How many mobs the charged effect is able to chain to at once" )
	public static Integer chargedEffectChainCount = 2;

	@ConfigRange( min = -1, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "chargedEffectMaxChain", comment = "How many times the charged effect is able to chain. -1 means it can chain infinitely" )
	public static Integer chargedEffectMaxChain = -1;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "chargedChainRange", comment = "The max distance in blocks the storm breath and charged effect is able to chain to mobs" )
	public static Integer chargedChainRange = 10;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "chargedEffectDamage", comment = "The amount of damage the charged effect deals each second" )
	public static Integer chargedEffectDamage = 1;

	@ConfigType(EntityType.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "storm_breath"}, key = "chargedSpreadBlacklist", comment = "List of entities that will not spread the charged effect. Format: modid:id" )
	public static List<String> chargedSpreadBlacklist = List.of("minecraft:armor_stand");


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightning", comment = "Whether the lightning ball ability should be enabled" )
	public static Boolean ballLightning = true;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningCooldown", comment = "The cooldown in ticks of the ball lightning ability" )
	public static Integer ballLightningCooldown = Functions.secondsToTicks(60);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningCasttime", comment = "The cast time in ticks of the ball lightning ability" )
	public static Integer ballLightningCasttime = Functions.secondsToTicks(2);

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningDamage", comment = "The amount of damage the lightning ball ability deals. This value is multiplied by the skill level." )
	public static Double ballLightningDamage = 4.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningManaCost", comment = "The mana cost for using the lightning ball ability" )
	public static Integer ballLightningManaCost = 3;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoul", comment = "Whether the revealing The Soul ability should be enabled" )
	public static Boolean revealingTheSoul = true;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulDuration", comment = "The duration in seconds of the revealing The Soul effect given when the ability is used" )
	public static Integer revealingTheSoulDuration = 60;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulCooldown", comment = "The cooldown in ticks of the revealing the soul ability" )
	public static Integer revealingTheSoulCooldown = Functions.secondsToTicks(30);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulCasttime", comment = "The cast time in ticks of the revealing the soul ability" )
	public static Integer revealingTheSoulCasttime = Functions.secondsToTicks(5);

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulManaCost", comment = "The mana cost for using the revealing The Soul ability" )
	public static Integer revealingTheSoulManaCost = 3;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulMaxEXP", comment = "The max amount of increased exp that can be gained from a single mob with reavling the soul" )
	public static Integer revealingTheSoulMaxEXP = 20;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulMultiplier", comment = "The multiplier that is applied to exp with revealing the soul, the extra exp is in addition to the normal drops. so 1.0 = 100% increase" )
	public static Double revealingTheSoulMultiplier = 1.0;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVision", comment = "Whether the sea vision ability should be enabled" )
	public static Boolean seaEyes = true;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVisionDuration", comment = "The duration in seconds of the sea vision effect given when the ability is used" )
	public static Integer seaEyesDuration = 90;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVisionCooldown", comment = "The cooldown in ticks of the sea vision ability" )
	public static Integer seaEyesCooldown = Functions.secondsToTicks(60);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaEyesCasttime", comment = "The cast time in ticks of the sea vision ability" )
	public static Integer seaEyesCasttime = Functions.secondsToTicks(2);

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVisionManaCost", comment = "The mana cost for using the sea vision ability" )
	public static Integer seaEyesManaCost = 2;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "passives"}, key = "seaMagic", comment = "Whether the sea magic ability should be enabled" )
	public static Boolean seaMagic = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "passives"}, key = "seaAthletics", comment = "Whether the sea athletics ability should be enabled" )
	public static Boolean seaAthletics = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "passives"}, key = "water", comment = "Whether the water ability should be enabled" )
	public static Boolean water = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "passives"}, key = "spectralImpact", comment = "Whether the spectralImpact ability should be enabled" )
	public static Boolean spectralImpact = true;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "sea_dragon", "passives"}, key = "spectralImpactProcChance", comment = "The percentage chance that spectral impact will proc. This is multiplied by the level of the skill." )
	public static Integer spectralImpactProcChance = 15;


	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreath", comment = "Whether the firebreath ability should be enabled" )
	public static Boolean fireBreath = true;

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathDamage", comment = "The amount of damage the firebreath ability deals. This value is multiplied by the skill level." )
	public static Double fireBreathDamage = 3.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathInitialMana", comment = "The mana cost for starting the firebreath ability" )
	public static Integer fireBreathInitialMana = 2;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathCooldown", comment = "The cooldown in ticks of the fire breath ability" )
	public static Integer fireBreathCooldown = Functions.secondsToTicks(5);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathCasttime", comment = "The cast time in ticks of the fire breath ability" )
	public static Integer fireBreathCasttime = 10;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathOvertimeMana", comment = "The mana cost of sustaining the firebreath ability" )
	public static Integer fireBreathOvertimeMana = 1;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathManaTicks", comment = "How often in ticks, mana is consumed while using fire breath" )
	public static Integer fireBreathManaTicks = Functions.secondsToTicks(2);

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathSpreadsFire", comment = "Whether the fire breath actually spreads fire when used" )
	public static Boolean fireBreathSpreadsFire = true;

	@ConfigType(Block.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathBlockBreaks", comment = "Blocks that have a chance to be broken by fire breath. Formatting: block/modid:id" )
	public static List<String> fireBreathBlockBreaks = List.of("minecraft:ice", "minecraft:snow", "minecraft:crops", "minecraft:leaves", "minecraft:flowers", "minecraft:banners", "minecraft:lush_plants_replaceable", "minecraft:azalea_log_replaceable", "minecraft:replaceable_plants", "minecraft:wooden_fences", "minecraft:logs_that_burn", "minecraft:mycelium", "minecraft:wooden_stairs", "minecraft:wooden_doors", "minecraft:wool", "minecraft:saplings", "minecraft:impermeable", "minecraft:cobweb", "minecraft:large_fern", "minecraft:sugar_cane", "minecraft:snow_block");

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fireball"}, key = "fireball", comment = "Whether the fireball ability should be enabled" )
	public static Boolean fireball = true;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fireball"}, key = "fireballCooldown", comment = "The cooldown in ticks of the fireball ability" )
	public static Integer fireballCooldown = Functions.secondsToTicks(40);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fireball"}, key = "fireballCasttime", comment = "The cast time in ticks of the fireball ability" )
	public static Integer fireballCasttime = Functions.secondsToTicks(4);

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fireball"}, key = "fireballDamage", comment = "The amount of damage the fireball ability deals. This value is multiplied by the skill level." )
	public static Double fireballDamage = 5.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fireball"}, key = "fireballManaCost", comment = "The mana cost for using the fireball ball ability" )
	public static Integer fireballManaCost = 3;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "tough_skin"}, key = "toughSkin", comment = "Whether the tough skin ability should be enabled" )
	public static Boolean toughSkin = true;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "tough_skin"}, key = "toughSkinDuration", comment = "The duration in seconds of the tough skin effect given when the ability is used" )
	public static Integer toughSkinDuration = 180;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "tough_skin"}, key = "toughSkinCooldown", comment = "The cooldown in ticks of the tough skin ability" )
	public static Integer toughSkinCooldown = Functions.secondsToTicks(30);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "tough_skin"}, key = "toughSkinCasttime", comment = "The cast time in ticks of the tough skin ability" )
	public static Integer toughSkinCasttime = Functions.secondsToTicks(5);

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "tough_skin"}, key = "toughSkinManaCost", comment = "The mana cost for using the tough skin ability" )
	public static Integer toughSkinManaCost = 3;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "tough_skin"}, key = "toughSkinArmorValue", comment = "The amount of extra armor given per level of tough skin effect" )
	public static Double toughSkinArmorValue = 3.0;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVision", comment = "Whether the lava vision ability should be enabled" )
	public static Boolean lavaVision = true;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionDuration", comment = "The duration in seconds of the lava vision effect given when the ability is used" )
	public static Integer lavaVisionDuration = 60;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionCooldown", comment = "The cooldown in ticks of the lava vision ability" )
	public static Integer lavaVisionCooldown = Functions.secondsToTicks(60);

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionCasttime", comment = "The cast time in ticks of the lava vision ability" )
	public static Integer lavaVisionCasttime = Functions.secondsToTicks(2);

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionManaCost", comment = "The mana cost for using the lava vision ability" )
	public static Integer lavaVisionManaCost = 2;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "passives"}, key = "caveMagic", comment = "Whether the cave magic ability should be enabled" )
	public static Boolean caveMagic = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "passives"}, key = "caveAthletics", comment = "Whether the cave athletics ability should be enabled" )
	public static Boolean caveAthletics = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "passives"}, key = "contrastShower", comment = "Whether the contrast shower ability should be enabled" )
	public static Boolean contrastShower = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"magic", "abilities", "cave_dragon", "passives"}, key = "burn", comment = "Whether the burn ability should be enabled" )
	public static Boolean burn = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "endVoidTeleport", comment = "Should the player be teleported to the overworld when they fall in the end?" )
	public static Boolean endVoidTeleport = true;
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "general", key = "elytraForDragon", comment = "Whether dragons are allowed to use Elytra" )
	public static Boolean dragonsAllowedToUseElytra = false;

	@ConfigRange( min = 1, max = 120 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "princessDespawnDelay", comment = "Princess or prince may despawn after this many minutes" )
	public static Integer princessDespawnDelay = 15;

	@ConfigRange( min = 1, max = 120 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "hunterDespawnDelay", comment = "Any dragon hunter may despawn after this many minutes" )
	public static Integer hunterDespawnDelay = 15;

	@ConfigRange( min = 10, max = 240 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "princessSpawnDelay", comment = "Minimum delay between prince or princess spawning, in minutes" )
	public static Integer princessSpawnDelay = 120;

	@ConfigRange( min = 12, max = 240 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "hunterGroupSpawnDelay", comment = "Minimum delay between Dragon hunter group spawning, in minutes" )
	public static Integer hunterSpawnDelay = 20;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "allowKnightSpawning", comment = "Dragon knight spawning enabled?" )
	public static Boolean spawnKnight = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "allowSquireSpawning", comment = "Dragon Squire spawning enabled?" )
	public static Boolean spawnSquire = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "allowHunterSpawning", comment = "Dragon Hunter spawning enabled?" )
	public static Boolean spawnHunter = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "allowHoundSpawning", comment = "Dragon Knight hound spawning enabled?" )
	public static Boolean spawnHound = true;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "allowPrinceAndPrincessSpawning", comment = "Princess and prince spawning enabled?" )
	public static Boolean spawnPrinceAndPrincess = true;

	@ConfigRange( min = 10, max = 1000 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "villagerKillxp", comment = "How many experience points are gained for killing a villager" )
	public static Integer xpGain = 10;

	@ConfigType(EntityType.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "evilDragonStatusGivers", comment = "Entities which give 'Evil dragon' status on death" )
	public static List<String> evilDragonStatusGivers = List.of("minecraft:villager", "dragonsurvival:hunter_hound", "dragonsurvival:knight", "dragonsurvival:shooter", "dragonsurvival:squire", "dragonsurvival:prince", "dragonsurvival:princess", "dragonsurvival:princess_entity");

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "preserveEvilDragonAfterDeath", comment = "Preserve effect 'Evil dragon' after death?" )
	public static Boolean preserveEvilDragonEffectAfterDeath = false;

	@ConfigRange( min = 6, max = 128 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "princessAndHuntersLowerSpawnBound", comment = "Lowest Y value allowed for princess and hunter spawning" )
	public static Integer riderSpawnLowerBound = 32;

	@ConfigRange( min = 64, max = 250 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_hunters", key = "princessAndHuntersUpperSpawnBound", comment = "Highest Y value allowed for princess and hunter spawning" )
	public static Integer riderSpawnUpperBound = 80;

	@ConfigRange( min = 10d, max = 80d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "knight"}, key = "knightHealth", comment = "Dragon Knight health" )
	public static Double knightHealth = 40d;

	@ConfigRange( min = 1d, max = 32d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "knight"}, key = "knightDamage", comment = "Dragon Knight base damage" )
	public static Double knightDamage = 12d;

	@ConfigRange( min = 0d, max = 30d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "knight"}, key = "knightArmor", comment = "Dragon Knight armor" )
	public static Double knightArmor = 10d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "knight"}, key = "knightSpeed", comment = "Dragon Knight speed" )
	public static Double knightSpeed = 0.35d;

	@ConfigRange( min = 0.0d, max = 1d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "knight"}, key = "knightShieldChance", comment = "Chance of having shield" )
	public static Double knightShieldChance = 0.1d;

	@ConfigRange( min = 8d, max = 40d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hound"}, key = "houndHealth", comment = "Knight Hound health" )
	public static Double houndHealth = 10d;

	@ConfigRange( min = 1d, max = 10d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hound"}, key = "houndDamage", comment = "Knight Hound damage" )
	public static Double houndDamage = 2d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hound"}, key = "houndSpeed", comment = "Knight Hound speed" )
	public static Double houndSpeed = 0.45d;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hound"}, key = "houndDoesSlowdown", comment = "Does Knight Hound apply speed slowdown?" )
	public static Boolean houndDoesSlowdown = true;

	@ConfigRange( min = 10d, max = 60d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterHealth", comment = "Dragon Hunter health" )
	public static Double hunterHealth = 24d;

	@ConfigRange( min = 2d, max = 20d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterDamage", comment = "Dragon Hunter damage" )
	public static Double hunterDamage = 5d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterSpeed", comment = "Dragon Hunter speed" )
	public static Double hunterSpeed = 0.35d;

	@ConfigRange( min = 0d, max = 20d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterArmor", comment = "Dragon Hunter armor" )
	public static Double hunterArmor = 0d;

	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterThrowsBolas", comment = "Is Dragon hunter able to throw a bolas?" )
	public static Boolean hunterHasBolas = true;

	@ConfigRange( min = 10d, max = 60d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "squire"}, key = "squireHealth", comment = "Dragon Squire health" )
	public static Double squireHealth = 24d;

	@ConfigRange( min = 2d, max = 20d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "squire"}, key = "squireDamage", comment = "Dragon Squire damage" )
	public static Double squireDamage = 6d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "squire"}, key = "squireSpeed", comment = "Dragon Squire speed" )
	public static Double squireSpeed = 0.35d;

	@ConfigRange( min = 0d, max = 20d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "squire"}, key = "squireArmor", comment = "Dragon Squire armor" )
	public static Double squireArmor = 2d;


	@ConfigRange( min = 10d, max = 60d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "prince"}, key = "princeHealth", comment = "Prince health" )
	public static Double princeHealth = 20d;

	@ConfigRange( min = 1d, max = 20d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "prince"}, key = "princeDamage", comment = "Prince base damage" )
	public static Double princeDamage = 1d;

	@ConfigRange( min = 0.2d, max = 0.6d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "prince"}, key = "princeSpeed", comment = "Prince speed" )
	public static Double princeSpeed = 0.3d;

	@ConfigRange( min = 0d, max = 20d )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = {"dragon_hunters", "prince"}, key = "princeArmor", comment = "Prince armor" )
	public static Double princeArmor = 6d;

	@ConfigRange( min = 1, max = 60 * 60 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_beacons", key = "constantEffect", comment = "Duration of effect given by beacon constantly in seconds" )
	public static Integer secondsOfBeaconEffect = 20;

	@ConfigRange( min = 1, max = 60 * 2 )
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_beacons", key = "temporaryEffect", comment = "Duration of effect given in exchange for experience in minutes" )
	public static Integer minutesOfDragonEffect = 10;

	@ConfigType(MobEffect.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_beacons", key = "peaceBeaconEffects", comment = "Effects of Peace beacon" )
	public static List<String> peaceBeaconEffects = List.of("dragonsurvival:peace", "dragonsurvival:animal_peace");

	@ConfigType(MobEffect.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_beacons", key = "magicBeaconEffects", comment = "Effects of Magic beacon" )
	public static List<String> magicBeaconEffects = List.of("dragonsurvival:magic", "dragonsurvival:predator_anti_spawn");

	@ConfigType(MobEffect.class)
	@ConfigOption( side = Dist.DEDICATED_SERVER, category = "dragon_beacons", key = "fireBeaconEffects", comment = "Effects of Fire beacon" )
	public static List<String> fireBeaconEffects = List.of("dragonsurvival:fire", "dragonsurvival:strong_leather");
}