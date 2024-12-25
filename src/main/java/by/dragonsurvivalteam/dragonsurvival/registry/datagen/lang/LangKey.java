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

    @Translation(type = Translation.Type.MISC, comments = " to %s being looked at ")
    public static final String ABILITY_LOOKAT = Translation.Type.ABILITY.wrap("general.ability.lookat");

    @Translation(type = Translation.Type.MISC, comments = " to %s within %s blocks being looked at")
    public static final String ABILITY_TO_TARGET_LOOKAT = Translation.Type.ABILITY.wrap("general.ability.to_target_lookat");

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

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can summon up to %s entities:§r")
    public static final String ABILITY_SUMMON = Translation.Type.ABILITY.wrap("general.ability.summon");

    @Translation(type = Translation.Type.MISC, comments = "- %s with a chance of %s")
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
