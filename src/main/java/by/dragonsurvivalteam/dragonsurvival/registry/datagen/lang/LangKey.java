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

    @Translation(type = Translation.Type.MISC, comments = "Next level requires %s experience points (about level %s)")
    public static final String ABILITY_LEVEL_MANUAL_UPGRADE = Translation.Type.ABILITY.wrap("general.ability_level_manual_upgrade");

    @Translation(type = Translation.Type.MISC, comments = "Next level will be unlocked at experience level %s")
    public static final String ABILITY_LEVEL_AUTO_UPGRADE = Translation.Type.ABILITY.wrap("general.ability_level_auto_upgrade");

    @Translation(type = Translation.Type.MISC, comments = "Next level will be unlocked at size %s")
    public static final String ABILITY_GROWTH_AUTO_UPGRADE = Translation.Type.ABILITY.wrap("general.ability_growth_auto_upgrade");

    @Translation(type = Translation.Type.MISC, comments = "self")
    public static final String ABILITY_TARGET_SELF = Translation.Type.ABILITY.wrap("general.ability_target_self");

    @Translation(type = Translation.Type.MISC, comments = " to %s")
    public static final String ABILITY_TO_TARGET = Translation.Type.ABILITY.wrap("general.ability_to_target");

    @Translation(type = Translation.Type.MISC, comments = " in a %s block radius")
    public static final String ABILITY_AREA = Translation.Type.ABILITY.wrap("general.ability_area");

    @Translation(type = Translation.Type.MISC, comments = " to %s in a %s block radius")
    public static final String ABILITY_TO_TARGET_AREA = Translation.Type.ABILITY.wrap("general.ability_to_target_area");

    @Translation(type = Translation.Type.MISC, comments = " in a %s block cone")
    public static final String ABILITY_CONE = Translation.Type.ABILITY.wrap("general.ability_cone");

    @Translation(type = Translation.Type.MISC, comments = " to %s in a %s block cone")
    public static final String ABILITY_TO_TARGET_CONE = Translation.Type.ABILITY.wrap("general.ability_to_target_cone");

    @Translation(type = Translation.Type.MISC, comments = " to %s being looked at ")
    public static final String ABILITY_LOOKAT = Translation.Type.ABILITY.wrap("general.ability_lookat");

    @Translation(type = Translation.Type.MISC, comments = " to %s within %s blocks being looked at")
    public static final String ABILITY_TO_TARGET_LOOKAT = Translation.Type.ABILITY.wrap("general.ability_to_target_lookat");

    @Translation(type = Translation.Type.MISC, comments = " every %s seconds")
    public static final String ABILITY_X_SECONDS = Translation.Type.ABILITY.wrap("general.ability_x_seconds");

    @Translation(type = Translation.Type.MISC, comments = " on hit")
    public static final String ABILITY_ON_HIT = Translation.Type.ABILITY.wrap("general.on_hit");

    @Translation(type = Translation.Type.MISC, comments = "Applies ")
    public static final String ABILITY_APPLIES = Translation.Type.ABILITY.wrap("general.applies");

    @Translation(type = Translation.Type.MISC, comments = "§6■ %s §6Damage:§r %s")
    public static final String ABILITY_DAMAGE = Translation.Type.ABILITY.wrap("general.damage");

    @Translation(type = Translation.Type.MISC, comments = "§6■%s§6Explosion Power:§r %s")
    public static final String ABILITY_EXPLOSION_POWER = Translation.Type.ABILITY.wrap("general.explosion_power");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Cooldown:§r %ss")
    public static final String ABILITY_COOLDOWN = Translation.Type.ABILITY.wrap("general.cooldown");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Initial mana cost:§r %s")
    public static final String ABILITY_INITIAL_MANA_COST = Translation.Type.ABILITY.wrap("general.initial_mana_cost");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Continuous mana cost:§r %s")
    public static final String ABILITY_CONTINUOUS_MANA_COST = Translation.Type.ABILITY.wrap("general.continuous_mana_cost");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Cast time:§r %ss")
    public static final String ABILITY_CAST_TIME = Translation.Type.ABILITY.wrap("general.cast_time");

    @Translation(type = Translation.Type.MISC, comments = " for %s seconds")
    public static final String ABILITY_EFFECT_DURATION = Translation.Type.ABILITY.wrap("general.effect_duration");

    @Translation(type = Translation.Type.MISC, comments = " with a %s chance")
    public static final String ABILITY_EFFECT_CHANCE = Translation.Type.ABILITY.wrap("general.effect_chance");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Harvest Level Bonus:§r %s")
    public static final String ABILITY_HARVEST_LEVEL_BONUS = Translation.Type.ABILITY.wrap("general.harvest_level_bonus");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Immune§r to ")
    public static final String ABILITY_IMMUNITY = Translation.Type.ABILITY.wrap("general.immunity");

    @Translation(type = Translation.Type.MISC, comments = "§6■ %s% §r reduced damage taken from ")
    public static final String ABILITY_DAMAGE_REDUCTION = Translation.Type.ABILITY.wrap("general.damage_reduction");

    @Translation(type = Translation.Type.MISC, comments = "§6■ %s% §r increased damage taken from ")
    public static final String ABILITY_DAMAGE_INCREASE = Translation.Type.ABILITY.wrap("general.damage_increase");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can use flight")
    public static final String ABILITY_FLIGHT = Translation.Type.ABILITY.wrap("general.flight");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can use spin")
    public static final String ABILITY_SPIN = Translation.Type.ABILITY.wrap("general.spin");

    @Translation(type = Translation.Type.MISC, comments = " after %s seconds")
    public static final String PENALTY_SUPPLY_TRIGGER = Translation.Type.PENALTY.wrap("general.supply_trigger");

    // --- Projectile effects --- //

    @Translation(type = Translation.Type.MISC, comments = " §6Projectile ")
    public static final String ABILITY_PROJECTILE = Translation.Type.ABILITY.wrap("general.projectile");

    // Needed because of formatting messiness when combining type and projectile together
    @Translation(type = Translation.Type.MISC, comments = "§6■ %s §6Projectile Damage:§r %s")
    public static final String ABILITY_PROJECTILE_DAMAGE = Translation.Type.ABILITY.wrap("general.projectile_damage");

    // --- Misc --- //

    @Translation(type = Translation.Type.MISC, comments = "Kingdom Explorer Map")
    public static final String ITEM_KINGDOM_EXPLORER_MAP = Translation.Type.ITEM.wrap("kingdom_explorer_map");
}
