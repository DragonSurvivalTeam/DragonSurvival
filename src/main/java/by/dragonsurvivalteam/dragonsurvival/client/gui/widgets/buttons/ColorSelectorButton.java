package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ColorSelectorComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.HueSelectorComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject.Texture;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorScreen;
import java.awt.*;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class ColorSelectorButton extends ExtendedButton {
	private final DragonEditorScreen screen;
	public final EnumSkinLayer layer;
	public Consumer<Double> setter;
	public boolean toggled;
	public int xSize, ySize;
	private HueSelectorComponent hueComponent;
	private ColorSelectorComponent colorComponent;
	private Renderable renderButton;

	public ColorSelectorButton(DragonEditorScreen screen, EnumSkinLayer layer, int x, int y, int xSize, int ySize, Consumer<Double> setter){
		super(x, y, xSize, ySize, Component.empty(), pButton -> {});
		this.xSize = xSize;
		this.ySize = ySize;
		this.setter = setter;
		this.screen = screen;
		this.layer = layer;
		visible = true;
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230430_2_, int p_230430_3_, float p_230430_4_){
		super.renderWidget(guiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
		active = !screen.preset.skinAges.get(screen.level).get().defaultSkin;

		if (visible) {
			RenderingUtils.drawGradientRect(guiGraphics.pose().last().pose(), 100, getX() + 2, getY() + 2, getX() + xSize - 2, getY() + ySize - 2, new int[]{Color.red.getRGB(), Color.GREEN.getRGB(), Color.BLUE.getRGB(), Color.yellow.getRGB()});
		}

		if(toggled && (!visible || !isMouseOver(p_230430_2_, p_230430_3_) && (hueComponent == null || !hueComponent.isMouseOver(p_230430_2_, p_230430_3_)) && (colorComponent == null || !colorComponent.isMouseOver(p_230430_2_, p_230430_3_)))){
			toggled = false;
			Screen screen = Minecraft.getInstance().screen;
			screen.children().removeIf(s -> s == colorComponent);
			screen.children().removeIf(s -> s == hueComponent);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, screen.handler), layer, screen.preset.skinAges.get(screen.level).get().layerSettings.get(layer).get().selectedSkin, screen.handler.getType());

		visible = text != null && text.colorable;
	}

	@Override
	public @NotNull Component getMessage(){
		return Component.empty();
	}

	@Override
	public void onPress(){
		Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, screen.handler), layer, screen.preset.skinAges.get(screen.level).get().layerSettings.get(layer).get().selectedSkin, screen.handler.getType());
		if(!toggled){
			renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), pButton -> {}){
				@Override
				public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230430_2_, int p_230430_3_, float p_230430_4_){
					active = visible = false;

					if (hueComponent != null && text.defaultColor == null) {
						hueComponent.visible = ColorSelectorButton.this.visible && text.defaultColor == null;
						if (hueComponent.visible)
							hueComponent.render(guiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
					}

					if (colorComponent != null && text.defaultColor != null) {
						colorComponent.visible = ColorSelectorButton.this.visible && text.defaultColor != null;
						if (colorComponent.visible)
							colorComponent.render(guiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
					}
				}
			};

			Screen screen = Minecraft.getInstance().screen;

			if(text.defaultColor == null){
				int offset = screen.height - (getY() + 80);
				hueComponent = new HueSelectorComponent(this.screen, getX() + xSize - 120, getY() + Math.min(offset, 0), 120, 90, layer);
				((AccessorScreen)screen).children().add(0, hueComponent);
				((AccessorScreen)screen).children().add(hueComponent);
			}else{
				int offset = screen.height - (getY() + 80);
				colorComponent = new ColorSelectorComponent(this.screen, getX() + xSize - 120, getY() + Math.min(offset, 0), 120, 90, layer);
				((AccessorScreen)screen).children().add(0, colorComponent);
				((AccessorScreen)screen).children().add(colorComponent);
			}
			screen.renderables.add(renderButton);
		}else{
			screen.children().removeIf(s -> s == colorComponent);
			screen.children().removeIf(s -> s == hueComponent);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		toggled = !toggled;
	}
}