package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.settings.ConfigScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.fields.TextField;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.settings.DSDropDownOption;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.lists.OptionsList;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncBooleanConfig;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncEnumConfig;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncListConfig;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncNumberConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.lang.reflect.Field;
import java.util.List;

public class ResetSettingsButton extends Button{
	public static final ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/reset_icon.png");

	private final Option option;

	public ResetSettingsButton(int x, int y, Option option){
		super(x, y, 20, 20, null, btn -> {
			if(btn.active)
				if(OptionsList.configMap.containsKey(option)){
					String key = OptionsList.configMap.get(option);

					ConfigOption opt = ConfigHandler.configObjects.get(key);
					Field fe = ConfigHandler.configFields.get(key);
					ConfigValue<?> value = ConfigHandler.configValues.get(key);

					if(Minecraft.getInstance().screen instanceof ConfigScreen){
						ConfigScreen screen = (ConfigScreen)Minecraft.getInstance().screen;

						ConfigHandler.updateConfigValue(value, ConfigHandler.defaultConfigValues.get(key));

						String configKey = OptionsList.configMap.get(option);
						AbstractWidget widget = screen.list.findOption(option);

						Object ob = ConfigHandler.defaultConfigValues.get(key);
						if(ob instanceof Boolean){
							if(widget != null){
								((CycleButton)widget).setValue(ob);
							}
							if(opt.side() == Dist.DEDICATED_SERVER){
								NetworkHandler.CHANNEL.sendToServer(new SyncBooleanConfig(configKey, (Boolean)ob));
							}
						}else if(ob instanceof Integer){
							if(widget != null){
								if(widget instanceof AbstractOptionSliderButton){
									widget.setMessage(((ProgressOption)option).getMessage(Minecraft.getInstance().options));
									((AbstractOptionSliderButton)widget).value = ((ProgressOption)option).toPct((Integer)ob);
								}else if(widget instanceof TextField){
									((TextField)widget).setValue(ob.toString());
								}
							}
							if(opt.side() == Dist.DEDICATED_SERVER){
								NetworkHandler.CHANNEL.sendToServer(new SyncNumberConfig(configKey, (Integer)ob));
							}
						}else if(ob instanceof Double){
							if(widget != null){
								if(widget instanceof AbstractOptionSliderButton){
									widget.setMessage(((ProgressOption)option).getMessage(Minecraft.getInstance().options));
									((AbstractOptionSliderButton)widget).value = ((ProgressOption)option).toPct((Double)ob);
								}else if(widget instanceof TextField){
									((TextField)widget).setValue(ob.toString());
								}
							}
							if(opt.side() == Dist.DEDICATED_SERVER){
								NetworkHandler.CHANNEL.sendToServer(new SyncNumberConfig(configKey, (Double)ob));
							}
						}else if(ob instanceof Long){
							if(widget != null){
								if(widget instanceof AbstractOptionSliderButton){
									widget.setMessage(((ProgressOption)option).getMessage(Minecraft.getInstance().options));
									((AbstractOptionSliderButton)widget).value = ((ProgressOption)option).toPct((Long)ob);
								}else if(widget instanceof TextField){
									((TextField)widget).setValue(ob.toString());
								}
							}
							if(opt.side() == Dist.DEDICATED_SERVER){
								NetworkHandler.CHANNEL.sendToServer(new SyncNumberConfig(configKey, (Long)ob));
							}
						}else if(ob instanceof Enum){
							((DSDropDownOption)option).btn.current = ((Enum<?>)ob).name();
							((DSDropDownOption)option).btn.updateMessage();

							if(opt.side() == Dist.DEDICATED_SERVER){
								NetworkHandler.CHANNEL.sendToServer(new SyncEnumConfig(configKey, (Enum)ob));
							}
						}else if(ob instanceof List){
							if(opt.side() == Dist.DEDICATED_SERVER){
								NetworkHandler.CHANNEL.sendToServer(new SyncListConfig(configKey, (List<String>)ob));
							}
						}
					}
				}
		});

		this.option = option;
	}

	@Override
	public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
		if(this.visible){
			this.active = false;
			this.isHovered = p_230430_2_ >= this.x && p_230430_3_ >= this.y && p_230430_2_ < this.x + this.width && p_230430_3_ < this.y + this.height;

			if(OptionsList.configMap.containsKey(option)){
				String key = OptionsList.configMap.get(option);

				ConfigOption opt = ConfigHandler.configObjects.get(key);
				Field fe = ConfigHandler.configFields.get(key);
				ConfigValue<?> value = ConfigHandler.configValues.get(key);
				this.active = !value.get().equals(ConfigHandler.defaultConfigValues.get(key));
			}

			Minecraft minecraft = Minecraft.getInstance();
			RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
			int i = this.getYImage(this.isHoveredOrFocused());
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			this.blit(p_230430_1_, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
			this.blit(p_230430_1_, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
			this.renderBg(p_230430_1_, minecraft, p_230430_2_, p_230430_3_);

			RenderSystem.setShaderTexture(0, texture);
			blit(p_230430_1_, x + 2, y + 2, 0, 0, 16, 16, 16, 16);
		}
	}
}