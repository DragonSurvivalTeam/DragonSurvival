package by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/** Translation keys which are used in multiple places */
public class LangKey {
    /** This is not assigned to one element but rather used at two places which dynamically create the translation key / translation */
    public static final String CATEGORY_PREFIX = Translation.Type.CONFIGURATION.prefix + "category.";

    /** Needed since the tags (and therefor their translations) are dynamically created */
    public static final Function<ResourceLocation, String> FOOD = id -> id.getPath() + "_food";

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

    @Translation(comments = "§8I'll get you to the islands with Ender Dragon magic if you feed me a pearl.")
    public static final String MESSAGE_ANCHOR = Translation.Type.GUI.wrap("message.anchor");

    @Translation(comments = "■ Join our §6discord server§r!§7 Read the Rules, FAQ and Wiki before you ask anything.")
    public static final String DISCORD = Translation.Type.GUI.wrap("general.discord");

    // --- Growth Stage --- //

    @Translation(comments = "■ §6Stage§r§7: ")
    public static final String GROWTH_STAGE = Translation.Type.GUI.wrap("handler.growth_stage");

    @Translation(comments = "■ §6Age§r§7: %s")
    public static final String GROWTH_AGE = Translation.Type.GUI.wrap("handler.growth_age");

    @Translation(comments = "■ §6Growth§r§7: %s")
    public static final String GROWTH_AMOUNT = Translation.Type.GUI.wrap("handler.growth_amount");

    @Translation(comments = {
            "\n■ All dragons will gradually grow as time passes, improving their attributes. At certain growth stages, your appearance will change, and your growth will slow.§r",
            "§7■ Certain items can speed up or reduce your growth.\n§r",
            "■ §6Growth items§r§7:§r"
    })
    public static final String GROWTH_INFO = Translation.Type.GUI.wrap("handler.growth_info");

    @Translation(comments = "§6Minimum Growth§r§7: %s")
    public static final String GROWTH_STARTING_AMOUNT = Translation.Type.GUI.wrap("handler.growth_starting_amount");

    @Translation(comments = "§6Maximum Growth§r§7: %s")
    public static final String GROWTH_MAX_AMOUNT = Translation.Type.GUI.wrap("handler.growth_max_amount");

    @Translation(comments = "§6Time to Grow§r§7: %s")
    public static final String GROWTH_TIME = Translation.Type.GUI.wrap("handler.growth_time");

    @Translation(comments = "§6Can destroy blocks at growth§r§7: %s")
    public static final String GROWTH_CAN_DESTROY_BLOCKS = Translation.Type.GUI.wrap("handler.growth_items");

    @Translation(comments = "§6Can crush entities at growth§r§7: %s")
    public static final String GROWTH_CAN_CRUSH_ENTITIES = Translation.Type.GUI.wrap("handler.growth_can_crush_entities");

    @Translation(comments = "\n§6--- Modifiers at max. growth ---§r§7")
    public static final String GROWTH_MODIFIERS_AT_MAX_GROWTH = Translation.Type.GUI.wrap("handler.growth_modifiers_at_max_growth");

    // --- Ability effects --- //

    @Translation(comments = " every %s seconds")
    public static final String ABILITY_X_SECONDS = Translation.Type.GUI.wrap("ability.every_x_seconds");

    @Translation(comments = " on hit")
    public static final String ABILITY_ON_HIT = Translation.Type.GUI.wrap("ability.on_hit");

    @Translation(comments = "§6■ Applies %s")
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

    @Translation(comments = "All blocks")
    public static final String ALL_BLOCKS = Translation.Type.GUI.wrap("ability.all_blocks");

    @Translation(comments = "Various Blocks (%s)")
    public static final String VARIOUS_BLOCKS = Translation.Type.GUI.wrap("ability.various_blocks");

    @Translation(comments = "None")
    public static final String NONE = Translation.Type.GUI.wrap("ability.none");

    // --- Projectile effects --- //

    @Translation(comments = " §6Projectile ")
    public static final String ABILITY_PROJECTILE = Translation.Type.GUI.wrap("ability.projectile");

    // --- Misc --- //

    @Translation(comments = "Kingdom Explorer Map")
    public static final String ITEM_KINGDOM_EXPLORER_MAP = Translation.Type.ITEM.wrap("kingdom_explorer_map");
}
