package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.config.obj.*;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;


public class ServerConfig{
	public static final Double DEFAULT_MAX_GROWTH_SIZE = 60.0;
	ServerConfig(ModConfigSpec.Builder builder){
		ConfigHandler.addConfigs(builder, ConfigSide.SERVER);
	}

	@ConfigRange( min = 0, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "altarUsageCooldown", comment = "How long of a cooldown in seconds the altar has after each use." )
	public static Integer altarUsageCooldown = 0;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "altarCraftable", comment = "Whether dragon altars are craftable or not. When disabled you can only use the command or creative mode to become a dragon." )
	public static Boolean altarCraftable = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "keepClawItems", comment = "Whether to keep items in the claw slots on death otherwise they will drop on death." )
	public static Boolean keepClawItems = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "syncClawRender", comment = "If players are allowed to hide their claws and teeth from other players. If it is important to you to see your opponent's weapon during pvp, set false." )
	public static Boolean syncClawRender = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "canMoveInEmote", comment = "If players are allowed to move while performing emotes" )
	public static Boolean canMoveInEmote = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "canMoveWhileCasting", comment = "If you should be able to move while casting certain skills or if player movement can be prevented." )
	public static Boolean canMoveWhileCasting = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "startWithDragonChoice", comment = "Should the dragon altar interface be opened when the player first joins the world?" )
	public static Boolean startWithDragonChoice = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "allowDragonChoiceFromInventory", comment = "Should the dragon altar be accessible from the vanilla inventory if the player has not made a choice yet?" )
	public static Boolean allowDragonChoiceFromInventory = true;

	// Growth
	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "destructibleBlocks", comment = "Blocks that are destructible when block destruction is enabled. Blocks that can be harvested with an axe are also destroyable by default on whitelist mode. Formatting: block/modid:id" )
	public static List<String> destructibleBlocks = List.of(
			"minecraft:leaves",
			"minecraft:mineable/axe",
			"minecraft:flowers",
			"minecraft:replaceable"
	);

	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "useBlacklistForDestructibleBlocks", comment = "Use a blacklist for destructible blocks instead of a whitelist.")
	public static Boolean useBlacklistForDestructibleBlocks = false;

	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "allowLargeBlockDestruction", comment = "Allow a dragon to instantly destroy certain colliding blocks if they are above a certain size.")
	public static Boolean allowLargeBlockDestruction = false;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeBlockDestructionRemovePercentage", comment = "The percentage of blocks removed instead of destroyed when a dragon instantly destroys blocks. If a block is removed, it doesn't make a sound or particle effect. This is to minimize lag from particle VFX and audio spam.")
	public static Double largeBlockDestructionRemovePercentage = 0.96;

	@ConfigRange( min = 14.0, max = 1000000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeBlockDestructionSize", comment = "The size threshold for a dragon to start instantly destroying blocks. Crouching prevents destruction from occurring.")
	public static Double largeBlockDestructionSize = 120.0;

	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "allowCrushing", comment = "Allow a dragon to crush entities beneath it after being above a certain size. Crouching prevents crushing from occurring.")
	public static Boolean allowCrushing = false;

	@ConfigRange( min = 14.0, max = 1000000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushingSize", comment = "The size at which a dragon can begin to crush entities.")
	public static Double crushingSize = 120.0;

	@ConfigRange( min = 0.0, max = 20.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushingDamageScalar", comment = "The amount damage dealt per dragon size when crushing entities.")
	public static Double crushingDamageScalar = 0.05;

	@ConfigRange( min = 0, max = 20 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushingSize", comment = "The amount of ticks before entities can be crushed again after they were already crushed.")
	public static Integer crushingTickDelay = 20;

	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "allowLargeScaling", comment = "Allow a dragon's max health, damage, reach, step height, and jump height to continue to scale with growth beyond its normal limits.")
	public static Boolean allowLargeScaling = false;

	@ConfigRange( min = 1, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeMaxHealth", comment = "The maximum health when the dragon is at maximum growth size if large scaling is enabled.")
	public static Integer largeMaxHealth = 80;
	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeMovementSpeedScalar", comment = "The bonus movement speed multiplier per 60 size when the dragon is at maximum growth size if large scaling is enabled.")
	public static Double largeMovementSpeedScalar = 0.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeDamageBonus", comment = "The bonus damage when the dragon is at maximum growth size if large scaling is enabled.")
	public static Double largeDamageBonus = 6.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeReachScalar", comment = "The bonus reach given per 60 size once the dragon is above the default growth size of 60 if large scaling is enabled.")
	public static Double largeReachScalar = 0.5;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeStepHeightScalar", comment = "The bonus step height given per 60 size once the dragon is above the default growth size of 60 if large scaling is enabled.")
	public static Double largeStepHeightScalar = 1.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeStepHeightScalar", comment = "The bonus jump height given per 60 size once the dragon is above the default growth size of 60 if large scaling is enabled.")
	public static Double largeJumpHeightScalar = 0.15;

	@ConfigRange( min = 0.0, max = 10.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "largeBlockBreakRadiusScalar", comment = "The bonus block break radius given per 60 size once the dragon is above the default growth size of 60 if large scaling is enabled. A block radius of 0 disables this feature. Crouching allows you to mine one block at a time.")
	public static Double largeBlockBreakRadiusScalar = 0.7;

	@ConfigType(Item.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "growNewborn", comment = "List of items to grow newborn dragon. Format: item/modid:id" )
	public static List<String> growNewborn = List.of("dragonsurvival:heart_element", "dragonsurvival:weak_dragon_heart", "dragonsurvival:elder_dragon_heart");

	@ConfigType(Item.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "growYoung", comment = "List of items to grow young dragon. Format: item/modid:id" )
	public static List<String> growYoung = List.of("dragonsurvival:weak_dragon_heart", "dragonsurvival:elder_dragon_heart");

	@ConfigType(Item.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "growAdult", comment = "List of items to grow adult dragon. Format: item/modid:id" )
	public static List<String> growAdult = List.of("dragonsurvival:elder_dragon_heart");

	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "alternateGrowing", comment = "If true, dragons will grow without the use of catalyst grow items. Does not broker the use of items. Just an additional type of growth." )
	public static Boolean alternateGrowing = true;

	@ConfigRange( min = 14.0, max = 1000000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "maxGrowthSize", comment = "Defines the max size your dragon can grow to. Values that are too high can break your game. It is not advisable to set a number higher than 60." )
	public static Double maxGrowthSize = DEFAULT_MAX_GROWTH_SIZE;

	@ConfigRange( min = 0, max = 1000000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "moveSpeedNewborn", comment = "The movement speed multiplier for newborn dragons. Default is 1.0.")
	public static Double moveSpeedNewborn = 1.0;

	@ConfigRange( min = 0, max = 1000000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "moveSpeedYoung", comment = "The movement speed multiplier for young dragons. Default is 1.0.")
	public static Double moveSpeedYoung = 1.0;

	@ConfigRange( min = 0, max = 1000000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "moveSpeedAdult", comment = "The movement speed multiplier for adult dragons. Default is 1.0.")
	public static Double moveSpeedAdult = 1.0;

	@ConfigRange( min = 0, max = 1000000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "reachBonus", comment = "The bonus that is given to dragons at 60 size. The bonus gradually scales up to the maximum size. Human players have 1.0x reach and a size 60 dragon will have 1.5x distance with default values.")
	public static Double reachBonus = 0.5;

	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "saveGrowthStage", comment = "Should the growth stage of a dragon be saved even when you change. Does not affect the saving progress of magic (use saveAllAbilities). The author does not approve of weredragons, but if you insist..." )
	public static Boolean saveGrowthStage = false;

	@ConfigRange( min = 1, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "minHealth", comment = "Dragon starting health. Minimum health dragons will start off with." )
	public static Integer minHealth = 14;

	@ConfigRange( min = 1, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "maxHealth", comment = "The maximum health when the dragon is fully grown." )
	public static Integer maxHealth = 40;

	@ConfigRange( min = 1, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth"}, key = "maxHealthSize", comment = "The size at which the maximum health is reached." )
	public static Integer maxHealthSize = 40;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "newbornGrowthModifier", comment = "A multiplier to change the growth rate from newborn to young. At 1.0 it takes about 3 hours to turn a newborn dragon into a young dragon." )
	public static Double newbornGrowthModifier = 0.3;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "youngGrowthModifier", comment = "A multiplier to change the growth rate from young to adult. At 1.0 it takes about 1 day to turn a young dragon into a adult dragon." )
	public static Double youngGrowthModifier = 0.5;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "adultGrowthModifier", comment = "A multiplier to change the growth rate from adult to a full sized adult. At 1.0 it takes about 3 days to become a dragon of maximum adult size." )
	public static Double adultGrowthModifier = 0.9;

	@ConfigRange( min = 0.0, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "maxGrowthModifier", comment = "A multiplier to change the growth rate from full sized adult to max size. The change in growth after the maximum adult size is measured in months and years." )
	public static Double maxGrowthModifier = 1.0;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "dragonHeartShardChance", comment = "The chance for dragon heart shards to drop from any mobs with max health between 14-20" )
	public static Double dragonHeartShardChance = 0.03;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "weakDragonHeartChance", comment = "The chance for weak dragon heart to drop from any mobs with max health between 20-50" )
	public static Double weakDragonHeartChance = 0.01;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "elderDragonHeartChance", comment = "The chance for dragon heart to drop from any mobs with max health above 50" )
	public static Double elderDragonHeartChance = 0.01;

	@ConfigType(EntityType.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "dragonHeartEntityList", comment = "Decide which entities can drop dragon hearts" )
	public static List<String> dragonHeartEntityList = List.of();

	@ConfigType(EntityType.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "weakDragonHeartEntityList", comment = "Decide which entities can drop weak dragon hearts" )
	public static List<String> weakDragonHeartEntityList = List.of();

	@ConfigType(EntityType.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "elderDragonHeartEntityList", comment = "Decide which entities can drop elder dragon hearts" )
	public static List<String> elderDragonHeartEntityList = List.of();

	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "dragonHeartWhiteList", comment = "Should the dragonHeartEntityList be treated as an allowlist rather than a block list?" )
	public static Boolean dragonHeartWhiteList = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "weakDragonHeartWhiteList", comment = "Should the weakDragonHeartEntityList be treated as an allowlist rather than a block list?" )
	public static Boolean weakDragonHeartWhiteList = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "elderDragonHeartWhiteList", comment = "Should the elderDragonHeartEntityList be treated as an allowlist rather than a block list?" )
	public static Boolean elderDragonHeartWhiteList = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "dragonHeartUseList", comment = "Should the dragonHeartEntityList be used instead of the health requirement?" )
	public static Boolean dragonHeartUseList = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "weakDragonHeartUseList", comment = "Should the weakDragonHeartUseList be used instead of the health requirement?" )
	public static Boolean weakDragonHeartUseList = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "drops", key = "elderDragonHeartUseList", comment = "Should the elderDragonHeartUseList be used instead of the health requirement?" )
	public static Boolean elderDragonHeartUseList = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "treasure", key = "treasureHealthRegen", comment = "Whether sleeping on treasure will recover health or not. " )
	public static Boolean treasureHealthRegen = true;

	@ConfigRange( min = 1, max = 10000000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "treasure", key = "treasureRegenTicks", comment = "The time in seconds it takes to recover 1hp while sleeping on one treasure. A large number of treasures in one place reduces time." )
	public static Integer treasureRegenTicks = 241;

	@ConfigRange( min = 1, max = 10000000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "treasure", key = "treasureRegenTicksReduce", comment = "The amount of seconds each additional treasure reduces the regen time by" )
	public static Integer treasureRegenTicksReduce = 1;

	@ConfigRange( min = 1, max = 10000000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "treasure", key = "maxTreasures", comment = "The max amount of additional treasure that can be used to reduce the regen time" )
	public static Integer maxTreasures = 240;

	@ConfigOption( side = ConfigSide.SERVER, category = "source_of_magic", key = "damageWrongSourceOfMagic", comment = "Whether using the the source of magic intended for another dragon type will hurt you." )
	public static Boolean damageWrongSourceOfMagic = true;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "source_of_magic", key = "elderDragonDustTime", comment = "How long duration of the infinite magic effect using elder dragon dust gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer elderDragonDustTime = 20;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "source_of_magic", key = "elderDragonBoneTime", comment = "How long duration of the infinite magic effect using elder dragon bone gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer elderDragonBoneTime = 60;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "source_of_magic", key = "weakHeartShardTime", comment = "How long duration of the infinite magic effect using weak heart shard gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer weakHeartShardTime = 110;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "source_of_magic", key = "weakDragonHeartTime", comment = "How long duration of the infinite magic effect using weak dragon heart gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer weakDragonHeartTime = 310;

	@ConfigRange( min = 1, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "source_of_magic", key = "elderDragonHeartTime", comment = "How long duration of the infinite magic effect using elder dragon heart gives in seconds. Note that you also spend 10 seconds while waiting." )
	public static Integer elderDragonHeartTime = 1010;

	@ConfigOption(side = ConfigSide.SERVER, category = "general", key = "disableDragonSuffocation", comment = "Should suffocation damage be disabled for dragon players?")
	public static Boolean disableSuffocation = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "healthMod", comment = "Apply a health modifier for dragons. The older the dragon, the more health it has." )
	public static Boolean healthAdjustments = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "bonuses", comment = "Set too false to toggle off all dragon bonuses and play as human." )
	public static Boolean bonuses = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "attackMod", comment = "Apply an attack damage modifier for dragons." )
	public static Boolean attackDamage = true;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "babyAttackMod", comment = "Attack modifier for baby dragons." )
	public static Double babyBonusDamage = 1.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "youngAttackMod", comment = "Attack modifier for young dragons." )
	public static Double youngBonusDamage = 2.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "adultAttackMod", comment = "Attack modifier for adult dragons." )
	public static Double adultBonusDamage = 3.0;

	@ConfigRange( min = 0.0, max = 0.9 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "newbornJump", comment = "Jumping height bonus for a newborn dragon." )
	public static Double newbornJump = 0.025;

	@ConfigRange( min = 0.0, max = 0.9 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "youngJump", comment = "Jumping height bonus for a young dragon." )
	public static Double youngJump = 0.05;

	@ConfigRange( min = 0.0, max = 0.9 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "adultJump", comment = "Jumping height bonus for an adult dragon." )
	public static Double adultJump = 0.1;

	@ConfigRange( min = 0.0, max = 10.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "newbornStepHeight", comment = "Step height for a newborn dragon. Default is 1 block." )
	public static Double newbornStepHeight = 0.0;

	@ConfigRange( min = 0.0, max = 10.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "youngStepHeight", comment = "Step height for a young dragon. Default is 1.25 block." )
	public static Double youngStepHeight = 0.25;

	@ConfigRange( min = 0.0, max = 10.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "adultStepHeight", comment = "Step height for a adult dragon. Default is 1.5 block." )
	public static Double adultStepHeight = 0.5;


	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "clawsAreTools", comment = "Whether dragon claws function as tools." )
	public static Boolean clawsAreTools = true;

	@ConfigRange(min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "baseHarvestLevel", comment = "The harvest level to apply when dragons breaks a block, regardless of dragon/tool type." )
	public static Integer baseHarvestLevel = 0;

	@ConfigRange(min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "bonusHarvestLevel", comment = "The harvest level to apply to a dragons specific tool type once unlocked." )
	public static Integer bonusHarvestLevel = 1;

	@ConfigRange(min = 1, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "bonusBreakSpeed", comment = "Bonus break speed against blocks which are effective for the dragon type (break speed * bonus) - only applied if the bonus is unlocked")
	public static Float bonusBreakSpeed = 2f;

	@ConfigRange(min = 1, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "bonusBreakSpeedAdult", comment = "Bonus break speed against blocks which are effective for the dragon type (break speed * bonus) - only applied if the bonus is unlocked and the dragon is fully grown")
	public static Float bonusBreakSpeedAdult = 2.5f;

	@ConfigRange(min = 1, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "baseBreakSpeedAdult", comment = "Bonus break speed against all blocks (break speed * bonus) - only applied if the dragon is fully grown (unlocked bonus value will overwrite this one for effective blocks)")
	public static Float baseBreakSpeedAdult = 1.5f;

	@ConfigRange(min = 1, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "bonusBreakSpeedReduction", comment = "Value the bonus will be divided by if an effective claw tool is present for the block")
	public static Float bonusBreakSpeedReduction = 2f;

	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "bonusUnlockedAt", comment = "The stage that dragons unlock the bonus harvest level." )
	public static DragonLevel bonusUnlockedAt = DragonLevel.YOUNG;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = "bonuses", key = "speedupEffectLevel", comment = "The speed effect level for dragon block-specific speedups. Set to 0 to disable." )
	public static Integer speedupEffectLevel = 2;


	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "cave"}, key = "fireImmunity", comment = "Whether cave dragons are immune to fire damage types." )
	public static Boolean caveFireImmunity = true;

	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "cave"}, key = "lavaSwimming", comment = "Set to false to disable cave dragon fast lava swimming." )
	public static Boolean caveLavaSwimming = true;

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "cave"}, key = "lavaSwimTicks", comment = "The maximum number of ticks a cave dragon can swim in lava. Set to 0 to allow unlimited air while under lava." )
	public static Integer caveLavaSwimmingTicks = 3600;

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "cave"}, key = "caveSpeedupBlocks", comment = "Blocks cave dragons gain speed when standing above. Formatting: block/modid:id" )
	public static List<String> caveSpeedupBlocks = List.of(
			"minecraft:base_stone_nether",
			"minecraft:base_stone_overworld",
			"minecraft:stone_bricks",
			"minecraft:beacon_base_blocks",
			"c:cobblestone",
			"c:sandstone/blocks",
			"c:stones",
			"c:ores",
			"quark:deepslate",
			"quark:deepslate_bricks",
			"quark:cobbled_deepslate",
			"minecraft:lava",
			"minecraft:fire",
			"minecraft:soul_fire"
	);


	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "forest"}, key = "fallReduction", comment = "How many blocks of fall damage is mitigated for forest dragons. Set to 0.0 to disable." )
	public static Double forestFallReduction = 5.0;

	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "forest"}, key = "bushImmunity", comment = "Whether forest dragons are immune to Sweet Berry Bush damage." )
	public static Boolean forestBushImmunity = true;

	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "forest"}, key = "cactiImmunity", comment = "Whether forest dragons are immune to Cactus damage." )
	public static Boolean forestCactiImmunity = true;

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "forest"}, key = "forestSpeedupBlocks", comment = "Blocks forest dragons gain speed when standing above. Formatting: block/modid:id" )
	public static List<String> forestSpeedupBlocks = List.of(
			"minecraft:logs",
			"minecraft:leaves",
			"minecraft:planks",
			"minecraft:dirt",
			"minecraft:grass",
			"minecraft:dirt",
			"minecraft:wooden_slab"
	);


	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "sea"}, key = "waterBonuses", comment = "Whether sea dragons gain bonus swim speed and unlimited air." )
	public static Boolean seaSwimmingBonuses = true;

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"bonuses", "sea"}, key = "seaSpeedupBlocks", comment = "Blocks sea dragons gain speed when standing above. Formatting: block/modid:id" )
	public static List<String> seaSpeedupBlocks = List.of("minecraft:ice", "minecraft:impermeable", "minecraft:sand", "minecraft:mud", "minecraft:coral_blocks", "c:sands", "minecraft:dirt_path", "minecraft:sandstone", "minecraft:cut_sandstone", "minecraft:chiseled_sandstone", "minecraft:smooth_sandstone", "minecraft:red_sandstone", "minecraft:cut_red_sandstone", "minecraft:chiseled_red_sandstone", "minecraft:smooth_red_sandstone", "minecraft:water", "quark:permafrost", "immersive_weathering:permafrost", "architects_palette:polished_packed_ice");

	//Dragon Penalties
	@ConfigOption( side = ConfigSide.SERVER, category = "penalties", key = "penalties", comment = "Set to false to toggle off all dragon penalties." )
	public static Boolean penalties = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "penalties", key = "dragonsAreScary", comment = "Whether dragons are scary or not." )
	public static Boolean dragonsAreScary = true;

	@ConfigType(EntityType.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "penalties", key = "allowedVehicles", comment = "List of rideable entities. Format: modid:id" )
	public static List<String> allowedVehicles = List.of("minecraft:boat", "littlelogistics:seater_barge", "minecraft:minecart", "create:seat", "create:contraption", "create:gantry_contraption", "create:stationary_contraption", "hexerei:broom", "botania:player_mover", "quark:quark_boat");

	@ConfigOption( side = ConfigSide.SERVER, category = "penalties", key = "limitedRiding", comment = "Should dragons be limited by which entities they can ride" )
	public static Boolean ridingBlacklist = true;

	@ConfigType(Item.class) // TODO :: Use tag
	@ConfigOption( side = ConfigSide.SERVER, category = "penalties", key = "blacklistedItems", comment = "List of items that are disallowed to be used by dragons. Format: item/modid:id" )
	public static List<String> blacklistedItems = List.of(
			"c:tools/shields",
			"c:tools/bows",
			"c:tools/crossbows",
			"minecraft:trident",
			"minecraft:elytra",
			"quark:flamerang",
			"quark:pickarang",
			"mowziesmobs:wrought_axe",
			"nethers_exoticism:rambutan_shield",
			"aquaculture:neptunium_bow",
			"endermanoverhaul:corrupted_shield",
			"upgradednetherite:echo_upgraded_netherite_shield",
			"upgradednetherite:corrupt_upgraded_netherite_shield",
			"upgradednetherite:feather_upgraded_netherite_shield",
			"upgradednetherite:phantom_upgraded_netherite_shield",
			"upgradednetherite:poison_upgraded_netherite_shield",
			"upgradednetherite:wither_upgraded_netherite_shield",
			"upgradednetherite:water_upgraded_netherite_shield",
			"upgradednetherite:ender_upgraded_netherite_shield",
			"magistuarmory:bronze_ellipticalshield",
			"magistuarmory:wood_roundshield",
			"magistuarmory:stone_roundshield",
			"magistuarmory:iron_roundshield",
			"magistuarmory:gold_roundshield",
			"magistuarmory:diamond_roundshield",
			"magistuarmory:netherite_roundshield",
			"magistuarmory:copper_roundshield",
			"magistuarmory:steel_roundshield",
			"magistuarmory:silver_roundshield",
			"magistuarmory:tin_roundshield",
			"magistuarmory:bronze_roundshield",
			"magistuarmory:wood_kiteshield",
			"magistuarmory:stone_kiteshield",
			"magistuarmory:iron_kiteshield",
			"magistuarmory:gold_kiteshield",
			"magistuarmory:diamond_kiteshield",
			"magistuarmory:netherite_kiteshield",
			"magistuarmory:steel_kiteshield",
			"magistuarmory:copper_kiteshield",
			"magistuarmory:silver_kiteshield",
			"magistuarmory:tin_kiteshield",
			"magistuarmory:bronze_kiteshield",
			"unusualend:blob_shield",
			"upgradednetherite:netherite_shield",
			"upgradednetherite:gold_upgraded_netherite_shield",
			"upgradednetherite:fire_upgraded_netherite_shield",
			"magistuarmory:stone_ellipticalshield",
			"magistuarmory:iron_ellipticalshield",
			"magistuarmory:diamond_ellipticalshield",
			"magistuarmory:gold_ellipticalshield",
			"infernalexp:glowsilk_bow",
			"magistuarmory:tin_ellipticalshield",
			"upgradednetherite:gold_upgraded_netherite_bow",
			"upgradednetherite:fire_upgraded_netherite_bow",
			"upgradednetherite:ender_upgraded_netherite_bow",
			"magistuarmory:silver_ellipticalshield",
			"magistuarmory:steel_ellipticalshield",
			"magistuarmory:copper_ellipticalshield",
			"magistuarmory:netherite_ellipticalshield",
			"upgradednetherite:netherite_crossbow",
			"upgradednetherite:gold_upgraded_netherite_crossbow",
			"upgradednetherite:fire_upgraded_netherite_crossbow",
			"upgradednetherite:ender_upgraded_netherite_crossbow",
			"upgradednetherite:water_upgraded_netherite_crossbow",
			"upgradednetherite:wither_upgraded_netherite_crossbow",
			"upgradednetherite:poison_upgraded_netherite_crossbow",
			"upgradednetherite:phantom_upgraded_netherite_crossbow",
			"upgradednetherite:feather_upgraded_netherite_crossbow",
			"upgradednetherite:corrupt_upgraded_netherite_crossbow",
			"upgradednetherite:echo_upgraded_netherite_crossbow",
			"magistuarmory:wood_heatershield", "magistuarmory:stone_heatershield",
			"magistuarmory:iron_heatershield", "magistuarmory:gold_heatershield",
			"magistuarmory:diamond_heatershield", "magistuarmory:netherite_heatershield",
			"magistuarmory:copper_heatershield", "magistuarmory:steel_heatershield",
			"magistuarmory:silver_heatershield", "magistuarmory:tin_heatershield",
			"magistuarmory:bronze_heatershield", "magistuarmory:wood_ellipticalshield",
			"magistuarmory:silver_target", "magistuarmory:steel_target",
			"magistuarmory:copper_target", "magistuarmory:netherite_target",
			"magistuarmory:diamond_target",
			"magistuarmory:gold_target",
			"magistuarmory:iron_target",
			"magistuarmory:stone_target",
			"aquaculture:neptunium_bow",
			"magistuarmory:gold_pavese",
			"magistuarmory:iron_pavese",
			"magistuarmory:wood_pavese",
			"magistuarmory:tin_tartsche",
			"magistuarmory:silver_tartsche",
			"magistuarmory:steel_tartsche",
			"magistuarmory:copper_tartsche",
			"magistuarmory:diamond_tartsche",
			"magistuarmory:iron_tartsche",
			"magistuarmory:stone_tartsche",
			"magistuarmory:wood_tartsche",
			"magistuarmory:bronze_rondache",
			"magistuarmory:tin_rondache",
			"magistuarmory:netherite_rondache",
			"magistuarmory:diamond_rondache",
			"magistuarmory:gold_rondache",
			"magistuarmory:iron_rondache",
			"magistuarmory:wood_rondache",
			"magistuarmory:tin_buckler",
			"magistuarmory:steel_buckler",
			"magistuarmory:copper_buckler",
			"magistuarmory:netherite_buckler",
			"magistuarmory:diamond_buckler",
			"magistuarmory:gold_buckler",
			"magistuarmory:iron_buckler",
			"magistuarmory:stone_buckler",
			"magistuarmory:tin_target",
			"magistuarmory:diamond_pavese",
			"magistuarmory:netherite_pavese",
			"magistuarmory:copper_pavese",
			"magistuarmory:steel_pavese",
			"magistuarmory:tin_pavese",
			"endermanoverhaul:corrupted_shield",
			"upgradednetherite:wither_upgraded_netherite_bow",
			"upgradednetherite:water_upgraded_netherite_bow",
			"upgradednetherite:poison_upgraded_netherite_bow",
			"upgradednetherite:phantom_upgraded_netherite_bow",
			"upgradednetherite:feather_upgraded_netherite_bow",
			"upgradednetherite:corrupt_upgraded_netherite_bow",
			"upgradednetherite:echo_upgraded_netherite_bow",
			"cataclysm:ignitium_elytra_chestplate",
			"revised_phantoms:phantom_wings_chestplate",
			"deeperdarker:soul_elytra",
			"born_in_chaos_v1:staffof_magic_arrows",
			"magistuarmory:heavy_crossbow",
			"spartanweaponry:boomerang_.*",
			"spartanshields:shield_.*",
			"spartanweaponry:heavy_crossbow_.*",
			"spartanweaponry:longbow_.*"
	);

	// Cave Dragon Penalties
	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"penalties", "cave"}, key = "waterDamage", comment = "The amount of damage taken per water damage tick (once every 10 ticks). Set to 0.0 to disable water damage." )
	public static Double caveWaterDamage = 1.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category =  {"penalties", "cave"}, key = "rainDamage", comment = "The amount of damage taken per rain damage tick (once every 40 ticks). Set to 0.0 to disable rain damage." )
	public static Double caveRainDamage = 1.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "cave"}, key = "splashDamage", comment = "The amount of damage taken when hit with a snowball or a water bottle. Set to 0.0 to disable splash damage." )
	public static Double caveSplashDamage = 2.0;

	// Forest Dragon Penalties
	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "forest"}, key = "ticksBeforeStressed", comment = "The number of ticks in darkness before the forest dragon gets Stress effect. Set to 0 to disable to stress effect." )
	public static Integer forestStressTicks = 100;

	@ConfigRange( min = 2, max = 100000 )
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "forest"}, key = "stressEffectDuration", comment = "The number of seconds the stress effect lasts for." )
	public static Integer forestStressEffectDuration = 10;

	@ConfigRange( min = 0.1, max = 4.0 )
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "forest"}, key = "stressExhaustion", comment = "The amount of exhaustion applied per 10 ticks during the stress effect." )
	public static Double stressExhaustion = 1.0;

	// Sea Dragon Penalties

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "sea"}, key = "ticksWithoutWater", comment = "The number of ticks out of water before the sea dragon will start taking dehydration damage. Set to 0 to disable. Note: This value can stack up to double while dehydrated." )
	public static Integer seaTicksWithoutWater = 1000;

	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "sea"}, key = "waterConsumptionDependsOnTemperature", comment = "Whether the sea dragon should lose more water in warmer biomes and less during the night." )
	public static Boolean seaTicksBasedOnTemperature = true;

	@ConfigRange( min = 0.5, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "sea"}, key = "dehydrationDamage", comment = "The amount of damage taken per tick while dehydrated (once every 40 ticks unless fully dehydrated, then once every 20 ticks)." )
	public static Double seaDehydrationDamage = 1.0;

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "sea"}, key = "seaHydrationBlocks", comment = "When sea dragons stand on these blocks, hydration is restored. Format: block/modid:id" )
	public static List<String> seaHydrationBlocks = List.of("minecraft:ice", "minecraft:snow", "minecraft:powder_snow", "minecraft:snow_block", "minecraft:muddy_mangrove_roots", "minecraft:mud", "minecraft:wet_sponge", "dragonsurvival:sea_source_of_magic", "immersive_weathering:thin_ice", "immersive_weathering:cryosol", "immersive_weathering:permafrost", "immersive_weathering:frosty_grass", "immersive_weathering:frosty_fern", "ecologics:thin_ice", "ecologics:ice_bricks", "ecologics:ice_brick_stairs", "ecologics:ice_brick_slab", "ecologics:ice_brick_wall", "ecologics:snow_bricks", "ecologics:snow_brick_stairs", "ecologics:snow_brick_slab", "ecologics:snow_brick_wall", "architects_palette:poliched_packed_ice", "architects_palette:poliched_packed_ice_slab", "architects_palette:poliched_packed_ice_vertical_slab", "architects_palette:poliched_packed_ice_stairs", "architects_palette:poliched_packed_ice_wall", "architects_palette:chiseled_packed_ice", "architects_palette:packed_ice_pillar", "architects_palette:coarse_snow", "fantasyfurniture:decorations/snowballs", "immersive_weathering:icicle", "regions_unexplored:plains_mud", "regions_unexplored:silt_mud", "regions_unexplored:peat_mud", "regions_unexplored:forest_mud", "naturearchitect:snow_block_0", "naturearchitect:snow_block_2", "naturearchitect:snow_cover_1", "naturearchitect:snow_cover_2", "naturearchitect:snow_cover_3", "naturearchitect:snow_block_2", "immersive_weathering:snowy_stone_brick_wall", "immersive_weathering:snowy_stone_brick_stairs", "immersive_weathering:snowy_chiseled_stone_bricks", "immersive_weathering:snowy_stone_bricks", "immersive_weathering:snowy_cobblestone_wall", "immersive_weathering:snowy_cobblestone_slab", "immersive_weathering:snowy_cobblestone_stairs", "immersive_weathering:snowy_cobblestone", "immersive_weathering:snowy_stone_wall", "immersive_weathering:snowy_stone_slab", "immersive_weathering:snowy_stone_stairs", "immersive_weathering:snowy_stone", "immersive_weathering:snow_brick_wall", "immersive_weathering:snow_brick_slab", "immersive_weathering:snow_brick_stairs", "immersive_weathering:snow_bricks", "frozenup:compacted_snow_foundation", "frozenup:compacted_snow_brick_vertical_slab", "frozenup:compacted_snow_brick_slab", "frozenup:compacted_snow_brick_stairs", "frozenup:compacted_snow_bricks", "frozenup:compacted_snow_brick_stairs");

	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "sea"}, key = "allowWaterBottles", comment = "Set to false to disable sea dragons using vanilla water bottles to avoid dehydration." )
	public static Boolean seaAllowWaterBottles = true;

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "sea"}, key = "waterItemRestorationTicks", comment = "How many ticks do water restoration items restore when used. Set to 0 to disable." )
	public static Integer seaTicksWithoutWaterRestored = 5000;

	@ConfigType(Item.class)
	@ConfigOption( side = ConfigSide.SERVER, category  = {"penalties", "sea"}, key = "seaHydrationItems", comment = "Additional modded USEABLE items that restore water when used (called from LivingEntityUseItemEvent.Finish). Format: item/modid:id" )
	public static List<String> seaAdditionalWaterUseables = List.of("immersive_weathering:icicle");

	// Ore Loot
	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "humanOreDustChance", comment = "The odds of dust dropping when a human harvests an ore." )
	public static Double humanOreDustChance = 0.1;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragonOreDustChance", comment = "The odds of dust dropping when a dragon harvests an ore." )
	public static Double dragonOreDustChance = 0.2;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "humanOreBoneChance", comment = "The odds of a bone dropping when a human harvests an ore." )
	public static Double humanOreBoneChance = 0.0;

	@ConfigRange( min = 0.0, max = 1.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragonOreBoneChance", comment = "The odds of a bone dropping when a dragon harvests an ore." )
	public static Double dragonOreBoneChance = 0.01;

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "oresTag", comment = "The tag that contains all ores that can drop dust/bones when harvested. Will not drop if the ore drops another of the items in this tag. Format: modid:id" )
	public static String oresTag = "c:ores";

	@ConfigType(Item.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"food", "cave_dragon", "other"}, key = "hurtfulToCaveDragon", comment = "Items which will cause damage to cave dragons when consumed. Formatting: item/modid:itemid:damage" )
	public static List<String> caveDragonHurtfulItems = Arrays.asList("minecraft:potion:2", "minecraft:water_bottle:2", "minecraft:milk_bucket:2");

	@ConfigType(Item.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"food", "sea_dragon", "other"}, key = "hurtfulToSeaDragon", comment = "Items which will cause damage to sea dragons when consumed. Formatting: item/modid:itemid:damage" )
	public static List<String> seaDragonHurtfulItems = Collections.emptyList();

	@ConfigType(Item.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"food", "forest_dragon", "other"}, key = "hurtfulToForestDragon", comment = "Items which will cause damage to forest dragons when consumed. Formatting: item/modid:itemid:damage" )
	public static List<String> forestDragonHurtfulItems = Collections.emptyList();

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"food", "cave_dragon", "other"}, key = "chargedSoupBuffDuration", comment = "How long in seconds should the cave fire effect from charged soup last. (Default to 5min) Set to 0 to disable." )
	public static Integer chargedSoupBuffDuration = 300;


	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "mana", "sea_dragon_mana"}, key = "seaDragonManaBlocks", comment = "Blocks that will restore mana quicker when a sea dragon is standing on it. Formatting: block/modid:blockid" )
	public static List<String> seaDragonManaBlocks = List.of("dragonsurvival:sea_source_of_magic", "minecraft:ice", "minecraft:snow", "minecraft:snow_block", "minecraft:powder_snow", "minecraft:water", "minecraft:wet_sponge", "minecraft:cauldron", "naturearchitect:snow_block_0", "naturearchitect:snow_block_2", "naturearchitect:snow_cover_1", "naturearchitect:snow_cover_2", "naturearchitect:snow_cover_3", "naturearchitect:snow_block_2", "immersive_weathering:snowy_stone_brick_wall", "immersive_weathering:snowy_stone_brick_stairs", "immersive_weathering:snowy_chiseled_stone_bricks", "immersive_weathering:snowy_stone_bricks", "immersive_weathering:snowy_cobblestone_wall", "immersive_weathering:snowy_cobblestone_slab", "immersive_weathering:snowy_cobblestone_stairs", "immersive_weathering:snowy_cobblestone", "immersive_weathering:snowy_stone_wall", "immersive_weathering:snowy_stone_slab", "immersive_weathering:snowy_stone_stairs", "immersive_weathering:snowy_stone", "immersive_weathering:snow_brick_wall", "immersive_weathering:snow_brick_slab", "immersive_weathering:snow_brick_stairs", "immersive_weathering:snow_bricks", "frozenup:compacted_snow_foundation", "frozenup:compacted_snow_brick_vertical_slab", "frozenup:compacted_snow_brick_slab", "frozenup:compacted_snow_brick_stairs", "frozenup:compacted_snow_bricks", "frozenup:compacted_snow_brick_stairs");

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "mana", "forest_dragon_mana"}, key = "forestDragonManaBlocks", comment = "Blocks that will restore mana quicker when a forest dragon is standing on it. Formatting: block/modid:blockid" )
	public static List<String> forestDragonManaBlocks = List.of("dragonsurvival:forest_source_of_magic", "minecraft:grass_block", "minecraft:grass_block", "minecraft:small_flowers", "minecraft:flowers", "minecraft:tall_flowers", "minecraft:lily_pad", "minecraft:red_mushroom", "minecraft:brown_mushroom", "minecraft:sweet_berry_bush", "minecraft:oak_leaves", "naturearchitect:grass_cover_stairs", "naturearchitect:grass_cover_slab", "farmersdelight:brown_mushroom_colony", "farmersdelight:red_mushroom_colony", "gothic:black_mushroom", "gothic:tall_mushrooms", "gothic:cave_mushrooms", "naturearchitect:grass_carpet", "regions_unexplored:mycotoxic_mushrooms", "naturearchitect:moss_cover_3", "naturearchitect:moss_cover_2", "naturearchitect:moss_cover_1", "naturearchitect:mycelium_block_2", "naturearchitect:mycelium_cover_3", "naturearchitect:mycelium_cover_1", "naturearchitect:mycelium_cover_2", "naturearchitect:mycelium_block_1", "naturearchitect:moss_plant_1", "naturearchitect:moss_plant_2", "naturearchitect:moss_plant_3", "naturearchitect:moss_patch", "naturearchitect:moss_patch_dense", "regions_unexplored:spanish_moss", "minecraft:mycelium", "minecraft:moss_block", "minecraft:moss_carpet", "regions_unexplored:alpha_grass_block", "regions_unexplored:chalk_grass_block", "regions_unexplored:peat_grass_block", "regions_unexplored:silt_grass_block", "regions_unexplored:argillite_grass_block", "regions_unexplored:stone_grass_block", "regions_unexplored:deepslate_grass_block", "immersive_weathering:rooted_grass_block", "naturearchitect:grass_block", "naturearchitect:grass_cover", "naturearchitect:crimson_92", "naturearchitect:grass_1", "naturearchitect:grass_2", "naturearchitect:grass_3", "naturearchitect:grass_4", "naturearchitect:grass_windy", "naturearchitect:grass_stalk", "naturearchitect:grass_fern", "naturearchitect:grass_sapling", "phantasm:vivid_nihilium_grass", "vinery:grass_slab", "naturearchitect:grass_liana", "naturearchitect:grass_ivy", "naturearchitect:grass_bean_stalk");

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "mana", "cave_dragon_mana"}, key = "caveDragonManaBlocks", comment = "Blocks that will restore mana quicker when a cave dragon is standing on it. Formatting: block/modid:blockid" )
	public static List<String> caveDragonManaBlocks = List.of("dragonsurvival:cave_source_of_magic", "minecraft:fire", "minecraft:campfires", "minecraft:lava", "minecraft:smoker", "minecraft:furnace", "minecraft:magma_block", "minecraft:blast_furnace", "netherdepthsupgrade:wet_lava_sponge", "regions_unexplored:brimwood_log_magma", "infernalexp:magmatic_chiseled_basalt_bricks", "infernalexp:basaltic_magma", "regions_unexplored:brimwood_log_magma", "naturearchitect:magma_inactive", "naturearchitect:magma_cracks", "netherdepthsupgrade:lava_sponge");


	@ConfigOption( side = ConfigSide.SERVER, category = "magic", key = "dragonAbilities", comment = "Whether dragon abilities should be enabled" )
	public static Boolean dragonAbilities = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "magic", key = "caveDragonAbilities", comment = "Whether cave dragon abilities should be enabled" )
	public static Boolean caveDragonAbilities = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "magic", key = "forestDragonAbilities", comment = "Whether forest dragon abilities should be enabled" )
	public static Boolean forestDragonAbilities = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "magic", key = "seaDragonAbilities", comment = "Whether sea dragon abilities should be enabled" )
	public static Boolean seaDragonAbilities = true;


	@ConfigOption( side = ConfigSide.SERVER, category = "magic", key = "noEXPRequirements", comment = "Disable the exp requirements for leveling up active skills" )
	public static Boolean noEXPRequirements = false;

	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "mana"}, key = "consumeEXPAsMana", comment = "Whether to use exp instead of mana if mana is empty" )
	public static Boolean consumeEXPAsMana = true;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = "magic", key = "initialPassiveCost", comment = "The initial exp cost for leveling passive skills." )
	public static Integer initialPassiveCost = 4;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = "magic", key = "passiveScalingCost", comment = "The multiplier that is used to increase the passive skill costs per level" )
	public static Double passiveScalingCost = 6.0;


	@ConfigRange( min = 1, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "mana"}, key = "favorableManaRegen", comment = "How fast in seconds should mana be recovered in favorable conditions" )
	public static Integer favorableManaTicks = 1;

	@ConfigRange( min = 1, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "mana"}, key = "normalManaRegen", comment = "How fast in seconds should mana be recovered in normal conditions" )
	public static Integer normalManaTicks = 10;


	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities"}, key = "saveAllAbilities", comment = "Whether to save passives skills when changing dragon type" )
	public static Boolean saveAllAbilities = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "general", key = "endVoidTeleport", comment = "Should the player be teleported to the overworld when they fall in the end?" )
	public static Boolean endVoidTeleport = true;

	@ConfigRange( min = 1, max = 120 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "hunterDespawnDelay", comment = "Any dragon hunter, princess and prince group may despawn after this many minutes" )
	public static Integer hunterDespawnDelay = 20;

	@ConfigRange( min = 10, max = 240 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "princessSpawnDelay", comment = "Minimum delay between prince or princess spawning around village, in minutes" )
	public static Integer royalSpawnDelay = 240;

	@ConfigRange( min = 1, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "royalDisappearInMinutes", comment = "In how many minutes the Prince and Princess will disappear after the call with the summon scroll. Default is 15 minutes" )
	public static Integer royalDisappearInMinutes = 15;

	@ConfigRange( min = 12, max = 240 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "hunterGroupSpawnDelay", comment = "Minimum delay between Dragon hunter group spawning, in minutes" )
	public static Integer hunterSpawnDelay = 20;

	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "allowKnightSpawning", comment = "Dragon knight spawning enabled?" )
	public static Boolean spawnKnight = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "allowSquireSpawning", comment = "Dragon Squire spawning enabled?" )
	public static Boolean spawnSquire = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "allowHunterSpawning", comment = "Dragon Hunter spawning enabled?" )
	public static Boolean spawnHunter = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "allowHoundSpawning", comment = "Dragon Knight hound spawning enabled?" )
	public static Boolean spawnHound = true;

	@ConfigRange( min = 10, max = 1000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "villagerKillxp", comment = "How many experience points are gained for killing a villager" )
	public static Integer xpGain = 10;

	@ConfigType(EntityType.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "royalChaseStatusGivers", comment = "Entities which give 'Evil dragon' status on death" )
	public static List<String> royalChaseStatusGivers = List.of("minecraft:villager", "minecraft:iron_golem", "dragonsurvival:hunter_hound", "dragonsurvival:knight", "dragonsurvival:shooter", "dragonsurvival:squire", "dragonsurvival:prince", "dragonsurvival:princess", "dragonsurvival:princess_entity", "guardvillagers:guard");

	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "preserveRoyalChaseAfterDeath", comment = "Preserve effect 'Evil dragon' after death?" )
	public static Boolean preserveRoyalChaseEffectAfterDeath = false;

	@ConfigRange( min = 6, max = 128 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "princessAndHuntersLowerSpawnBound", comment = "Lowest Y value allowed for princess and hunter spawning" )
	public static Integer riderSpawnLowerBound = 32;

	@ConfigRange( min = 64, max = 250 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_hunters", key = "princessAndHuntersUpperSpawnBound", comment = "Highest Y value allowed for princess and hunter spawning" )
	public static Integer riderSpawnUpperBound = 80;

	@ConfigRange( min = 10d, max = 80d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knightHealth", comment = "Dragon Knight health" )
	public static Double knightHealth = 40d;

	@ConfigRange( min = 1d, max = 32d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knightDamage", comment = "Dragon Knight base damage" )
	public static Double knightDamage = 12d;

	@ConfigRange( min = 0d, max = 30d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knightArmor", comment = "Dragon Knight armor" )
	public static Double knightArmor = 10d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knightSpeed", comment = "Dragon Knight speed" )
	public static Double knightSpeed = 0.3d;

	@ConfigRange( min = 0.0d, max = 1d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knightShieldChance", comment = "Chance of having shield" )
	public static Double knightShieldChance = 0.1d;

	@ConfigRange( min = 8d, max = 40d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "houndHealth", comment = "Knight Hound health" )
	public static Double houndHealth = 10d;

	@ConfigRange( min = 1d, max = 10d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "houndDamage", comment = "Knight Hound damage" )
	public static Double houndDamage = 2d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "houndSpeed", comment = "Knight Hound speed" )
	public static Double houndSpeed = 0.45d;

	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "houndDoesSlowdown", comment = "Does Knight Hound apply speed slowdown?" )
	public static Boolean houndDoesSlowdown = true;

	@ConfigRange( min = 10d, max = 60d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterHealth", comment = "Dragon Hunter health" )
	public static Double hunterHealth = 24d;

	@ConfigRange( min = 2d, max = 20d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterDamage", comment = "Dragon Hunter damage" )
	public static Double hunterDamage = 5d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterSpeed", comment = "Dragon Hunter speed" )
	public static Double hunterSpeed = 0.35d;

	@ConfigRange( min = 0d, max = 20d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterArmor", comment = "Dragon Hunter armor" )
	public static Double hunterArmor = 0d;

	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterHasBolas", comment = "Is Dragon hunter able to throw a bolas?" )
	public static Boolean hunterHasBolas = true;

	@ConfigRange( min = 1.0, max = 60.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterBolasFrequency", comment = "How frequently does the dragon hunter throw the bolas?" )
	public static Double hunterBolasFrequency = 10.0;

	@ConfigRange( min = 1.0, max = 60.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "hunter"}, key = "hunterTrappedDebuffDuration", comment = "How long does the trapped debuff last?" )
	public static Double hunterTrappedDebuffDuration = 5.0;

	@ConfigRange( min = 10d, max = 60d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "squire"}, key = "squireHealth", comment = "Dragon Squire health" )
	public static Double squireHealth = 24d;

	@ConfigRange( min = 2d, max = 20d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "squire"}, key = "squireDamage", comment = "Dragon Squire damage" )
	public static Double squireDamage = 6d;

	@ConfigRange( min = 0.1d, max = 0.6d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "squire"}, key = "squireSpeed", comment = "Dragon Squire speed" )
	public static Double squireSpeed = 0.35d;

	@ConfigRange( min = 0d, max = 20d )
	@ConfigOption( side = ConfigSide.SERVER, category = {"dragon_hunters", "squire"}, key = "squireArmor", comment = "Dragon Squire armor" )
	public static Double squireArmor = 2d;

	@ConfigRange( min = 1, max = 60 * 60 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_beacons", key = "constantEffect", comment = "Duration of effect given by beacon constantly in seconds" )
	public static Integer secondsOfBeaconEffect = 20;

	@ConfigRange( min = 1, max = 60 * 2 )
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_beacons", key = "temporaryEffect", comment = "Duration of effect given in exchange for experience in minutes" )
	public static Integer minutesOfDragonEffect = 10;

	@ConfigType(MobEffect.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_beacons", key = "peaceBeaconEffects", comment = "Effects of Peace beacon" )
	public static List<String> peaceBeaconEffects = List.of("dragonsurvival:peace", "dragonsurvival:animal_peace");

	@ConfigType(MobEffect.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_beacons", key = "magicBeaconEffects", comment = "Effects of Magic beacon" )
	public static List<String> magicBeaconEffects = List.of("dragonsurvival:magic", "minecraft:haste");

	@ConfigType(MobEffect.class)
	@ConfigOption( side = ConfigSide.SERVER, category = "dragon_beacons", key = "fireBeaconEffects", comment = "Effects of Fire beacon" )
	public static List<String> fireBeaconEffects = List.of("dragonsurvival:fire", "dragonsurvival:strong_leather");
}