package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;

public class DragonEditorSlotButton extends Button{
	private final DragonEditorScreen screen;
	public int num;

	public DragonEditorSlotButton(int p_i232255_1_, int p_i232255_2_, int num, DragonEditorScreen parent){
		super(p_i232255_1_, p_i232255_2_, 12, 12, Component.empty(), btn -> {}, DEFAULT_NARRATION);
		this.num = num;
		screen = parent;
	}

	@Override
	public void onPress(){
		if(screen.dragonType != null){
			DragonEditorRegistry.getSavedCustomizations().skinPresets.computeIfAbsent(screen.dragonType.getTypeNameUpperCase(), t -> new HashMap<>());
			DragonEditorRegistry.getSavedCustomizations().skinPresets.get(screen.dragonType.getTypeNameUpperCase()).put(screen.currentSelected, screen.preset);
		}

		screen.currentSelected = num - 1;
		screen.update();
		screen.handler.getSkinData().compileSkin();
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230431_2_, int p_230431_3_, float p_230431_4_){
		if(screen.currentSelected == num - 1){
			guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, new Color(1, 1, 1, isHovered ? 0.95F : 0.75F).getRGB());
			guiGraphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, new Color(0.05F, 0.05F, 0.05F, isHovered ? 0.95F : 0.75F).getRGB());
		}

		TextRenderUtil.drawScaledText(guiGraphics, getX() + 2.5f, getY() + 1f, 1.5F, Integer.toString(num), DyeColor.WHITE.getTextColor());
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks){
		active = visible = screen.showUi;
		super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
	}
}