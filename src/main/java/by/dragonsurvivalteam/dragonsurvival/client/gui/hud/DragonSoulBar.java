package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DragonSoulBar {
    @ConfigRange
    @Translation(key = "dragon_soul_bar_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset to the x position of the dragon soul bar indicator")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "dragon_soul"}, key = "dragon_soul_bar_x_offset")
    public static int xOffset;

    @ConfigRange
    @Translation(key = "dragon_soul_bar_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset to the y position of the dragon soul bar indicator")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "dragon_soul"}, key = "dragon_soul_bar_y_offset")
    public static int yOffset;

    public static final ResourceLocation ID = DragonSurvival.res("dragon_soul_bar");
    private static final ResourceLocation DRAGON_SOUL_BAR = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/dragon_soul_bar.png");

    public static void render(@NotNull final GuiGraphics graphics, @NotNull final DeltaTracker tracker) {
        Player player =  Minecraft.getInstance().player;

        if (player == null || player.isSpectator()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        if (player.isUsingItem() && stack.is(DSItems.DRAGON_SOUL)) {
            int duration = stack.getUseDuration(player);

            float progress = ((float) duration - (float) player.getUseItemRemainingTicks()) / (float) duration;

            Window window = Minecraft.getInstance().getWindow();
            int x = window.getGuiScaledWidth() / 2 - 66 / 2;
            int y = window.getGuiScaledHeight() - 96;

            x += xOffset;
            y += yOffset;

            int width = (int) (progress * 62);
            graphics.blit(DRAGON_SOUL_BAR, x, y, 0, 0, 66, 21, 256, 256);
            graphics.blit(DRAGON_SOUL_BAR, x + 4, y + 1, 4, 21, width, 21, 256, 256);
        }
    }
}
