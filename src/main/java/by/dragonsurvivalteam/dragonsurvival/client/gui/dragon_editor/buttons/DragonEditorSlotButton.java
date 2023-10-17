package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.DyeColor;

import java.awt.Color;
import java.util.HashMap;

public class DragonEditorSlotButton extends Button{
	private final DragonEditorScreen screen;
	public int num;

	public DragonEditorSlotButton(int p_i232255_1_, int p_i232255_2_, int num, DragonEditorScreen parent){
		super(p_i232255_1_, p_i232255_2_, 12, 12, null, btn -> {});
		this.num = num;
		screen = parent;
	}

	@Override
	public void onPress(){
		if(screen.dragonType != null){
			DragonEditorRegistry.getSavedCustomizations().skinPresets.computeIfAbsent(screen.dragonType.getTypeName().toUpperCase(), t -> new HashMap<>());
			DragonEditorRegistry.getSavedCustomizations().skinPresets.get(screen.dragonType.getTypeName().toUpperCase()).put(screen.currentSelected, screen.preset);
		}

		screen.currentSelected = num - 1;
		screen.update();
		screen.handler.getSkinData().compileSkin();
	}

	@Override
	public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
		if(screen.currentSelected == num - 1){
			Gui.fill(stack, x, y, x + width, y + height, new Color(1, 1, 1, isHovered ? 0.95F : 0.75F).getRGB());
			Gui.fill(stack, x + 1, y + 1, x + width - 1, y + height - 1, new Color(0.05F, 0.05F, 0.05F, isHovered ? 0.95F : 0.75F).getRGB());
		}
		TextRenderUtil.drawScaledText(stack, x + 2.5f, y + 1f, 1.5F, Integer.toString(num), DyeColor.WHITE.getTextColor());
	}

	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		active = visible = screen.showUi;
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}
}