package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/** HUD that is shown when the dragon is holding an item that can change its growth */
public class GrowthHUD {
    public static final ResourceLocation ID = DragonSurvival.res("growth_hud");

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

    /** Renders the growth icon above the experience bar when an item is selected which grants growth */
    public static void render(@NotNull final GuiGraphics graphics, @NotNull final DeltaTracker tracker) {
        Player player = Minecraft.getInstance().player;

        if (player == null || player.isSpectator()) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        Holder<DragonStage> dragonStage = handler.stage();
        double nextSize = dragonStage.value().sizeRange().max();

        float currentProgress = (float) dragonStage.value().getProgress(handler.getSize());
        float desiredProgress = (float) dragonStage.value().getProgress(handler.getDesiredSize());
        float progressDiff = Math.abs(currentProgress - desiredProgress);
        boolean progressDiffIsSmall = progressDiff < 0.01;

        if (progressDiffIsSmall && (handler.getSize() == nextSize || dragonStage.value().growthItems().stream().noneMatch(growthItem -> growthItem.canBeUsed(handler, stack.getItem())))) {
            return;
        }

        currentProgress = Math.min(1, currentProgress);

        int radius = 17;
        int circleX = graphics.guiWidth() / 2 - radius;
        int circleY = graphics.guiHeight() - 90;

        circleX += growthXOffset;
        circleY += growthYOffset;

        float targetProgress;

        if (progressDiffIsSmall) {
            targetProgress = (float) dragonStage.value().getProgress(handler.getSize() + DragonGrowthHandler.getGrowth(handler, stack.getItem()));
        } else {
            targetProgress = desiredProgress;
        }

        RenderingUtils.drawGrowthCircle(graphics, circleX, circleY, radius, 6, 0.13f, currentProgress, targetProgress, CENTER_COLOR, OUTLINE_COLOR, ADD_COLOR, SUBTRACT_COLOR);

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        graphics.blit(StageResources.getHoverGrowthIcon(handler.species(), handler.stageKey()), circleX + 7, circleY + 6, 0, 0, 20, 20, 20, 20);
        graphics.pose().popPose();
    }
}