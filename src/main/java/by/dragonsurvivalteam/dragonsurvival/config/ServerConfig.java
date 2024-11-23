package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {
    ServerConfig(ModConfigSpec.Builder builder) {
        ConfigHandler.createConfigEntries(builder, ConfigSide.SERVER);
    }

    @Translation(key = "force_vault_state_updates", type = Translation.Type.CONFIGURATION, comments = "If enabled vaults will immediately update their state")
    @ConfigOption(side = ConfigSide.SERVER, category = "debug", key = "force_vault_state_updates")
    public static Boolean forceStateUpdatingOnVaults = false;

    @ConfigRange(min = 0, max = 1000)
    @Translation(key = "altar_cooldown", type = Translation.Type.CONFIGURATION, comments = "Cooldown (in seconds) after using an altar")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "altar_cooldown")
    public static Integer altarUsageCooldown = 0;

    @Translation(key = "transform_altar", type = Translation.Type.CONFIGURATION, comments = "Enables the transformation of certain blocks into dragon altars when using an elder dragon bone item on them")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "transform_altar")
    public static Boolean transformAltar = true;

    @Translation(key = "retain_claw_items", type = Translation.Type.CONFIGURATION, comments = "If enabled the items in the claw inventory will not drop on death")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "retain_claw_items")
    public static Boolean retainClawItems = false;

    @Translation(key = "sync_claw_render", type = Translation.Type.CONFIGURATION, comments = {
            "If enabled dragon claw and teeth (which indicate the currently equipped claw tools) will be synchronized to other players",
            "This may be relevant for any sort of PvP content"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "sync_claw_render")
    public static Boolean syncClawRender = true;

    @Translation(key = "can_move_in_emotes", type = Translation.Type.CONFIGURATION, comments = "If enabled players will be able to move while performing emotes")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "can_move_in_emotes")
    public static Boolean canMoveInEmote = true;

    @Translation(key = "can_move_while_casting", type = Translation.Type.CONFIGURATION, comments = "If enabled the movement restrictions from casting certain abilities will be ignored")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "can_move_while_casting")
    public static Boolean canMoveWhileCasting = false;

    @Translation(key = "start_with_dragon_choice", type = Translation.Type.CONFIGURATION, comments = "If enabled players will be giving a choice to select a dragon type when first joining the world")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "start_with_dragon_choice")
    public static Boolean startWithDragonChoice = true;

    @Translation(key = "allow_dragon_choice_from_inventory", type = Translation.Type.CONFIGURATION, comments = "If enabled players that have not yet chosen a dragon type will be able to do so from the vanilla inventory")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "allow_dragon_choice_from_inventory")
    public static Boolean allowDragonChoiceFromInventory = true;

    @Translation(key = "disable_dragon_suffocation", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will not take suffocation damage")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "disable_dragon_suffocation")
    public static Boolean disableDragonSuffocation = true;

    // --- Large dragon scaling --- //

    @Translation(key = "destructible_blocks_blacklist", type = Translation.Type.CONFIGURATION, comments = "If enabled the destructible block tag for is used as a blacklist - if disabled it will be used as a whitelist")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "destructible_blocks_blacklist")
    public static Boolean destructibleBlocksIsBlacklist = false;

    @Translation(key = "allow_block_destruction", type = Translation.Type.CONFIGURATION, comments = "If enabled certain blocks will be automatically destroyed when dragons above a certain size collide with them - not active while crouching")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "allow_block_destruction")
    public static Boolean allowBlockDestruction = false;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "block_destruction_removal", type = Translation.Type.CONFIGURATION, comments = {
            "Determines the percentage chance that a block is removed, bypassing sound or particle effects",
            "This is to avoid potential lag issues due to large amounts of sound effects or particles"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "block_destruction_removal")
    public static Double blockDestructionRemoval = 0.96;

    @ConfigRange(min = 14.0, max = 1_000_000.0)
    @Translation(key = "block_destruction_size", type = Translation.Type.CONFIGURATION, comments = "Determines at which size a dragon destroys blocks")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "block_destruction_size")
    public static Double largeBlockDestructionSize = 120.0;

    @ConfigRange(min = 0.0, max = 10.0)
    @Translation(key = "large_block_break_radius_scaling", type = Translation.Type.CONFIGURATION, comments = {
            "Determines the block break radius bonus at max. growth - the intention is to grant the specified value per 60 size - disabled if set to 0",
            "This bonus is not active while crouching"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "large_block_break_radius_scaling")
    public static Double largeBlockBreakRadiusScalar = 0.0;

    @Translation(key = "allow_crushing", type = Translation.Type.CONFIGURATION, comments = "If enabled entities beneath dragons above a certain size will be crushed - not active while crouching")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "allow_crushing")
    public static Boolean allowCrushing = false;

    @ConfigRange(min = 14.0, max = 1_000_000.0)
    @Translation(key = "crushing_size", type = Translation.Type.CONFIGURATION, comments = "Determines at which size a dragon crushes entities")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushing_size")
    public static Double crushingSize = 120.0;

    @ConfigRange(min = 0.0, max = 20.0)
    @Translation(key = "crushing_damage_scaling", type = Translation.Type.CONFIGURATION, comments = "Scales the dragon size to determine the damage dealt to crushed entities")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushing_damage_scaling")
    public static Double crushingDamageScalar = 0.05;

    @ConfigRange(min = 0, max = 20)
    @Translation(key = "crushing_interval", type = Translation.Type.CONFIGURATION, comments = "The amount of ticks (20 ticks = 1 second) before an entity can be crushed again")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushing_interval")
    public static Integer crushingTickDelay = 20;

    // --- Standard dragon scaling --- //

    @Translation(key = "natural_growth", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will also naturally grow over time")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "standard_dragon"}, key = "natural_growth")
    public static Boolean naturalGrowth = true;

    @Translation(key = "save_growth_stage", type = Translation.Type.CONFIGURATION, comments = "If enabled the current growth will be saved for the current dragon type when changing types or reverting back to being a human")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth"}, key = "save_growth_stage")
    public static Boolean saveGrowthStage = false;

    // --- Item drops --- //

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "dragon_heart_shard_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of dragon heart shards dropping from entities with a maximum health between 14 and 20")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragon_heart_shard_chance")
    public static Double dragonHeartShardChance = 0.03;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "weak_dragon_heart_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of weak dragon hearts dropping from entities with a maximum health between 20 and 50")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weak_dragon_heart_chance")
    public static Double weakDragonHeartChance = 0.01;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "elder_dragon_heart_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of elder dragon hearts dropping from entities with a maximum health above 50")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elder_dragon_heart_chance")
    public static Double elderDragonHeartChance = 0.01;

    @ConfigType(EntityType.class) // FIXME :: tag
    @Translation(key = "dragon_heart_entity_list", type = Translation.Type.CONFIGURATION, comments = "Determines either which entities cannot drop dragon hearts or which entities are allowed to drop dragon hearts")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragon_heart_entity_list", validation = Validation.RESOURCE_LOCATION)
    public static List<String> dragonHeartEntityList = List.of();

    @ConfigType(EntityType.class) // FIXME :: tag
    @Translation(key = "weak_dragon_heart_entity_list", type = Translation.Type.CONFIGURATION, comments = "Determines either which entities cannot drop weak dragon hearts or which entities are allowed to drop weak dragon hearts")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weak_dragon_heart_entity_list", validation = Validation.RESOURCE_LOCATION)
    public static List<String> weakDragonHeartEntityList = List.of();

    @ConfigType(EntityType.class) // FIXME :: tag
    @Translation(key = "elder_dragon_heart_entity_list", type = Translation.Type.CONFIGURATION, comments = "Determines either which entities cannot drop elder dragon hearts or which entities are allowed to drop elder dragon hearts")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elder_dragon_heart_entity_list", validation = Validation.RESOURCE_LOCATION)
    public static List<String> elderDragonHeartEntityList = List.of();

    @Translation(key = "dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "If enabled the entity list for dragon hearts acts as a whitelist - if disabled it acts as a blacklist")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragon_heart_white_list")
    public static Boolean dragonHeartWhiteList = false;

    @Translation(key = "weak_dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "If enabled the entity list for weak dragon hearts acts as a whitelist - if disabled it acts as a blacklist")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weak_dragon_heart_white_list")
    public static Boolean weakDragonHeartWhiteList = false;

    @Translation(key = "elder_dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "If enabled the entity list for elder dragon hearts acts as a whitelist - if disabled it acts as a blacklist")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elder_dragon_heart_white_list")
    public static Boolean elderDragonHeartWhiteList = false;

    // --- Treasure blocks --- //

    @Translation(key = "treasure_health_regeneration", type = Translation.Type.CONFIGURATION, comments = "Sleeping on treasure blocks will regenerate health if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "treasure_health_regeneration")
    public static Boolean treasureHealthRegen = true;

    @ConfigRange(min = 1, max = /* 1 hour */ 72_000)
    @Translation(key = "treasure_health_regeneration_rate", type = Translation.Type.CONFIGURATION, comments = "The time in ticks (20 ticks = 1 second) it takes to recover 1 health while sleeping on treasure")
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "treasure_health_regeneration_rate")
    public static Integer treasureRegenTicks = Functions.secondsToTicks(14);

    @ConfigRange(min = 1, max = /* 1 hour */ 72_000)
    @Translation(key = "nearby_treasure_rate_reduction", type = Translation.Type.CONFIGURATION, comments = "The amount of ticks (20 ticks = 1 second) each nearby treasure reduces the health regeneration time by")
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "nearby_treasure_rate_reduction")
    public static Integer treasureRegenTicksReduce = 1;

    @ConfigRange(min = 1, max = /* 16 x 9 x 16 hardcoded radius */ 2304)
    @Translation(key = "max_treasure_for_rate_reduction", type = Translation.Type.CONFIGURATION, comments = {
            "The maximum amount of additional treasure that can affect the health regeneration reduction",
            "Only treasure within a 16 x 9 x 16 radius is considered"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "max_treasure_for_rate_reduction")
    public static Integer maxTreasures = 240;

    // --- Source of magic --- //

    @Translation(key = "damage_on_wrong_source_of_magic", type = Translation.Type.CONFIGURATION, comments = "Source of magic that does not match the dragon type will damage the player if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "damage_on_wrong_source_of_magic")
    public static Boolean damageWrongSourceOfMagic = true;

    @ConfigRange(min = 1, max = 10_000)
    @Translation(key = "elder_dragon_dust_time", type = Translation.Type.CONFIGURATION, comments = "Duration (in seconds) of the infinite magic effect when using the elder dragon dust at a source of magic - Note that 10 seconds are spent waiting")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "elder_dragon_dust_time")
    public static Integer elderDragonDustTime = 20;

    @ConfigRange(min = 1, max = 10_000)
    @Translation(key = "elder_dragon_bone_time", type = Translation.Type.CONFIGURATION, comments = "Duration (in seconds) of the infinite magic effect when using the elder dragon bone at a source of magic - Note that 10 seconds are spent waiting")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "elder_dragon_bone_time")
    public static Integer elderDragonBoneTime = 60;

    @ConfigRange(min = 1, max = 10_000)
    @Translation(key = "weak_heart_shard_time", type = Translation.Type.CONFIGURATION, comments = "Duration (in seconds) of the infinite magic effect when using the weak heart shard at a source of magic - Note that 10 seconds are spent waiting")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "weak_heart_shard_time")
    public static Integer weakHeartShardTime = 110;

    @ConfigRange(min = 1, max = 10_000)
    @Translation(key = "weak_dragon_heart_time", type = Translation.Type.CONFIGURATION, comments = "Duration (in seconds) of the infinite magic effect when using the weak dragon heart at a source of magic - Note that 10 seconds are spent waiting")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "weak_dragon_heart_time")
    public static Integer weakDragonHeartTime = 310;

    @ConfigRange(min = 1, max = 100_00)
    @Translation(key = "elder_dragon_heart_time", type = Translation.Type.CONFIGURATION, comments = "Duration (in seconds) of the infinite magic effect when using the elder dragon heart at a source of magic - Note that 10 seconds are spent waiting")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "elder_dragon_heart_time")
    public static Integer elderDragonHeartTime = 1010;

    // --- Penalties --- //

    // FIXME :: is this checked in all places? should we have these global "turn off all" configs at all? seems easy to miss
    @Translation(key = "penalties_enabled", type = Translation.Type.CONFIGURATION, comments = "If disabled all penalties will be turned off")
    @ConfigOption(side = ConfigSide.SERVER, category = "penalties", key = "penalties_enabled")
    public static Boolean penaltiesEnabled = true;

    @Translation(key = "dragons_are_scary", type = Translation.Type.CONFIGURATION, comments = "If enabled animals will try run away from dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "penalties", key = "dragons_are_scary")
    public static Boolean dragonsAreScary = true;

    @Translation(key = "limited_riding", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will be limited to riding the entities in the entity tag 'dragonsurvival:vehicle_whitelist'")
    @ConfigOption(side = ConfigSide.SERVER, category = "penalties", key = "limited_riding")
    public static Boolean limitedRiding = true;

    @ConfigType(Item.class) // FIXME :: handle with tag (and keep this config for regex-only)
    @Translation(key = "blacklisted_items", type = Translation.Type.CONFIGURATION, comments = "Items which dragons are not allowed to use - Format: namespace:path (the path allows regular expressions, e.g. '.*bow')")
    @ConfigOption(side = ConfigSide.SERVER, category = "penalties", key = "blacklisted_items", validation = Validation.RESOURCE_LOCATION_REGEX)
    public static List<String> blacklistedItems = List.of(
            "c:tools/shield",
            "c:tools/bow",
            "c:tools/crossbow",
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

    // --- Ore loot --- //

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "human_ore_dust_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of dust dropping when a human harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "human_ore_dust_chance")
    public static Double humanOreDustChance = 0.1;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "dragon_ore_dust_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of dust dropping when a dragon harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragon_ore_dust_chance")
    public static Double dragonOreDustChance = 0.2;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "human_ore_bone_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of bones dropping when a human harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "human_ore_bone_chance")
    public static Double humanOreBoneChance = 0.0;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "dragon_ore_bone_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of bones dropping when a dragon harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragon_ore_bone_chance")
    public static Double dragonOreBoneChance = 0.01;

    // --- Magic --- //

    @Translation(key = "dragon_abilities", type = Translation.Type.CONFIGURATION, comments = "Enable / Disable dragon abilities")
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities"}, key = "dragon_abilities")
    public static Boolean dragonAbilities = true;

    // TODO :: make the required experience configurable -> probably only when the system gets reworked into datapack registries
    @Translation(key = "no_experience_requirements", type = Translation.Type.CONFIGURATION, comments = "If enabled abilities will be unlocked at their max. level at all times")
    @ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "no_experience_requirements")
    public static Boolean noExperienceRequirements = false;

    @Translation(key = "consume_experience_as_mana", type = Translation.Type.CONFIGURATION, comments = "If enabled experience will be used to substitute for missing mana (10 experience points equals 1 mana point)")
    @ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "consume_experience_as_mana")
    public static Boolean consumeExperienceAsMana = true;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "passive_ability_initial_cost", type = Translation.Type.CONFIGURATION, comments = "The initial experience cost for leveling passive abilities")
    @ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "passive_ability_initial_cost")
    public static Integer initialPassiveCost = 1;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "passive_ability_cost_multiplier", type = Translation.Type.CONFIGURATION, comments = "Multiplier to the experience cost of passive skills")
    @ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "passive_ability_cost_multiplier")
    public static Double passiveScalingCost = 4.0;

    @ConfigRange(min = 1, max = 1000)
    @Translation(key = "favorable_mana_regeneration", type = Translation.Type.CONFIGURATION, comments = "Determines how fast (in ticks) (20 ticks = 1 second) mana is restored in favorable conditions")
    @ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "favorable_mana_regeneration")
    public static Integer favorableManaTicks = 1;

    @ConfigRange(min = 1, max = 1000)
    @Translation(key = "normal_mana_regeneration", type = Translation.Type.CONFIGURATION, comments = "Determines how fast (in ticks) (20 ticks = 1 second) mana is restored in normal conditions")
    @ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "normal_mana_regeneration")
    public static Integer normalManaTicks = 10;

    @Translation(key = "save_all_abilities", type = Translation.Type.CONFIGURATION, comments = {
            "If enabled all abilities will remain when changing dragon types",
            "This does not mean that the other dragon type gains these abilities",
            "It means that when turning to the previous type the abilities will have the same levels"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities"}, key = "save_all_abilities")
    public static Boolean saveAllAbilities = false;

    @Translation(key = "end_void_teleport", type = Translation.Type.CONFIGURATION, comments = "If enabled the player will be teleported to the overworld when they fall into the void in the end dimension")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "end_void_teleport")
    public static Boolean endVoidTeleport = true;

    // --- Dragon hunters --- //

    @ConfigRange(min = 1, max = 1000)
    @Translation(key = "pillager_experience_gain", type = Translation.Type.CONFIGURATION, comments = "How many experience points are gained when stealing from villagers")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_hunters", key = "pillager_experience_gain")
    public static Integer pillageXPGain = 4;

    @ConfigType(EntityType.class) // FIXME :: use tag
    @Translation(key = "gives_hunter_omen_status", type = Translation.Type.CONFIGURATION, comments = "Determines which entities give the 'Hunter Omen' status when killed (aside from villagers)")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_hunters", key = "gives_hunter_omen_status", validation = Validation.RESOURCE_LOCATION)
    public static List<String> hunterOmenStatusGivers = List.of("minecraft:iron_golem");

    @ConfigRange(min = 10d, max = 100)
    @Translation(key = "knight_health", type = Translation.Type.CONFIGURATION, comments = "Amount of health the knight has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_health")
    public static Double knightHealth = 40d;

    @ConfigRange(min = 1d, max = 40)
    @Translation(key = "knight_damage", type = Translation.Type.CONFIGURATION, comments = "Amount of damage the knight deals")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_damage")
    public static Double knightDamage = 12d;

    @ConfigRange(min = 0d, max = 30d)
    @Translation(key = "knight_armor", type = Translation.Type.CONFIGURATION, comments = "Amount of armor the knight has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_armor")
    public static Double knightArmor = 10d;

    @ConfigRange(min = 0.1d, max = 1)
    @Translation(key = "knight_speed", type = Translation.Type.CONFIGURATION, comments = "Knight speed")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_speed")
    public static Double knightSpeed = 0.3d;

    @ConfigRange(min = 0.0d, max = 1d)
    @Translation(key = "knight_shield_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of knights having a shield")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_shield_chance")
    public static Double knightShieldChance = 0.1d;

    @ConfigRange(min = 60, max = 1_200_000)
    @Translation(key = "ambusher_spawn_frequency", type = Translation.Type.CONFIGURATION, comments = "Determines the amount of time (in ticks) (20 ticks = 1 second) that needs to pass be fore another ambusher spawn attempt is made")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "ambusher_spawn_frequency")
    public static int ambusherSpawnAttemptFrequency = Functions.minutesToTicks(10);

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "amusher_spawn_chance", type = Translation.Type.CONFIGURATION, comments = {
            "Determines the chance (in %) of an ambusher spawning",
            "The spawn frequency will reset even if no actual spawn occurs due to this chance not being met"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "amusher_spawn_chance")
    public static double ambusherSpawnChance = 0.2;

    @ConfigRange(min = 10d, max = 100)
    @Translation(key = "ambusher_health", type = Translation.Type.CONFIGURATION, comments = "Amount of health the ambusher has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "ambusher_health")
    public static Double ambusherHealth = 40d;

    @ConfigRange(min = 1, max = 20)
    @Translation(key = "ambusher_damage", type = Translation.Type.CONFIGURATION, comments = "Amount of damage the ambusher deals")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "ambusher_damage")
    public static Integer ambusherDamage = 12;

    @ConfigRange(min = 0d, max = 30d)
    @Translation(key = "ambusher_armor", type = Translation.Type.CONFIGURATION, comments = "Amount of armor the ambusher has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "ambusher_armor")
    public static Double ambusherArmor = 10d;

    @ConfigRange(min = 0.1d, max = 1)
    @Translation(key = "ambusher_speed", type = Translation.Type.CONFIGURATION, comments = "Speed of the ambusher")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "ambusher_speed")
    public static Double ambusherSpeed = 0.3d;

    @ConfigRange(min = AmbusherEntity.CROSSBOW_SHOOT_AND_RELOAD_TIME + 5, max = 1000)
    @Translation(key = "ambusher_attack_interval", type = Translation.Type.CONFIGURATION, comments = "Determines the crossbow attack rate (in ticks) (20 ticks = 1 second) of the ambusher")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "ambusher_attack_interval")
    public static Integer ambusherAttackInterval = 65;

    @ConfigRange(min = 0, max = 10)
    @Translation(key = "spearman_reinforcement_count", type = Translation.Type.CONFIGURATION, comments = "Determines how many spearman reinforce the ambusher when he is attacked")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "spearman_reinforcement_count")
    public static Integer ambusherSpearmanReinforcementCount = 4;

    @ConfigRange(min = 0, max = 10)
    @Translation(key = "hound_reinforcement_count", type = Translation.Type.CONFIGURATION, comments = "Determines how many hounds reinforce the ambusher when he is attacked")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "ambusher"}, key = "hound_reinforcement_count")
    public static Integer ambusherHoundReinforcementCount = 2;

    @ConfigRange(min = 8d, max = 100)
    @Translation(key = "hound_health", type = Translation.Type.CONFIGURATION, comments = "Amount of health the knight hound has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "hound_health")
    public static Double houndHealth = 10d;

    @ConfigRange(min = 1d, max = 20)
    @Translation(key = "hound_damage", type = Translation.Type.CONFIGURATION, comments = "Amount of damage the knight hound deals")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "hound_damage")
    public static Double houndDamage = 2d;

    @ConfigRange(min = 0.1d, max = 1)
    @Translation(key = "hound_speed", type = Translation.Type.CONFIGURATION, comments = "Knight hound speed")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "hound_speed")
    public static Double houndSpeed = 0.45d;

    @ConfigRange(min = 0.1d, max = 1.0d)
    @Translation(key = "hound_slowdown_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of the knight hound applying the slowness effect when they attack")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "hound"}, key = "hound_slowdown_chance")
    public static Double houndSlowdownChance = 0.5d;

    @ConfigRange(min = 8d, max = 100)
    @Translation(key = "griffin_health", type = Translation.Type.CONFIGURATION, comments = "Amount of health of the griffin")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_health")
    public static Double griffinHealth = 10d;

    @ConfigRange(min = 1d, max = 20d)
    @Translation(key = "griffin_damage", type = Translation.Type.CONFIGURATION, comments = "Amount of damage the griffin deals")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_damage")
    public static Double griffinDamage = 2d;

    @ConfigRange(min = 0.1d, max = 1)
    @Translation(key = "griffin_speed", type = Translation.Type.CONFIGURATION, comments = "Speed of the griffin")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_speed")
    public static Double griffinSpeed = 0.2d;

    @ConfigRange(min = 0.1d, max = 2.0d)
    @Translation(key = "griffin_range", type = Translation.Type.CONFIGURATION, comments = "Determines the attack radius of the griffin")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_range")
    public static Double griffinRange = 0.9d;

    @ConfigRange(min = 1.0, max = 60.0)
    @Translation(key = "trapped_effect_duration", type = Translation.Type.CONFIGURATION, comments = "Determines how long (in seconds) the trapped effect lasts")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters"}, key = "trapped_effect_duration")
    public static Double hunterTrappedDebuffDuration = 5.0;

    @ConfigRange(min = 10d, max = 100)
    @Translation(key = "spearman_health", type = Translation.Type.CONFIGURATION, comments = "Amount of health the spearman has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_health")
    public static Double spearmanHealth = 24d;

    @ConfigRange(min = 2d, max = 20d)
    @Translation(key = "spearman_damage", type = Translation.Type.CONFIGURATION, comments = "Amount of damage the spearman deals")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_damage")
    public static Double spearmanDamage = 6d;

    @ConfigRange(min = 0.1d, max = 1)
    @Translation(key = "spearman_speed", type = Translation.Type.CONFIGURATION, comments = "Speed of the spearman")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_speed")
    public static Double spearmanSpeed = 0.35d;

    @ConfigRange(min = 0d, max = 20d)
    @Translation(key = "spearman_armor", type = Translation.Type.CONFIGURATION, comments = "Amount of armor the spearman has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_armor")
    public static Double spearmanArmor = 2d;

    @ConfigRange(min = 0d, max = 20d)
    @Translation(key = "spearman_bonus_horizontal_reach", type = Translation.Type.CONFIGURATION, comments = "Additional horizontal reach for the spearman")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_bonus_horizontal_reach")
    public static Double spearmanBonusHorizontalReach = 0.5d;

    @ConfigRange(min = 0d, max = 20d)
    @Translation(key = "spearman_bonus_vertical_reach", type = Translation.Type.CONFIGURATION, comments = "Additional vertical reach for the spearman")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_bonus_vertical_reach")
    public static Double spearmanBonusVerticalReach = 2.5d;

    @ConfigRange(min = 0.1d, max = 1)
    @Translation(key = "leader_speed", type = Translation.Type.CONFIGURATION, comments = "Speed of the leader")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "leader"}, key = "leader_speed")
    public static Double leaderSpeed = 0.35d;

    @ConfigRange(min = 10d, max = 100)
    @Translation(key = "leader_health", type = Translation.Type.CONFIGURATION, comments = "Amount of health the leader has")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "leader"}, key = "leader_health")
    public static Double leaderHealth = 24d;

    // --- Dragon beacons --- //

    @ConfigRange(min = 1, max = 60 * 60)
    @Translation(key = "beacon_effect_seconds", type = Translation.Type.CONFIGURATION, comments = "The duration (in seconds) of the effects the beacon is constantly applying")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_beacons", key = "beacon_effect_seconds")
    public static Integer secondsOfBeaconEffect = 20;

    @ConfigRange(min = 1, max = 60 * 2)
    @Translation(key = "beacon_effect_minutes", type = Translation.Type.CONFIGURATION, comments = "The duration (in minutes) of the effect when exchanging experience points")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_beacons", key = "beacon_effect_minutes")
    public static Integer minutesOfDragonEffect = 10;
    // FIXME Please help me to fix this config. It doesn't work for buying in exchange for experience in beacon. My fix doesn't help, so I think the problem is deeper than I can solve it.

    @ConfigType(MobEffect.class) // FIXME :: tag
    @Translation(key = "forest_dragon_beacon", type = Translation.Type.CONFIGURATION, comments = "The effects which are granted by the forest dragon beacon")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_beacons", key = "forest_dragon_beacon", validation = Validation.RESOURCE_LOCATION)
    public static List<String> forestDragonBeaconEffects = List.of("dragonsurvival:peace", "dragonsurvival:animal_peace");

    @ConfigType(MobEffect.class) // FIXME :: tag
    @Translation(key = "sea_dragon_beacon", type = Translation.Type.CONFIGURATION, comments = "The effects which are granted by the sea dragon beacon")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_beacons", key = "sea_dragon_beacon", validation = Validation.RESOURCE_LOCATION)
    public static List<String> seaDragonBeaconEffects = List.of("dragonsurvival:magic", "minecraft:haste");

    @ConfigType(MobEffect.class) // FIXME :: tag
    @Translation(key = "fire_beacon_effects", type = Translation.Type.CONFIGURATION, comments = "The effects which are granted by the cave dragon beacon")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_beacons", key = "fire_beacon_effects", validation = Validation.RESOURCE_LOCATION)
    public static List<String> caveDragonBeaconEffects = List.of("dragonsurvival:fire", "dragonsurvival:strong_leather");

    // --- Misc --- //

    @ConfigRange(min = 0, max = 60)
    @Translation(key = "levitation_after_effect", type = Translation.Type.CONFIGURATION, comments = "Determines how long wings stay disabled after the levitation effect has ended")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "levitation_after_effect")
    public static Integer levitationAfterEffect = 3;
}