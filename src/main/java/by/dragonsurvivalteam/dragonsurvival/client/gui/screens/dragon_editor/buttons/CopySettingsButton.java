package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.CopyEditorSettingsComponent;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

public class CopySettingsButton extends ExtendedButton {
	private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/copy_icon.png");

	private final DragonEditorScreen screen;
	public boolean toggled;
	private CopyEditorSettingsComponent component;
	private Renderable renderButton;

	public CopySettingsButton(DragonEditorScreen screen, int xPos, int yPos, int width, int height, Component displayString, OnPress handler){
		super(xPos, yPos, width, height, displayString, handler);
		this.screen = screen;
		setTooltip(Tooltip.create(displayString));
	}


	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks){
		if(visible){
			guiGraphics.blit(ICON, getX(), getY(), 0, 0, 16, 16, 16, 16);
		}

		if(toggled && (!visible || !isMouseOver(pMouseX, pMouseY) && (component == null || !component.isMouseOver(pMouseX, pMouseY)))){
			toggled = false;
			Screen screen = Minecraft.getInstance().screen;
			screen.children().removeIf(s -> s == component);
			screen.renderables.removeIf(s -> s == renderButton);
		}
	}

	@Override
	public void onPress(){
		if(!toggled){
			renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), pButton -> {}){
				@Override
				public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230430_2_, int p_230430_3_, float p_230430_4_){
					active = visible = false;

					if(component != null){
						component.visible = CopySettingsButton.this.visible;

						if(component.visible)
							component.render(guiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
					}
				}
			};

			int offset = screen.height - (getY() + 80);
			component = new CopyEditorSettingsComponent(screen, this, getX() + width - 80, getY() + Math.min(offset, 0), 80, 70);
			((AccessorScreen)screen).children().add(0, component);
			((AccessorScreen)screen).children().add(component);
			screen.renderables.add(0, renderButton);
			screen.renderables.add(renderButton);
		}else{
			screen.children().removeIf(s -> s == component);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		toggled = !toggled;
	}
}