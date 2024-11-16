package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ColorSelectorComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.HueSelectorComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject.Texture;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorSelectorButton extends ExtendedButton{
	private final DragonEditorScreen screen;
	private final EnumSkinLayer layer;
	public Consumer<Double> setter;
	public boolean toggled;
	public int xSize, ySize;
	private HueSelectorComponent hueComponent;
	private ColorSelectorComponent colorComponent;
	private Widget renderButton;

	public ColorSelectorButton(DragonEditorScreen screen, EnumSkinLayer layer, int x, int y, int xSize, int ySize, Consumer<Double> setter){
		super(x, y, xSize, ySize, null, null);
		this.xSize = xSize;
		this.ySize = ySize;
		this.setter = setter;
		this.screen = screen;
		this.layer = layer;
		visible = false;
	}

	@Override
	public void render(PoseStack stack, int p_230430_2_, int p_230430_3_, float p_230430_4_){
		if(!screen.showUi){
			active = false;
			return;
		}
		super.render(stack, p_230430_2_, p_230430_3_, p_230430_4_);
		active = !screen.preset.skinAges.get(screen.level).get().defaultSkin;

		if(visible)
			RenderingUtils.drawGradientRect(stack.last().pose(), 100, x + 2, y + 2, x + xSize - 2, y + ySize - 2, new int[]{Color.red.getRGB(),
			                                                                                                                Color.GREEN.getRGB(),
			                                                                                                                Color.BLUE.getRGB(),
			                                                                                                                Color.yellow.getRGB()});

		if(toggled && (!visible || !isMouseOver(p_230430_2_, p_230430_3_) && (hueComponent == null || !hueComponent.isMouseOver(p_230430_2_, p_230430_3_)) && (colorComponent == null || !colorComponent.isMouseOver(p_230430_2_, p_230430_3_)))){
			toggled = false;
			Screen screen = Minecraft.getInstance().screen;
			screen.children.removeIf(s -> s == colorComponent);
			screen.children.removeIf(s -> s == hueComponent);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, screen.handler), layer, screen.preset.skinAges.get(screen.level).get().layerSettings.get(layer).get().selectedSkin, screen.handler.getType());

		visible = text != null && text.colorable;
	}

	@Override
	public Component getMessage(){
		return Component.empty();
	}

	@Override
	public void onPress(){
		Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, screen.handler), layer, screen.preset.skinAges.get(screen.level).get().layerSettings.get(layer).get().selectedSkin, screen.handler.getType());

		if(!toggled){
			renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), null){
				@Override
				public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
					active = visible = false;

					if(hueComponent != null && text.defaultColor == null){
						hueComponent.visible = ColorSelectorButton.this.visible && text.defaultColor == null;
						if(hueComponent.visible)
							hueComponent.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
					}

					if(colorComponent != null && text.defaultColor != null){
						colorComponent.visible = ColorSelectorButton.this.visible && text.defaultColor != null;
						if(colorComponent.visible)
							colorComponent.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
					}
				}
			};

			Screen screen = Minecraft.getInstance().screen;

			if(text.defaultColor == null){
				int offset = screen.height - (y + 80);
				hueComponent = new HueSelectorComponent(this.screen, x + xSize - 120, y + Math.min(offset, 0), 120, 76, layer);
				screen.children.add(0, hueComponent);
				screen.children.add(hueComponent);
			}else{
				int offset = screen.height - (y + 80);
				colorComponent = new ColorSelectorComponent(this.screen, x + xSize - 120, y + Math.min(offset, 0), 120, 71, layer);
				screen.children.add(0, colorComponent);
				screen.children.add(colorComponent);
			}
			screen.renderables.add(renderButton);
		}else{
			screen.children.removeIf(s -> s == colorComponent);
			screen.children.removeIf(s -> s == hueComponent);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		toggled = !toggled;
	}
}