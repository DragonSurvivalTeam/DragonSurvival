package by.dragonsurvivalteam.dragonsurvival.client.gui;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

import java.util.List;

@SuppressWarnings("NonFinalFieldInEnum") // final fields cause issues with the config handler
public enum DisplayType {
    HIDDEN, INVENTORY, GAME, INVENTORY_AND_GAME;

    @Translation(key = "ability_effect_display_type", type = Translation.Type.CONFIGURATION, comments = "Determines how (or whether) ability effects should be displayed")
    @ConfigOption(side = ConfigSide.CLIENT, category = "effects", key = "ability_effect_display_type")
    public static DisplayType DISPLAY_TYPE = INVENTORY_AND_GAME;

    @Translation(key = "ability_effect_always_visible", type = Translation.Type.CONFIGURATION, comments = "Bypasses the set display type in case certain effects should be always visible")
    @ConfigOption(side = ConfigSide.CLIENT, category = "effects", key = "ability_effect_always_visible")
    public static List<String> ALWAYS_VISIBLE = List.of("dragonsurvival:dragon_wings", "dragonsurvival:ender_dragon_curse");

    public static boolean isVisible(boolean isInventory) {
        return switch (DISPLAY_TYPE) {
            case HIDDEN -> false;
            case INVENTORY_AND_GAME -> true;
            case INVENTORY -> isInventory;
            case GAME -> !isInventory;
        };
    }
}
