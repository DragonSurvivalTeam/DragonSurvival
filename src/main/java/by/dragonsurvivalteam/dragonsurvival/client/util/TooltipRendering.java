package by.dragonsurvivalteam.dragonsurvival.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.List;
import java.util.Optional;

public class TooltipRendering{
	public static void drawHoveringText(PoseStack poseStack, Component textLines, int x, int y){
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		drawHoveringText(poseStack, List.of(textLines), x, y, ItemStack.EMPTY, font);
	}

	private static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, ItemStack itemStack, Font font){
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if(screen == null){
			return;
		}

		Optional<TooltipComponent> tooltipImage = itemStack.getTooltipImage();
		List<ClientTooltipComponent> list = ForgeHooksClient.gatherTooltipComponents(itemStack, textLines, tooltipImage, x, Math.min(x + 200, screen.width), screen.height, null, font);
		screen.renderTooltipInternal(poseStack, list, x, y);
	}

	public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y){
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		drawHoveringText(poseStack, textLines, x, y, ItemStack.EMPTY, font);
	}
}