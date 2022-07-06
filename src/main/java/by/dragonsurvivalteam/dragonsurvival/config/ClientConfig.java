package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig{
	ClientConfig(ForgeConfigSpec.Builder builder){
		ConfigHandler.addConfigs(builder, Dist.CLIENT);
	}

	@ConfigOption( side = Dist.CLIENT, category = "misc", key = "clientDebugMessages", comment = "Enable client-side debug messages" )
	public static Boolean clientDebugMessages = false;

	@ConfigOption( side = Dist.CLIENT, category = "rendering", key = "enableTailPhysics", comment = "Enable movement based physics on the tail, this is still a work in progress and can be buggy." )
	public static Boolean enableTailPhysics = true;

	@ConfigOption(side = Dist.CLIENT, category = "misc", key = "stableNightVision", comment = "When enabled it stops the blinking effect of night vision when low duration, disable if it causes rendering issues with other mods.")
	public static Boolean stableNightVision = true;
}