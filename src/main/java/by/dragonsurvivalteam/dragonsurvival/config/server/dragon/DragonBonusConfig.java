package by.dragonsurvivalteam.dragonsurvival.config.server.dragon;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

public class DragonBonusConfig {
    @ConfigRange(min = 1, max = 10)
    @Translation(key = "break_speed_reduction", type = Translation.Type.CONFIGURATION, comments = {
            "The break speed multiplier of the dragon level will be divided by this value if:",
            "- The block is not part of the harvestable blocks of that dragon type",
            "- The dragon has a valid tool in the claw inventory"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "break_speed_reduction")
    public static Float breakSpeedReduction = 2f;
}
