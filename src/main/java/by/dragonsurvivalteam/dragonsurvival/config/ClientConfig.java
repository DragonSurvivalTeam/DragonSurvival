package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig{
	@ConfigOption(side = ConfigSide.CLIENT, category = "misc", key = "alternateCastMode", comment = "Should the cast mode where you click the keybind to cast be used?")
	public static Boolean alternateCastMode = false;

	ClientConfig(ModConfigSpec.Builder builder){
		ConfigHandler.addConfigs(builder, ConfigSide.CLIENT);
	}

	@ConfigOption(side = ConfigSide.CLIENT, category = "misc", key = "renderBreathRange", comment = "Whether the range of the breath should be rendered (while hitboxes are shown)")
	public static Boolean renderBreathRange = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "misc", key = "stableNightVision", comment = "When enabled it stops the blinking effect of night vision when low duration, disable if it causes rendering issues with other mods.")
	public static Boolean stableNightVision = true;
}