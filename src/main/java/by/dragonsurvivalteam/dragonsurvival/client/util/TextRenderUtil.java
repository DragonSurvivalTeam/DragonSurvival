package by.dragonsurvivalteam.dragonsurvival.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TextRenderUtil {
    public static void drawScaledText(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, float x, float y, float scale, String text, int color) {
        drawScaledText(GuiGraphicsExtractor, x, y, scale, text, color, 0);
    }

    public static void drawScaledText(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, float x, float y, float scale, String text, int color, int zLevel) {
        GuiGraphicsExtractor.pose().pushMatrix();
        GuiGraphicsExtractor.pose().translate(x - x * scale, y - y * scale);
        GuiGraphicsExtractor.pose().scale(scale, scale);
        GuiGraphicsExtractor.drawString(Minecraft.getInstance().font, text, (int)x, (int)y, color, false);
        GuiGraphicsExtractor.pose().popMatrix();
    }

    public static void drawCenteredScaledText(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int x, int y, float scale, String text, int color) {
        drawCenteredScaledText(GuiGraphicsExtractor, x, y, scale, text, color, 0);
    }

    public static void drawCenteredScaledText(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int x, int y, float scale, String text, int color, int zLevel) {
        GuiGraphicsExtractor.pose().pushMatrix();
        GuiGraphicsExtractor.pose().translate(x - x * scale, y - y * scale);
        GuiGraphicsExtractor.pose().scale(scale, scale);
        GuiGraphicsExtractor.drawCenteredString(Minecraft.getInstance().font, text, x, y, color);
        GuiGraphicsExtractor.pose().popMatrix();
    }

    public static void drawCenteredScaledTextSplit(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int x, int y, float scale, String text, int color, int maxLength, int zLevel) {
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(Component.empty().append(text), (int) (maxLength / scale));

        GuiGraphicsExtractor.pose().pushMatrix();
        GuiGraphicsExtractor.pose().translate(x - x * scale, y - y * scale);
        GuiGraphicsExtractor.pose().scale(scale, scale);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            GuiGraphicsExtractor.drawString(Minecraft.getInstance().font, line, (x - Minecraft.getInstance().font.width(line) / 2), y + i * Minecraft.getInstance().font.lineHeight, color, true);
        }

        GuiGraphicsExtractor.pose().popMatrix();
    }

    public static void drawScaledTextSplit(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, float x, float y, float scale, Component text, int color, int maxLength, int zLevel) {
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(text, (int) (maxLength / scale));

        GuiGraphicsExtractor.pose().pushMatrix();
        GuiGraphicsExtractor.pose().translate(x - x * scale, y - y * scale);
        GuiGraphicsExtractor.pose().scale(scale, scale);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            GuiGraphicsExtractor.drawString(Minecraft.getInstance().font, line, (int)x, (int)(y + i * Minecraft.getInstance().font.lineHeight), color, true);
        }

        GuiGraphicsExtractor.pose().popMatrix();
    }
}