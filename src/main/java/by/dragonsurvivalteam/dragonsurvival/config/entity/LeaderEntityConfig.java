package by.dragonsurvivalteam.dragonsurvival.config.entity;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

public class LeaderEntityConfig {
    @ConfigRange(min = 1)
    @Translation(key = "leader_health", type = Translation.Type.CONFIGURATION, comments = "Base value for the max health attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "leader"}, key = "leader_health")
    public static double MAX_HEALTH = 24;

    @ConfigRange(min = 0)
    @Translation(key = "leader_movement_speed", type = Translation.Type.CONFIGURATION, comments = "Base value for the movement speed attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "leader"}, key = "leader_movement_speed")
    public static double MOVEMENT_SPEED = 0.35;

    @ConfigRange(min = 0)
    @Translation(key = "leader_armor", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "leader"}, key = "leader_armor")
    public static double ARMOR = 0;

    @ConfigRange(min = 0)
    @Translation(key = "leader_armor_toughness", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor toughness attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "leader"}, key = "leader_armor_toughness")
    public static double ARMOR_TOUGHNESS = 0;

    @ConfigRange(min = 0)
    @Translation(key = "leader_knockback_resistance", type = Translation.Type.CONFIGURATION, comments = "Base value for the knockback resistance attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "leader"}, key = "leader_knockback_resistance")
    public static double KNOCKBACK_RESISTANCE = 0;
}
