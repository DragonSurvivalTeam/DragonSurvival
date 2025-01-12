package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    ClientConfig(ModConfigSpec.Builder builder) {
        ConfigHandler.createConfigEntries(builder, ConfigSide.CLIENT);
    }

    @Translation(key = "alternate_cast_mode", type = Translation.Type.CONFIGURATION, comments = {"If enabled abilities will be cast by pressing their respective keybinds", "If disabled the global casting keybind will be used"})
    @ConfigOption(side = ConfigSide.CLIENT, category = "misc", key = "alternate_cast_mode")
    public static Boolean alternateCastMode = false;

    @Translation(key = "stable_night_vision", type = Translation.Type.CONFIGURATION, comments = "If enabled night vision will no longer flicker when on a low duration")
    @ConfigOption(side = ConfigSide.CLIENT, category = "misc", key = "stable_night_vision")
    public static Boolean stableNightVision = true;

    @ConfigRange(min = 0.0, max = 1.0) // FIXME :: rework comment (unclear what this exactly does / what changing animation speed results in)
    @Translation(key = "small_size_animation_speed_factor", type = Translation.Type.CONFIGURATION, comments = "The factor by which the additional animation speed from being smaller is multiplied. 1.0 represents the speed accurately reflecting the size of the dragon.")
    @ConfigOption(side = ConfigSide.CLIENT, category = "animation", key = "small_size_animation_speed_factor")
    public static Double smallSizeAnimationSpeedFactor = 0.3;

    @ConfigRange(min = 0.0, max = 1.0) // FIXME :: rework comment (unclear what this exactly does / what changing animation speed results in)
    @Translation(key = "large_size_animation_speed_factor", type = Translation.Type.CONFIGURATION, comments = "The factor by which the reduced additional animation speed from being bigger is multiplied. 1.0 represents the speed accurately reflecting the size of the dragon.")
    @ConfigOption(side = ConfigSide.CLIENT, category = "animation", key = "large_size_animation_speed_factor")
    public static Double largeSizeAnimationSpeedFactor = 1.0;

    @ConfigRange(min = 0.0, max = 1.0) // FIXME :: rework comment (unclear what this exactly does / what changing animation speed results in)
    @Translation(key = "movement_animation_speed_factor", type = Translation.Type.CONFIGURATION, comments = "The amount by which the movement animation speed factor is multiplied. 1.0 represents the animation speed accurately reflecting the speed of your movement.")
    @ConfigOption(side = ConfigSide.CLIENT, category = "animation", key = "movement_animation_speed_factor")
    public static Double movementAnimationSpeedFactor = 1.0;

    @ConfigRange(min = 0.0, max = 10.0) // FIXME :: rework comment (unclear what this exactly does / what changing animation speed results in)
    @Translation(key = "max_animation_speed_factor", type = Translation.Type.CONFIGURATION, comments = "The maximum value that the speed factor can add to the base animation speed.")
    @ConfigOption(side = ConfigSide.CLIENT, category = "animation", key = "max_animation_speed_factor")
    public static Double maxAnimationSpeedFactor = 3.0;

    @ConfigRange(min = 1.0, max = 5.0) // FIXME :: rework comment (unclear what this exactly does / what changing animation speed results in)
    @Translation(key = "max_animation_speed", type = Translation.Type.CONFIGURATION, comments = "The maximum animation speed allowed for dragons.")
    @ConfigOption(side = ConfigSide.CLIENT, category = "animation", key = "max_animation_speed")
    public static Double maxAnimationSpeed = 1.5;

    @ConfigRange(min = 0.05, max = 1.0) // FIXME :: rework comment (unclear what this exactly does / what changing animation speed results in)
    @Translation(key = "min_animation_speed", type = Translation.Type.CONFIGURATION, comments = "The minimum animation speed allowed for dragons.")
    @ConfigOption(side = ConfigSide.CLIENT, category = "animation", key = "min_animation_speed")
    public static Double minAnimationSpeed = 0.2;

    @Translation(key = "force_cpu_skin_generation", type = Translation.Type.CONFIGURATION, comments = {"If enabled the skin generation will occur on the CPU instead of GPU", "Should only be needed if there are issues with your current graphics driver / graphics card"})
    @ConfigOption(side = ConfigSide.CLIENT, category = "misc", key = "force_cpu_skin_generation")
    public static Boolean forceCPUSkinGeneration = false;

    @Translation(key = "particles_on_dragons", type = Translation.Type.CONFIGURATION, comments = "If enabled particles (from dragon effects) will be rendered when dragons are affected from said effects")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "particles_on_dragons")
    public static Boolean particlesOnDragons = false;
}