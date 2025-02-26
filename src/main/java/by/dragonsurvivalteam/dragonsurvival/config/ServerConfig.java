package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    ServerConfig(final ModConfigSpec.Builder builder) {
        ConfigHandler.createConfigEntries(builder, ConfigSide.SERVER);
    }

    @Translation(key = "force_vault_state_updates", type = Translation.Type.CONFIGURATION, comments = "If enabled vaults will immediately update their state")
    @ConfigOption(side = ConfigSide.SERVER, category = "debug", key = "force_vault_state_updates")
    public static Boolean forceStateUpdatingOnVaults = false;

    @ConfigRange(min = 0)
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

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "crushing_size_ratio", type = Translation.Type.CONFIGURATION, comments = "The size ratio between the entity and the crusher for crushing to occur")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth", "big_dragon"}, key = "crushing_size_ratio")
    public static Double crushingSizeRatio = 0.25;

    // --- Standard dragon scaling --- //

    @Translation(key = "save_growth_stage", type = Translation.Type.CONFIGURATION, comments = "If enabled the current growth will be saved for the current dragon species when changing types or reverting back to being a human")
    @ConfigOption(side = ConfigSide.SERVER, category = {"growth"}, key = "save_growth_stage")
    public static Boolean saveGrowthStage = false;

    // --- Item drops --- //

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "dragon_heart_shard_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of dragon heart shards dropping from entities with a maximum health between 14 and 20")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragon_heart_shard_chance")
    public static Double dragonHeartShardChance = 0.03;

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "weak_dragon_heart_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of weak dragon hearts dropping from entities with a maximum health between 20 and 50")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weak_dragon_heart_chance")
    public static Double weakDragonHeartChance = 0.01;

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "elder_dragon_heart_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of elder dragon hearts dropping from entities with a maximum health above 50")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elder_dragon_heart_chance")
    public static Double elderDragonHeartChance = 0.01;

    @Translation(key = "dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "If enabled the entity list for dragon hearts acts as a whitelist - if disabled it acts as a blacklist")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragon_heart_white_list")
    public static Boolean dragonHeartWhiteList = false;

    @Translation(key = "weak_dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "If enabled the entity list for weak dragon hearts acts as a whitelist - if disabled it acts as a blacklist")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weak_dragon_heart_white_list")
    public static Boolean weakDragonHeartWhiteList = false;

    @Translation(key = "elder_dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "If enabled the entity list for elder dragon hearts acts as a whitelist - if disabled it acts as a blacklist")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elder_dragon_heart_white_list")
    public static Boolean elderDragonHeartWhiteList = false;

    // --- Source of magic --- //

    @Translation(key = "damage_on_wrong_source_of_magic", type = Translation.Type.CONFIGURATION, comments = "Source of magic that does not match the dragon species will damage the player if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "source_of_magic", key = "damage_on_wrong_source_of_magic")
    public static Boolean damageWrongSourceOfMagic = true;

    // --- Penalties --- //

    @Translation(key = "limited_riding", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will be limited to riding the entities in the entity tag 'dragonsurvival:vehicle_whitelist'")
    @ConfigOption(side = ConfigSide.SERVER, category = "penalties", key = "limited_riding")
    public static Boolean limitedRiding = true;

    // --- Magic --- //

    @Translation(key = "save_all_abilities", type = Translation.Type.CONFIGURATION, comments = {
            "If enabled all abilities will remain when changing dragon species",
            "This does not mean that the other dragon species gains these abilities",
            "It means that when turning to the previous type the abilities will have the same levels"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities"}, key = "save_all_abilities")
    public static Boolean saveAllAbilities = false;

    // --- Dragon hunters --- //

    @ConfigRange(min = 0)
    @Translation(key = "pillager_experience_gain", type = Translation.Type.CONFIGURATION, comments = "How many experience points are gained when stealing from villagers")
    @ConfigOption(side = ConfigSide.SERVER, category = "dragon_hunters", key = "pillager_experience_gain")
    public static Integer pillageXPGain = 4;

    @ConfigRange(min = 0)
    @Translation(key = "trapped_effect_duration", type = Translation.Type.CONFIGURATION, comments = "Determines how long (in seconds) the trapped effect lasts")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters"}, key = "trapped_effect_duration")
    public static Double hunterTrappedDebuffDuration = 5.0;
}