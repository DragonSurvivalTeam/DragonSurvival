package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.*;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GrowthHUD {
	private static final HashMap<String, ResourceLocation> CACHE = new HashMap<>();
	private static final Color COLOR = new Color(99, 99, 99);
	private static final Color BRIGHTER_COLOR = COLOR.brighter();

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = ConfigSide.CLIENT, category = {"ui", "growth"}, key = "growthXOffset", comment = "Offset the x position of the item growth icon in relation to its normal position" )
	public static Integer growthXOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = ConfigSide.CLIENT, category = {"ui", "growth"}, key = "growthYOffset", comment = "Offset the y position of the item growth icon in relation to its normal position" )
	public static Integer growthYOffset = 0;

	public static void renderGrowthHUD(final DragonStateHandler handler, @NotNull final GuiGraphics guiGraphics, int width, int height){
		Player localPlayer = Minecraft.getInstance().player;

		if (localPlayer == null || localPlayer.isSpectator()) {
			return;
		}

		ItemStack stack = localPlayer.getMainHandItem();

		if (stack.isEmpty()) {
			return;
		}

		int increment = DragonGrowthHandler.getIncrement(stack.getItem(), handler.getLevel());

		if (increment != 0 && (handler.getSize() < ServerConfig.maxGrowthSize && increment > 0 || increment < 0 && handler.getSize() >= DragonLevel.NEWBORN.size + 1)) {
			float curSize = (float) handler.getSize();
			float nextSize = (float) (handler.getSize() + increment);
			float progress = 0;
			float nextProgess = 0;

			if (handler.getLevel() == DragonLevel.NEWBORN) {
				progress = (curSize - DragonLevel.NEWBORN.size) / (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size);
				nextProgess = (nextSize - DragonLevel.NEWBORN.size) / (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size);
			} else if (handler.getLevel() == DragonLevel.YOUNG) {
				progress = (curSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
				nextProgess = (nextSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
			} else if (handler.getLevel() == DragonLevel.ADULT && handler.getSize() < 40) {
				progress = (curSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
				nextProgess = (nextSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
			} else if (handler.getLevel() == DragonLevel.ADULT) {
				progress = (float) ((curSize - 40) / (ServerConfig.maxGrowthSize - 40));
				nextProgess = (float) ((nextSize - 40) / (ServerConfig.maxGrowthSize - 40));
			}

			progress = Math.min(1.0f, progress);
			nextProgess = Math.min(1.0f, nextProgess);

			int radius = 17;
			int thickness = 5;
			int circleX = width / 2 - radius;
			int circleY = height - 90;

			circleX += growthXOffset;
			circleY += growthYOffset;

			RenderSystem.setShaderColor(0f, 0f, 0f, 1f);

			RenderSystem.setShaderColor(BRIGHTER_COLOR.getRed() / 255.0f, BRIGHTER_COLOR.getBlue() / 255.0f, BRIGHTER_COLOR.getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(guiGraphics, circleX + radius, circleY + radius, radius, 6, 1, 0);

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);

			if (nextProgess > progress) {
				int num = 1;
				double perSide = 1.0 / 6.0;

				if (nextProgess < progress + perSide) {
					nextProgess = (float) (progress + perSide);
					num = 2;
				}

				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_" + num + ".png"));
				RenderingUtils.drawTexturedCircle(guiGraphics, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);

				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_" + handler.getTypeNameLowerCase() + ".png"));
				RenderingUtils.drawTexturedCircle(guiGraphics, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);
			} else if (increment < 0) {
				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_3.png"));
				RenderingUtils.drawTexturedCircle(guiGraphics, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);

				RenderSystem.setShaderTexture(0, getOrCreate("textures/gui/growth/circle_" + handler.getTypeNameLowerCase() + ".png"));
				RenderingUtils.drawTexturedCircle(guiGraphics, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);
			}

			RenderSystem.setShaderColor(COLOR.getRed() / 255.0f, COLOR.getBlue() / 255.0f, COLOR.getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(guiGraphics, circleX + radius, circleY + radius, radius - thickness, 6, 1, 0);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 300);
			guiGraphics.blit(getOrCreate("textures/gui/growth/growth_" + handler.getTypeNameLowerCase() + "_" + (handler.getLevel().ordinal() + 1) + ".png"), circleX + 6, circleY + 6, 0, 0, 20, 20, 20, 20);
			guiGraphics.pose().popPose();
		}
	}

	private static ResourceLocation getOrCreate(final String path) {
		return CACHE.computeIfAbsent(path, key -> ResourceLocation.fromNamespaceAndPath(MODID, path));
	}
}