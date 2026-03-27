package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorPickerButton extends ExtendedButton {
    public final Color defaultColor;
    public final Consumer<Color> colorConsumer;
    double selectorX;
    double selectorY;

    public ColorPickerButton(int xPos, int yPos, int width, int height, Color defaultColor, Consumer<Color> colorConsumer) {
        super(xPos, yPos, width, height, Component.empty(), button -> { /* Nothing to do */ });
        this.defaultColor = defaultColor;
        this.colorConsumer = colorConsumer;

        float[] hsb = Color.RGBtoHSB(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), null);
        selectorX = (hsb[0] * width);
        selectorY = (hsb[2] < 1.0f ? hsb[2] * (height / 2f) : height / 2f + (1 - hsb[1]) * (height / 2f));
    }

    public void resetColor() {
        Color emptyColor = new Color(0, 0, 0, 0);
        colorConsumer.accept(emptyColor);
        float[] hsb = Color.RGBtoHSB(emptyColor.getRed(), emptyColor.getGreen(), emptyColor.getBlue(), null);
        selectorX = (hsb[0] * width);
        selectorY = (hsb[2] < 1.0f ? hsb[2] * (height / 2f) : height / 2f + (1 - hsb[1]) * (height / 2f));
    }

    @Override
    public void extractWidgetRenderState(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partial) {
        RenderingUtils.renderColorSquare(GuiGraphicsExtractor, getX(), getY(), width, height);
        RenderingUtils.fill(GuiGraphicsExtractor, getX() + selectorX - 2, getY() + selectorY - 2, getX() + selectorX + 2, getY() + selectorY + 2, Color.black.getRGB());
        RenderingUtils.fill(GuiGraphicsExtractor, getX() + selectorX - 1, getY() + selectorY - 1, getX() + selectorX + 1, getY() + selectorY + 1, getColor().getRGB());
    }

    public Color getColor() {
        double hue = selectorX / width * 360.0 / 360.0;

        if (selectorY > height / 2f) {
            double saturation = 1f - (selectorY - height / 2f) / (height / 2f) * 360.0 / 360.0;
            return Color.getHSBColor((float) hue, (float) saturation, 1f);
        } else {
            double brightness = selectorY / (height / 2f) * 360.0 / 360.0;
            return Color.getHSBColor((float) hue, 1f, (float) brightness);
        }
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        selectorX = Mth.clamp(event.x() - getX(), 0, width);
        selectorY = Mth.clamp(event.y() - getY(), 0, height);
        colorConsumer.accept(getColor());
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent event, double mouseX, double mouseY) {
        selectorX = Mth.clamp(event.x() - getX(), 0, width);
        selectorY = Mth.clamp(event.y() - getY(), 0, height);
        colorConsumer.accept(getColor());

        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public void playDownSound(@NotNull SoundManager handler) { /* Remove button sound */ }
}