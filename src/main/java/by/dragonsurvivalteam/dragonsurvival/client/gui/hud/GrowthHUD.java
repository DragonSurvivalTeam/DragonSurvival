package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/** HUD that is shown when the dragon is holding an item that can change its growth */
public class GrowthHUD {
    private static final Color CENTER_COLOR = new Color(125, 125, 125);
    private static final Color OUTLINE_COLOR = new Color(125, 125, 125);
    private static final Color ADD_COLOR = new Color(0, 200, 0);
    private static final Color SUBTRACT_COLOR = new Color(200, 0, 0);

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "growth_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the item growth icon")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "growth"}, key = "growth_x_offset")
    public static Integer growthXOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "growth_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the y position of the item growth icon")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "growth"}, key = "growth_y_offset")
    public static Integer growthYOffset = 0;

    public static void renderGrowthHUD(final DragonStateHandler handler, @NotNull final GuiGraphics guiGraphics, int width, int height) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null || localPlayer.isSpectator()) {
            return;
        }

        ItemStack stack = localPlayer.getMainHandItem();

        Holder<DragonStage> dragonStage = handler.getStage();
        double nextSize = dragonStage.value().sizeRange().max();

        float currentProgress = (float) dragonStage.value().getProgress(handler.getSize());
        float desiredProgress = (float) dragonStage.value().getProgress(handler.getDesiredSize());
        float progressDiff = Math.abs(currentProgress - desiredProgress);
        boolean progressDiffIsSmall = progressDiff < 0.01;
        if (progressDiffIsSmall && (handler.getSize() == nextSize || dragonStage.value().growthItems().stream().noneMatch(item -> item.items().contains(stack.getItemHolder())))) {
            return;
        }

        currentProgress = Math.min(1, currentProgress);

        int radius = 17;
        int circleX = width / 2 - radius;
        int circleY = height - 90;

        circleX += growthXOffset;
        circleY += growthYOffset;

        float targetProgress;
        if(progressDiffIsSmall) {
            targetProgress = (float) dragonStage.value().getProgress(handler.getSize() + DragonGrowthHandler.getGrowth(dragonStage, stack.getItem()));
        } else {
            targetProgress = desiredProgress;
        }
        RenderingUtils.drawGrowthCircle(guiGraphics, circleX, circleY, radius, 6, 0.13f, currentProgress, targetProgress, CENTER_COLOR, OUTLINE_COLOR, ADD_COLOR, SUBTRACT_COLOR);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        guiGraphics.blit(handler.getType().value().getHoverGrowthIcon(dragonStage), circleX + 7, circleY + 4, 0, 0, 20, 20, 20, 20);
        guiGraphics.pose().popPose();
    }
}