package by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

/** Translation keys which are used in multiple places */
public class LangKey {
    /** This is not assigned to one element but rather used at two places which dynamically create the translation key / translation */
    public static final String CATEGORY_PREFIX = Translation.Type.CONFIGURATION.prefix + "category.";

    // --- GUI --- //

    @Translation(type = Translation.Type.MISC, comments = "Cancel")
    public static final String GUI_CANCEL = Translation.Type.GUI.wrap("general.cancel");

    @Translation(type = Translation.Type.MISC, comments = "Confirm")
    public static final String GUI_CONFIRM = Translation.Type.GUI.wrap("general.confirm");

    @Translation(type = Translation.Type.MISC, comments = "Glowing")
    public static final String GUI_GLOWING = Translation.Type.GUI.wrap("general.glowing");

    @Translation(type = Translation.Type.MISC, comments = "DRAGON EDITOR")
    public static final String GUI_DRAGON_EDITOR = Translation.Type.GUI.wrap("general.dragon_editor");

    // --- GUI messages --- //

    @Translation(type = Translation.Type.MISC, comments = "Hunger has exhausted you, and you can't fly.")
    public static final String MESSAGE_NO_HUNGER = Translation.Type.GUI.wrap("message.no_hunger");

    // --- Growth Stage --- //

    @Translation(type = Translation.Type.MISC, comments = "■ §6Stage§r§7: ")
    public static final String GROWTH_STAGE = Translation.Type.GUI.wrap("handler.growth_stage");

    @Translation(type = Translation.Type.MISC, comments = "■ §6Age§r§7: %s")
    public static final String GROWTH_AGE = Translation.Type.GUI.wrap("handler.growth_age");

    @Translation(type = Translation.Type.MISC, comments = {
            "\n■ All dragons will gradually grow as time passes, improving their attributes. At certain growth stages, your appearance will change, and your growth will slow.§r",
            "§7■ A Star Bone will revert your growth slightly, and a Star Heart will completely stop you from growing. The biggest dragons can take other players for a ride!§r",
            "§7■ Growth items:§r"
    })
    public static final String GROWTH_INFO = Translation.Type.GUI.wrap("handler.growth_info");

    @Translation(type = Translation.Type.MISC, comments = "§6Minimum Size§r§7: %s")
    public static final String GROWTH_STARTING_SIZE = Translation.Type.GUI.wrap("handler.growth_starting_size");

    @Translation(type = Translation.Type.MISC, comments = "§6Maximum Size§r§7: %s")
    public static final String GROWTH_MAX_SIZE = Translation.Type.GUI.wrap("handler.growth_max_size");

    @Translation(type = Translation.Type.MISC, comments = "§6Time to Grow§r§7: %s")
    public static final String GROWTH_TIME = Translation.Type.GUI.wrap("handler.growth_time");

    @Translation(type = Translation.Type.MISC, comments = "§6Harvest Level Bonus§r§7: %s")
    public static final String GROWTH_HARVEST_LEVEL_BONUS = Translation.Type.GUI.wrap("handler.growth_harvest_level_bonus");

    @Translation(type = Translation.Type.MISC, comments = "§6Break Speed Multiplier§r§7: %sx")
    public static final String GROWTH_BREAK_SPEED_MULTIPLIER = Translation.Type.GUI.wrap("handler.growth_break_speed_multiplier");

    @Translation(type = Translation.Type.MISC, comments = "§6Can destroy blocks at size§r§7: %s")
    public static final String GROWTH_CAN_DESTROY_BLOCKS = Translation.Type.GUI.wrap("handler.growth_items");

    @Translation(type = Translation.Type.MISC, comments = "§6Can crush entities at size§r§7: %s")
    public static final String GROWTH_CAN_CRUSH_ENTITIES = Translation.Type.GUI.wrap("handler.growth_can_crush_entities");

    @Translation(type = Translation.Type.MISC, comments = "§6--- Modifiers at Max Size ---§r§7: ")
    public static final String GROWTH_MODIFIERS_AT_MAX_SIZE = Translation.Type.GUI.wrap("handler.growth_modifiers_at_max_size");

    // --- Flight data --- //

    @Translation(type = Translation.Type.MISC, comments = "You currently cannot fly")
    public static final String FLIGHT_CANNOT_FLY = Translation.Type.GUI.wrap("handler.flight_cannot_fly_or_spin");

    @Translation(type = Translation.Type.MISC, comments = "You currently can fly")
    public static final String FLIGHT_CAN_FLY = Translation.Type.GUI.wrap("handler.flight_can_fly");

    @Translation(type = Translation.Type.MISC, comments = "You currently can spin")
    public static final String FLIGHT_CAN_SPIN = Translation.Type.GUI.wrap("handler.flight_can_spin");

    // --- Ability effects --- //

    @Translation(type = Translation.Type.MISC, comments = "Next level requires %s experience points (roughly level %s)")
    public static final String ABILITY_LEVEL_MANUAL_UPGRADE = Translation.Type.ABILITY.wrap("general.ability.level_manual_upgrade");

    @Translation(type = Translation.Type.MISC, comments = "Next level will be unlocked at experience level %s")
    public static final String ABILITY_LEVEL_AUTO_UPGRADE = Translation.Type.ABILITY.wrap("general.ability.level_auto_upgrade");

    @Translation(type = Translation.Type.MISC, comments = "Next level will be unlocked at size %s")
    public static final String ABILITY_GROWTH_AUTO_UPGRADE = Translation.Type.ABILITY.wrap("general.ability.growth_auto_upgrade");

    @Translation(type = Translation.Type.MISC, comments = "self")
    public static final String ABILITY_TARGET_SELF = Translation.Type.ABILITY.wrap("general.ability.target_self");

    @Translation(type = Translation.Type.MISC, comments = " to %s")
    public static final String ABILITY_TO_TARGET = Translation.Type.ABILITY.wrap("general.ability.to_target");

    @Translation(type = Translation.Type.MISC, comments = " in a %s block radius")
    public static final String ABILITY_AREA = Translation.Type.ABILITY.wrap("general.ability.area");

    @Translation(type = Translation.Type.MISC, comments = " to %s in a %s block radius")
    public static final String ABILITY_TO_TARGET_AREA = Translation.Type.ABILITY.wrap("general.ability.to_target_area");

    @Translation(type = Translation.Type.MISC, comments = " in a %s block cone")
    public static final String ABILITY_CONE = Translation.Type.ABILITY.wrap("general.ability.cone");

    @Translation(type = Translation.Type.MISC, comments = " to %s in a %s block cone")
    public static final String ABILITY_TO_TARGET_CONE = Translation.Type.ABILITY.wrap("general.ability.to_target_cone");

    @Translation(type = Translation.Type.MISC, comments = " to a block that you are looking at within a range of %s blocks")
    public static final String ABILITY_LOOK_AT = Translation.Type.ABILITY.wrap("general.ability.look_at");

    // TODO :: 'to enemies that you are looking at (...)' -> it's to a singular enemy not all, unsure how to properly specify
    @Translation(type = Translation.Type.MISC, comments = " to %s that you are looking at within a range of %s blocks")
    public static final String ABILITY_LOOK_AT_TARGET = Translation.Type.ABILITY.wrap("general.ability.look_at_target");

    @Translation(type = Translation.Type.MISC, comments = " every %s seconds")
    public static final String ABILITY_X_SECONDS = Translation.Type.ABILITY.wrap("general.ability.x_seconds");

    @Translation(type = Translation.Type.MISC, comments = " on hit")
    public static final String ABILITY_ON_HIT = Translation.Type.ABILITY.wrap("general.ability.on_hit");

    @Translation(type = Translation.Type.MISC, comments = "Applies ")
    public static final String ABILITY_APPLIES = Translation.Type.ABILITY.wrap("general.ability.applies");

    @Translation(type = Translation.Type.MISC, comments = "§6■ %s §6Damage:§r %s")
    public static final String ABILITY_DAMAGE = Translation.Type.ABILITY.wrap("general.ability.damage");

    @Translation(type = Translation.Type.MISC, comments = "§6■%s§6Explosion Power:§r %s")
    public static final String ABILITY_EXPLOSION_POWER = Translation.Type.ABILITY.wrap("general.ability.explosion_power");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Cooldown:§r %ss")
    public static final String ABILITY_COOLDOWN = Translation.Type.ABILITY.wrap("general.ability.cooldown");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Initial mana cost:§r %s")
    public static final String ABILITY_INITIAL_MANA_COST = Translation.Type.ABILITY.wrap("general.ability.initial_mana_cost");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Continuous mana cost:§r %s")
    public static final String ABILITY_CONTINUOUS_MANA_COST = Translation.Type.ABILITY.wrap("general.ability.continuous_mana_cost");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Cast time:§r %ss")
    public static final String ABILITY_CAST_TIME = Translation.Type.ABILITY.wrap("general.ability.cast_time");

    @Translation(type = Translation.Type.MISC, comments = " for %s seconds")
    public static final String ABILITY_EFFECT_DURATION = Translation.Type.ABILITY.wrap("general.ability.effect_duration");

    @Translation(type = Translation.Type.MISC, comments = " with a %s chance")
    public static final String ABILITY_EFFECT_CHANCE = Translation.Type.ABILITY.wrap("general.ability.effect_chance");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Harvest Level Bonus:§r %s")
    public static final String ABILITY_HARVEST_LEVEL_BONUS = Translation.Type.ABILITY.wrap("general.ability.harvest_level_bonus");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Immune§r to ")
    public static final String ABILITY_IMMUNITY = Translation.Type.ABILITY.wrap("general.ability.immunity");

    @Translation(type = Translation.Type.MISC, comments = "§6■ %s% §r reduced damage taken from ")
    public static final String ABILITY_DAMAGE_REDUCTION = Translation.Type.ABILITY.wrap("general.ability.damage_reduction");

    @Translation(type = Translation.Type.MISC, comments = "§6■ %s% §r increased damage taken from ")
    public static final String ABILITY_DAMAGE_INCREASE = Translation.Type.ABILITY.wrap("general.ability.damage_increase");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can use flight")
    public static final String ABILITY_FLIGHT = Translation.Type.ABILITY.wrap("general.ability.flight");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can use spin")
    public static final String ABILITY_SPIN = Translation.Type.ABILITY.wrap("general.ability.spin");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can summon up to§r %s §6entities:§r")
    public static final String ABILITY_SUMMON = Translation.Type.ABILITY.wrap("general.ability.summon");

    @Translation(type = Translation.Type.MISC, comments = "\n- %s (%s chance)")
    public static final String ABILITY_SUMMON_CHANCE = Translation.Type.ABILITY.wrap("general.ability.summon_chance");

    @Translation(type = Translation.Type.MISC, comments = " after %s seconds")
    public static final String PENALTY_SUPPLY_TRIGGER = Translation.Type.PENALTY.wrap("general.penalty.supply_trigger");

    // --- Projectile effects --- //

    @Translation(type = Translation.Type.MISC, comments = " §6Projectile ")
    public static final String ABILITY_PROJECTILE = Translation.Type.ABILITY.wrap("general.projectile");

    // Needed because of formatting messiness when combining type and projectile together
    @Translation(type = Translation.Type.MISC, comments = "§6■ %s §6Projectile Damage:§r %s")
    public static final String ABILITY_PROJECTILE_DAMAGE = Translation.Type.ABILITY.wrap("general.projectile_damage");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Number of projectiles:§r %s")
    public static final String ABILITY_PROJECTILE_COUNT = Translation.Type.ABILITY.wrap("general.projectile_count");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Projectile Speed:§r %s")
    public static final String ABILITY_PROJECTILE_SPEED = Translation.Type.ABILITY.wrap("general.projectile_speed");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Projectile Spread:§r %s")
    public static final String ABILITY_PROJECTILE_SPREAD = Translation.Type.ABILITY.wrap("general.projectile_spread");

    // --- Misc --- //

    @Translation(type = Translation.Type.MISC, comments = "Kingdom Explorer Map")
    public static final String ITEM_KINGDOM_EXPLORER_MAP = Translation.Type.ITEM.wrap("kingdom_explorer_map");
}
