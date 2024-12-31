package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ColorPickerButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;


public class BackgroundColorSelectorComponent extends AbstractContainerEventHandler implements Renderable {
    public static final int BACKGROUND_COLOR = -14935012;
    public static final int INNER_BORDER_COLOR = new Color(0x78787880, true).getRGB();

    public static final ResourceLocation COLOR_RESET_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/color_reset_hover.png");
    public static final ResourceLocation COLOR_RESET_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/color_reset_main.png");

    public final ColorPickerButton colorPicker;
    public final HoverButton resetButton;
    private final int x;
    private final int y;
    private final int xSize;
    private final int ySize;
    public boolean visible;

    public BackgroundColorSelectorComponent(DragonEditorScreen screen, int x, int y, int xSize, int ySize) {
        this.x = x;
        this.y = y;
        this.xSize = xSize;
        this.ySize = ySize;

        Color defaultColor = new Color(screen.backgroundColor);
        float alpha = (float) (screen.backgroundColor >> 24 & 255) / 255.0F;

        colorPicker = new ColorPickerButton(x + 5, y + 18, xSize - 8, ySize - 26, defaultColor, color -> {
            Color c1 = new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
            screen.backgroundColor = c1.getRGB();
        });

        resetButton = new HoverButton(x + 5, y - 8, 24, 24, 24, 24, COLOR_RESET_MAIN, COLOR_RESET_HOVER, button -> {
            colorPicker.resetColor();
        });
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return visible && pMouseY >= (double) y - 18 && pMouseY <= (double) y + ySize + 3 && pMouseX >= (double) x && pMouseX <= (double) x + xSize;
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return ImmutableList.of(colorPicker, resetButton);
    }

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        if (visible) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 100);
           // guiGraphics.fill(x, y, x + xSize, y + ySize, Color.black.getRGB());
            // Background for reset button
            guiGraphics.fill(x + 2, y - 10, x + 32, y + 35,  BACKGROUND_COLOR);
            guiGraphics.renderOutline(x + 2, y - 11, 30, 41, Color.black.getRGB());
            guiGraphics.renderOutline(x + 3, y - 10, 28, 39, INNER_BORDER_COLOR);
            guiGraphics.pose().translate(0, 0, 100);

            // Background for color picker
            guiGraphics.fill(x, y + 15, x + xSize, y + ySize - 5,  BACKGROUND_COLOR);
            guiGraphics.renderOutline(x, y + 14, xSize, ySize - 18, Color.black.getRGB());
            guiGraphics.renderOutline(x + 1, y + 15, xSize - 2, ySize - 20, INNER_BORDER_COLOR);

            colorPicker.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
            resetButton.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
            guiGraphics.pose().popPose();
        }
    }
}