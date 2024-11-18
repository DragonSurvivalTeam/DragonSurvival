package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonEditorConfirmComponent extends AbstractContainerEventHandler implements Widget{
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_altar_warning.png");
	private final DragonEditorScreen screen;
	private final AbstractWidget btn1;
	private final AbstractWidget btn2;
	private final int x;
	private final int y;
	private final int xSize;
	private final int ySize;
	public boolean visible;


	public DragonEditorConfirmComponent(DragonEditorScreen screen, int x, int y, int xSize, int ySize){
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.xSize = xSize;
		this.ySize = ySize;

		btn1 = new ExtendedButton(x + 19, y + 133, 41, 21, CommonComponents.GUI_YES, null){
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				drawCenteredString(mStack, Minecraft.getInstance().font, getMessage(), x + width / 2, y + (height - 8) / 2, getFGColor());

				if(isHovered){
					TooltipRendering.drawHoveringText(mStack, Component.translatable("ds.gui.dragon_editor.tooltip.done"), mouseX, mouseY);
				}
			}

			@Override
			public void onPress(){
				screen.confirm();
			}
		};

		btn2 = new ExtendedButton(x + 66, y + 133, 41, 21, CommonComponents.GUI_NO, null){
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				drawCenteredString(mStack, Minecraft.getInstance().font, getMessage(), x + width / 2, y + (height - 8) / 2, getFGColor());

				if(isHovered){
					TooltipRendering.drawHoveringText(mStack, Component.translatable("ds.gui.dragon_editor.tooltip.cancel"), mouseX, mouseY);
				}
			}


			@Override
			public void onPress(){
				screen.confirmation = false;
			}
		};
	}

	@Override
	public List<? extends GuiEventListener> children(){
		return ImmutableList.of(btn1, btn2);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton){
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public void render(@NotNull PoseStack pose, int pMouseX, int pMouseY, float pPartialTicks){
		pose.pushPose();
		pose.translate(0, 0, 100);

		fillGradient(pose, 0, 0, Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), -1072689136, -804253680);
		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

		String key = "ds.gui.dragon_editor.confirm." + (!ServerConfig.saveAllAbilities && !ServerConfig.saveGrowthStage ? "all" : ServerConfig.saveAllAbilities && !ServerConfig.saveGrowthStage ? "ability" : !ServerConfig.saveAllAbilities && ServerConfig.saveGrowthStage ? "growth" : "");
		String text = Component.translatable(key).getString();
		blit(pose, x, y, 0, 0, xSize, ySize);
		TextRenderUtil.drawCenteredScaledTextSplit(pose, x + xSize / 2, y + 42, 1f, text, DyeColor.WHITE.getTextColor(), xSize - 10, 0);

		btn1.render(pose, pMouseX, pMouseY, pPartialTicks);
		btn2.render(pose, pMouseX, pMouseY, pPartialTicks);

		pose.popPose();
	}
}