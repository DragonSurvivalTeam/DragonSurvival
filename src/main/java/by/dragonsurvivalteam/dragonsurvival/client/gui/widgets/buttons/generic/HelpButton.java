package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipRender;
import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.Objects;

public class HelpButton extends ExtendedButton implements TooltipRender{
	public static final ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/help_button.png");
	public String text;
	public int variation;
	public AbstractDragonType type;

	public HelpButton(int x, int y, int sizeX, int sizeY, String text, int variation){
		this(DragonUtils.getDragonType(Minecraft.getInstance().player), x, y, sizeX, sizeY, text, variation);
	}

	public HelpButton(AbstractDragonType type, int x, int y, int sizeX, int sizeY, String text, int variation){
		super(x, y, sizeX, sizeY, Component.empty(), s -> {});
		this.text = text;
		this.variation = variation;
		this.type = type;
	}

	@Override
	public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		RenderSystem.setShaderTexture(0, texture);

		float size = variation == 0 ? 18f : 22f;
		float xSize = (float)(width + (variation == 0 ? 0 : 2)) / size;
		float ySize = (float)(height + (variation == 0 ? 0 : 2)) / size;

		int offset = 0;

		if (isHoveredOrFocused()) {
			int id;

			if (type == null) {
				id = 4;
			} else {
				id = switch (type.getTypeName()) {
					case "cave" -> 1;
					case "forest" -> 2;
					case "sea" -> 3;
					default -> 0;
				};
			}

			offset += (int) (id * size);
		}

		pMatrixStack.pushPose();
		pMatrixStack.translate(x - x * xSize, y - y * ySize, 0);
		pMatrixStack.scale(xSize, ySize, 0);

		if(variation == 0)
			blit(pMatrixStack, x, y, 0, (float) offset, 18, 18, 256, 256);
		else
			blit(pMatrixStack, x - 1, y - 1, 18, (float) offset, 22, 22, 256, 256);

		pMatrixStack.popPose();
	}

	@Override
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY){
		TooltipRendering.drawHoveringText(pPoseStack, Component.translatable(text), pMouseX, pMouseY);
	}

	@Override
	public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_){
		return false;
	}
}