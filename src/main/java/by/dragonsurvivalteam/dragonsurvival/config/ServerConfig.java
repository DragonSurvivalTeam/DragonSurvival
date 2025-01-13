package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigType;
import by.dragonsurvivalteam.dragonsurvival.config.obj.Validation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {
    ServerConfig(final ModConfigSpec.Builder builder) {
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

    @Translation(key = "start_with_dragon_choice", type = Translation.Type.CONFIGURATION, comments = "If enabled players will be given a choice to select a dragon species when first joining the world")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "start_with_dragon_choice")
    public static Boolean startWithDragonChoice = true;

    @Translation(key = "no_humans_allowed", type = Translation.Type.CONFIGURATION, comments = "If enabled, players will start as a dragon and will never be allowed to be a human.")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "no_humans_allowed")
    public static Boolean noHumansAllowed = false;

    @Translation(key = "allow_dragon_choice_from_inventory", type = Translation.Type.CONFIGURATION, comments = "If enabled players that have not yet chosen a dragon species will be able to do so from the vanilla inventory")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "allow_dragon_choice_from_inventory")
    public static Boolean allowDragonChoiceFromInventory = true;

    @Translation(key = "disable_dragon_suffocation", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will not take suffocation damage")
    @ConfigOption(side = ConfigSide.SERVER, category = "general", key = "disable_dragon_suffocation")
    public static Boolean disableDragonSuffocation = true;

    // --- Large dragon scaling --- //

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "block_destruction_removal", type = Translation.Type.CONFIGURATION, comments = {
            "Determines the percentage chance that a block is removed, bypassing sound or particle effects",
            "This is to avoid potential lag issues due to large amounts of sound effects or particles"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "block_destruction_removal")
    public static Double blockDestructionRemoval = 0.96;

    @ConfigRange(min = 0, max = 20)
    @Translation(key = "crushing_interval", type = Translation.Type.CONFIGURATION, comments = "The amount of ticks (20 ticks = 1 second) before an entity can be crushed again")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushing_interval")
    public static Integer crushingTickDelay = 20;

    // --- Standard dragon scaling --- //

    @Translation(key = "save_growth_stage", type = Translation.Type.CONFIGURATION, comments = "If enabled the current growth will be saved for the current dragon species when changing types or reverting back to being a human")
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

    @Translation(key = "damage_on_wrong_source_of_magic", type = Translation.Type.CONFIGURATION, comments = "Source of magic that does not match the dragon species will damage the player if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "damage_on_wrong_source_of_magic")
    public static Boolean damageWrongSourceOfMagic = true;

    // --- Penalties --- //

    @Translation(key = "dragons_are_scary", type = Translation.Type.CONFIGURATION, comments = "If enabled animals will try run away from dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "penalties", key = "dragons_are_scary")
    public static Boolean dragonsAreScary = true;

    @Translation(key = "limited_riding", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will be limited to riding the entities in the entity tag 'dragonsurvival:vehicle_whitelist'")
    @ConfigOption(side = ConfigSide.SERVER, category = "penalties", key = "limited_riding")
    public static Boolean limitedRiding = true;

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

    @Translation(key = "consume_experience_as_mana", type = Translation.Type.CONFIGURATION, comments = "If enabled experience will be used to substitute for missing mana (10 experience points equals 1 mana point)")
    @ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "consume_experience_as_mana")
    public static Boolean consumeExperienceAsMana = true;

    @Translation(key = "save_all_abilities", type = Translation.Type.CONFIGURATION, comments = {
            "If enabled all abilities will remain when changing dragon species",
            "This does not mean that the other dragon species gains these abilities",
            "It means that when turning to the previous type the abilities will have the same levels"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities"}, key = "save_all_abilities")
    public static Boolean saveAllAbilities = false;

    // --- Dragon hunters --- //

    @ConfigRange(min = 1, max = 1000)
    @Translation(key = "pillager_experience_gain", type = Translation.Type.CONFIGURATION, comments = "How many experience points are gained when stealing from villagers")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_hunters", key = "pillager_experience_gain")
    public static Integer pillageXPGain = 4;

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
}