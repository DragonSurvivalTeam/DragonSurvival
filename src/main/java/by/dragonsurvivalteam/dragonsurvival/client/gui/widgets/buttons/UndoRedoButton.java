package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipProvider;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class UndoRedoButton extends ArrowButton implements TooltipProvider {
	public static final ResourceLocation undo = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/arrow_undo.png");
	public static final ResourceLocation redo = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/arrow_redo.png");

	public UndoRedoButton(int x, int y, int xSize, int ySize, boolean next, OnPress pressable){
		super(x, y, xSize, ySize, next, pressable);
	}

	@Override
	public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
		if(next){
			RenderSystem.setShaderTexture(0, redo);
			blit(stack, x, y, 0, 0, width, height, width, height);
		}else{
			RenderSystem.setShaderTexture(0, undo);
			blit(stack, x, y, 0, 0, width, height, width, height);
		}
	}

	@Override
	public List<Component> getTooltip() {
		return List.of();
	}
}