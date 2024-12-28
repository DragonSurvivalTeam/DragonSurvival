package by.dragonsurvivalteam.dragonsurvival.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TextRenderUtil {
    public static void drawScaledText(@NotNull final GuiGraphics guiGraphics, float x, float y, float scale, String text, int color) {
        drawScaledText(guiGraphics, x, y, scale, text, color, 0);
    }

    public static void drawScaledText(@NotNull final GuiGraphics guiGraphics, float x, float y, float scale, String text, int color, int zLevel) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x - x * scale, y - y * scale, zLevel);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, color, /* TODO 1.20 :: Check */ false);
        guiGraphics.pose().popPose();
    }

    public static void drawCenteredScaledText(@NotNull final GuiGraphics guiGraphics, int x, int y, float scale, String text, int color) {
        drawCenteredScaledText(guiGraphics, x, y, scale, text, color, 0);
    }

    public static void drawCenteredScaledText(@NotNull final GuiGraphics guiGraphics, int x, int y, float scale, String text, int color, int zLevel) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x - x * scale, y - y * scale, zLevel);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, x, y, color);
        guiGraphics.pose().popPose();
    }

    public static void drawCenteredScaledTextSplit(@NotNull final GuiGraphics guiGraphics, int x, int y, float scale, String text, int color, int maxLength, int zLevel) {
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(Component.empty().append(text), (int) (maxLength / scale));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x - x * scale, y - y * scale, zLevel);
        guiGraphics.pose().scale(scale, scale, 1);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            guiGraphics.drawString(Minecraft.getInstance().font, line, (x - Minecraft.getInstance().font.width(line) / 2), y + i * Minecraft.getInstance().font.lineHeight, color, true);
        }

        guiGraphics.pose().popPose();
    }

    public static void drawScaledTextSplit(@NotNull final GuiGraphics guiGraphics, float x, float y, float scale, Component text, int color, int maxLength, int zLevel) {
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(text, (int) (maxLength / scale));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x - x * scale, y - y * scale, zLevel);
        guiGraphics.pose().scale(scale, scale, 1);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            guiGraphics.drawString(Minecraft.getInstance().font, line, x, y + i * Minecraft.getInstance().font.lineHeight, color, true);
        }

        guiGraphics.pose().popPose();
    }
}