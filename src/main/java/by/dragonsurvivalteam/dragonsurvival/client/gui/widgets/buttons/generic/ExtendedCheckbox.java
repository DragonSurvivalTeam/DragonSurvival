package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ScreenUtils;

import java.util.List;
import java.util.function.Consumer;

public class ExtendedCheckbox extends Checkbox implements TooltipProvider {
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/textbox.png");
	final ResourceLocation TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/checkbox.png");
	private final int renderWidth;
	public Consumer<ExtendedCheckbox> pressable;

	public ExtendedCheckbox(int pX, int pY, int pWidth, int renderWidth, int pHeight, Component pMessage, boolean pSelected, Consumer<ExtendedCheckbox> pressable){
		super(pX, pY, pWidth, pHeight, pMessage, pSelected);
		this.pressable = pressable;
		this.renderWidth = renderWidth;
	}

	@Override
	public void onPress(){
		super.onPress();
		pressable.accept(this);
	}

	@Override
	public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		pMatrixStack.pushPose();
		Minecraft minecraft = Minecraft.getInstance();
		Font fontrenderer = minecraft.font;

		if(height > 10){
			ScreenUtils.blitWithBorder(pMatrixStack, BACKGROUND_TEXTURE, x, y, 0, 0, width, height, 32, 32, 10, 10, 10, 10, (float)0);
			ScreenUtils.blitWithBorder(pMatrixStack, BACKGROUND_TEXTURE, x, y, 0, 0, renderWidth, height, 32, 32, 10, 10, 10, 10, (float)0);
		}


		RenderSystem.setShaderTexture(0, TEXTURE);

		if(height > 10){
			float widthMod = (renderWidth - 4) / 36f;
			float heightMod = (height - 4) / 36f;

			float u = isHoveredOrFocused() || isFocused() ? renderWidth - 4 : 0.0F;
			float v = selected() ? height - 4 : 0.0F;

			blit(pMatrixStack, x + 2, y + 2, u, v, renderWidth - 4, height - 4, (int)(72 * widthMod), (int)(72f * heightMod));
			renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
			// Insecure modification
			MutableComponent message = Component.empty().append(getMessage());
			if (active)
				message = message.withStyle(ChatFormatting.DARK_GRAY);
			pMatrixStack.pushPose();
			drawString(pMatrixStack, fontrenderer,  message, x + renderWidth + 2, y + (height - 8) / 2, 14737632);
			pMatrixStack.popPose();
		}else{
			float widthMod = renderWidth / 36f;
			float heightMod = height / 36f;

			float u = isHoveredOrFocused() || isFocused() ? renderWidth : 0.0F;
			float v = selected() ? height : 0.0F;

			blit(pMatrixStack, x, y, u, v, renderWidth, height, (int)(72 * widthMod), (int)(72f * heightMod));
			renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);

			//Component message = active ? getMessage() : ((TranslatableComponent)getMessage()).withStyle(ChatFormatting.DARK_GRAY);
			// Insecure modification
			MutableComponent message = Component.empty().append(getMessage());
			if (active)
				message = message.withStyle(ChatFormatting.DARK_GRAY);

			pMatrixStack.pushPose();
			drawString(pMatrixStack, fontrenderer, message, x + renderWidth + 2, y + (height - 8) / 2, 14737632);
			pMatrixStack.popPose();
		}
		pMatrixStack.popPose();
	}

	@Override
	public List<Component> getTooltip() {
		return List.of();
	}
}