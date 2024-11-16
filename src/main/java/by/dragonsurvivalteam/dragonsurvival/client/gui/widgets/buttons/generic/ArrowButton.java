package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;

public class ArrowButton extends Button {
	public static final ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_gui.png");
	public boolean next;

	public ArrowButton(int x, int y, int xSize, int ySize, boolean next, OnPress pressable){
		super(x, y, xSize, ySize, null, pressable);
		this.next = next;
	}

	@Override
	public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
		RenderSystem.setShaderTexture(0, texture);

		stack.pushPose();
		float xSize = (float)width / 34F;
		float ySize = (float)height / 34F;

		stack.translate(x - x * xSize, y - y * ySize, 0);
		stack.scale(xSize, ySize, 0);

		if(next)
			if(isHovered){
				blit(stack, x, y, 34, 34, 34, 34);
			}else{
				blit(stack, x, y, 0, 34, 34, 34);
			}
		else if(isHovered){
			blit(stack, x, y, 34, 0, 34, 34);
		}else{
			blit(stack, x, y, 0, 0, 34, 34);
		}
		stack.popPose();
	}
}