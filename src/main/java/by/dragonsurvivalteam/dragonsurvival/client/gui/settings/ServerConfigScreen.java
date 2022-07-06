package by.dragonsurvivalteam.dragonsurvival.client.gui.settings;


import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;

public class ServerConfigScreen extends ConfigScreen{
	public ServerConfigScreen(Screen p_i225930_1_, Options p_i225930_2_, Component p_i225930_3_){
		super(p_i225930_1_, p_i225930_2_, p_i225930_3_);
	}

	@Override
	public Dist screenSide(){
		return Dist.DEDICATED_SERVER;
	}

	@Override
	public void tick(){
		super.tick();

		if(!Minecraft.getInstance().player.hasPermissions(2)){
			this.minecraft.setScreen(this.lastScreen);
		}
	}

}