package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/** HUD that is shown when the dragon is holding an item that can change its growth */
public class GrowthHUD {
    public static final Function<String, String> CIRCLE_TEXTURE = dragonType -> "textures/gui/growth/circle_" + dragonType + ".png";
    private static final HashMap<String, ResourceLocation> CACHE = new HashMap<>();
    private static final Color CENTER_COLOR = new Color(125, 125, 125);
    private static final Color BORDER_COLOR = new Color(255, 204, 2);
    private static final Color OUTLINE_COLOR = new Color(125, 125, 125);
    private static float currentProgress = 0;
    private static ResourceKey<DragonStage> currentStage;

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

        if (stack.isEmpty()) {
            return;
        }

        Holder<DragonStage> dragonStage = handler.getStage();
        double nextSize = dragonStage.value().sizeRange().max();

        if (handler.getSize() == nextSize || dragonStage.value().growthItems().stream().noneMatch(item -> item.items().contains(stack.getItemHolder()))) {
            return;
        }

        float progress = (float) dragonStage.value().getProgress(handler.getSize());

        progress = Math.min(1, progress);

        int radius = 17;
        int circleX = width / 2 - radius;
        int circleY = height - 90;

        circleX += growthXOffset;
        circleY += growthYOffset;

        float deltaTick = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
        float lerpRate = Math.min(1, deltaTick * 0.2f);
        currentProgress = Mth.lerp(lerpRate, currentProgress, progress);
        ResourceKey<DragonStage> lastStage = currentStage;
        if(lastStage != dragonStage.getKey()) {
            if(progress > currentProgress) {
                currentProgress = 1;
            } else {
                currentProgress = 0;
            }
        }
        currentStage = dragonStage.getKey();
        RenderingUtils.drawGrowthCircle(guiGraphics, circleX, circleY, radius, 6, 0.13f, currentProgress, BORDER_COLOR, CENTER_COLOR, OUTLINE_COLOR);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        guiGraphics.blit(handler.getType().value().getGrowthIcon(dragonStage), circleX + 7, circleY + 4, 0, 0, 20, 20, 20, 20);
        guiGraphics.pose().popPose();
    }

    public static ResourceLocation getOrCreate(final String path) {
        return getOrCreate(DragonSurvival.MODID, path);
    }

    public static ResourceLocation getOrCreate(final String namespace, final String path) {
        return CACHE.computeIfAbsent(path, key -> ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}