package by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

/** Translation keys which are used in multiple places */
public class LangKey {
    /** This is not assigned to one element but rather used at two places which dynamically create the translation key / translation */
    public static final String CATEGORY_PREFIX = Translation.Type.CONFIGURATION.prefix + "category.";

    @Translation(comments = "%s seconds")
    public static final String SECONDS = Translation.Type.GUI.wrap("general.seconds");

    @Translation(comments = "Active Ability")
    public static final String ACTIVE_ABILITY = Translation.Type.GUI.wrap("general.active_ability");

    @Translation(comments = "Passive Ability")
    public static final String PASSIVE_ABILITY = Translation.Type.GUI.wrap("general.passive_ability");

    @Translation(comments = "Penalty")
    public static final String PENALTY = Translation.Type.GUI.wrap("general.penalty");

    @Translation(comments = "Duration: %s")
    public static final String DURATION = Translation.Type.GUI.wrap("general.duration");

    @Translation(comments = "Applied by: %s")
    public static final String APPLIED_BY = Translation.Type.GUI.wrap("general.applied_by");

    // --- GUI --- //

    @Translation(comments = "Cancel")
    public static final String GUI_CANCEL = Translation.Type.GUI.wrap("general.cancel");

    @Translation(comments = "Confirm")
    public static final String GUI_CONFIRM = Translation.Type.GUI.wrap("general.confirm");

    @Translation(comments = "Info")
    public static final String INFO = Translation.Type.GUI.wrap("general.info");

    @Translation(comments = "DRAGON EDITOR")
    public static final String GUI_DRAGON_EDITOR = Translation.Type.GUI.wrap("general.dragon_editor");

    // --- GUI messages --- //

    @Translation(comments = "Hunger has exhausted you, and you can't fly.")
    public static final String MESSAGE_NO_HUNGER = Translation.Type.GUI.wrap("message.no_hunger");

    @Translation(comments = "■ Join our §6discord server§r!§7 Read the Rules, FAQ and Wiki before you ask anything.")
    public static final String DISCORD = Translation.Type.GUI.wrap("general.discord");

    // --- Growth Stage --- //

    @Translation(comments = "■ §6Stage§r§7: ")
    public static final String GROWTH_STAGE = Translation.Type.GUI.wrap("handler.growth_stage");

    @Translation(comments = "■ §6Age§r§7: %s")
    public static final String GROWTH_AGE = Translation.Type.GUI.wrap("handler.growth_age");

    @Translation(comments = "■ §6Size§r§7: %s")
    public static final String GROWTH_SIZE = Translation.Type.GUI.wrap("handler.growth_size");

    @Translation(comments = {
            "\n■ All dragons will gradually grow as time passes, improving their attributes. At certain growth stages, your appearance will change, and your growth will slow.§r",
            "§7■ A Star Bone will revert your growth slightly, and a Star Heart will completely stop you from growing. The biggest dragons can take other players for a ride!§r",
            "§7■ Growth items:§r"
    })
    public static final String GROWTH_INFO = Translation.Type.GUI.wrap("handler.growth_info");

    @Translation(comments = "§6Minimum Size§r§7: %s")
    public static final String GROWTH_STARTING_SIZE = Translation.Type.GUI.wrap("handler.growth_starting_size");

    @Translation(comments = "§6Maximum Size§r§7: %s")
    public static final String GROWTH_MAX_SIZE = Translation.Type.GUI.wrap("handler.growth_max_size");

    @Translation(comments = "§6Time to Grow§r§7: %s")
    public static final String GROWTH_TIME = Translation.Type.GUI.wrap("handler.growth_time");

    @Translation(comments = "§6Can destroy blocks at size§r§7: %s")
    public static final String GROWTH_CAN_DESTROY_BLOCKS = Translation.Type.GUI.wrap("handler.growth_items");

    @Translation(comments = "§6Can crush entities at size§r§7: %s")
    public static final String GROWTH_CAN_CRUSH_ENTITIES = Translation.Type.GUI.wrap("handler.growth_can_crush_entities");

    @Translation(comments = "\n§6--- Modifiers at max. size ---§r§7")
    public static final String GROWTH_MODIFIERS_AT_MAX_SIZE = Translation.Type.GUI.wrap("handler.growth_modifiers_at_max_size");

    // --- Flight data --- //

    @Translation(comments = "You currently cannot fly")
    public static final String FLIGHT_CANNOT_FLY = Translation.Type.GUI.wrap("handler.flight_cannot_fly_or_spin");

    @Translation(comments = "You currently can fly")
    public static final String FLIGHT_CAN_FLY = Translation.Type.GUI.wrap("handler.flight_can_fly");

    @Translation(comments = "You currently can spin")
    public static final String FLIGHT_CAN_SPIN = Translation.Type.GUI.wrap("handler.flight_can_spin");

    // --- Ability effects --- //

    @Translation(comments = " every %s seconds")
    public static final String ABILITY_X_SECONDS = Translation.Type.GUI.wrap("ability.every_x_seconds");

    @Translation(comments = " on hit")
    public static final String ABILITY_ON_HIT = Translation.Type.GUI.wrap("ability.on_hit");

    @Translation(comments = "Applies ")
    public static final String ABILITY_APPLIES = Translation.Type.GUI.wrap("ability.applies");

    @Translation(comments = "§6■ %s §6Damage:§r %s")
    public static final String ABILITY_DAMAGE = Translation.Type.GUI.wrap("ability.x_damage");

    @Translation(comments = "§6■%s§6Explosion Power:§r %s")
    public static final String ABILITY_EXPLOSION_POWER = Translation.Type.GUI.wrap("ability.explosion_power");

    @Translation(comments = "§6■ Cooldown:§r %ss")
    public static final String ABILITY_COOLDOWN = Translation.Type.GUI.wrap("ability.cooldown_of_x");

    @Translation(comments = "§6■ Initial mana cost:§r %s")
    public static final String ABILITY_INITIAL_MANA_COST = Translation.Type.GUI.wrap("ability.initial_mana_cost_of_x");

    @Translation(comments = "§6■ Continuous mana cost:§r %s [%s]")
    public static final String ABILITY_CONTINUOUS_MANA_COST = Translation.Type.GUI.wrap("ability.continuous_mana_cost_of_x");

    @Translation(comments = "§6■ Cast time:§r %ss")
    public static final String ABILITY_CAST_TIME = Translation.Type.GUI.wrap("ability.cast_time_of_x");

    @Translation(comments = " for %s seconds")
    public static final String ABILITY_EFFECT_DURATION = Translation.Type.GUI.wrap("ability.x_effect_duration");

    @Translation(comments = " with a %s chance")
    public static final String ABILITY_EFFECT_CHANCE = Translation.Type.GUI.wrap("ability.effect_chance_of_x");

    // --- Projectile effects --- //

    @Translation(comments = " §6Projectile ")
    public static final String ABILITY_PROJECTILE = Translation.Type.GUI.wrap("ability.projectile");

    // --- Misc --- //

    @Translation(comments = "Kingdom Explorer Map")
    public static final String ITEM_KINGDOM_EXPLORER_MAP = Translation.Type.ITEM.wrap("kingdom_explorer_map");
}
