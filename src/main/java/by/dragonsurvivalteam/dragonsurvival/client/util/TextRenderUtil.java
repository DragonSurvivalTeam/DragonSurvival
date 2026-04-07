package by.dragonsurvivalteam.dragonsurvival.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TextRenderUtil {
    public static void drawScaledText(@NotNull final GuiGraphicsExtractor graphics, float x, float y, float scale, String text, int color) {
        drawScaledText(graphics, x, y, scale, text, color, 0);
    }

    public static void drawScaledText(@NotNull final GuiGraphicsExtractor graphics, float x, float y, float scale, String text, int color, int zLevel) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x - x * scale, y - y * scale);
        graphics.pose().scale(scale, scale);
        graphics.text(Minecraft.getInstance().font, text, (int)x, (int)y, color, false);
        graphics.pose().popMatrix();
    }

    public static void drawCenteredScaledText(@NotNull final GuiGraphicsExtractor graphics, int x, int y, float scale, String text, int color) {
        drawCenteredScaledText(graphics, x, y, scale, text, color, 0);
    }

    public static void drawCenteredScaledText(@NotNull final GuiGraphicsExtractor graphics, int x, int y, float scale, String text, int color, int zLevel) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x - x * scale, y - y * scale);
        graphics.pose().scale(scale, scale);
        graphics.centeredText(Minecraft.getInstance().font, text, x, y, color);
        graphics.pose().popMatrix();
    }

    public static void drawCenteredScaledTextSplit(@NotNull final GuiGraphicsExtractor graphics, int x, int y, float scale, String text, int color, int maxLength, int zLevel) {
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(Component.empty().append(text), (int) (maxLength / scale));

        graphics.pose().pushMatrix();
        graphics.pose().translate(x - x * scale, y - y * scale);
        graphics.pose().scale(scale, scale);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            graphics.text(Minecraft.getInstance().font, line, (x - Minecraft.getInstance().font.width(line) / 2), y + i * Minecraft.getInstance().font.lineHeight, color, true);
        }

        graphics.pose().popMatrix();
    }

    public static void drawScaledTextSplit(@NotNull final GuiGraphicsExtractor graphics, float x, float y, float scale, Component text, int color, int maxLength, int zLevel) {
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(text, (int) (maxLength / scale));

        graphics.pose().pushMatrix();
        graphics.pose().translate(x - x * scale, y - y * scale);
        graphics.pose().scale(scale, scale);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            graphics.text(Minecraft.getInstance().font, line, (int)x, (int)(y + i * Minecraft.getInstance().font.lineHeight), color, true);
        }

        graphics.pose().popMatrix();
    }
}
