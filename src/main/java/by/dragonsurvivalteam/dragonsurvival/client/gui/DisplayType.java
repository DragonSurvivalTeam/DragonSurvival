package by.dragonsurvivalteam.dragonsurvival.client.gui;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

public enum DisplayType {
    HIDDEN, INVENTORY, GAME, INVENTORY_AND_GAME;

    @SuppressWarnings("NonFinalFieldInEnum") // final fields cause issues with the config handler
    @Translation(key = "ability_effect_display_type", type = Translation.Type.CONFIGURATION, comments = "Determines how (or whether) ability effects should be displayed")
    @ConfigOption(side = ConfigSide.CLIENT, category = "ui", key = "ability_effect_display_type")
    public static DisplayType DISPLAY_TYPE = INVENTORY_AND_GAME;

    public static boolean isVisible(boolean isInventory) {
        return switch (DISPLAY_TYPE) {
            case HIDDEN -> false;
            case INVENTORY_AND_GAME -> true;
            case INVENTORY -> isInventory;
            case GAME -> !isInventory;
        };
    }
}
