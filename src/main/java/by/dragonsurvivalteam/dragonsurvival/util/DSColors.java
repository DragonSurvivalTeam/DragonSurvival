package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.FastColor;

/** Colors are defined as RGB (no alpha) */
public class DSColors {
    public static final int NONE = -1;

    /** {@link ChatFormatting#BLACK} */
    public static final int BLACK = 0x000000;

    /** {@link ChatFormatting#WHITE} */
    public static final int WHITE = 0xFFFFFF;

    /** {@link ChatFormatting#BLUE} */
    public static final int BLUE = 0x5555FF;

    /** {@link ChatFormatting#GRAY} */
    public static final int GRAY = 0xAAAAAA;

    /** {@link ChatFormatting#GOLD} */
    public static final int GOLD = 0xFFAA00;

    /** {@link ChatFormatting#DARK_RED} */
    public static final int DARK_RED = 0xAA0000;

    public static final int GREEN = 0x57882F;

    public static final int RED = 0xF3303B;
    public static final int DARK_GRAY = 0x262626;
    public static final int LIGHT_PURPLE = 0x9A849A;
    public static final int DARK_PURPLE = 0x594459;

    public static final int PASSIVE_BACKGROUND = 0x7F912E;
    public static final int ACTIVE_BACKGROUND = 0xC8861F;

    // TODO :: remove in the future - previous top & bottom colors for the tooltip
    public static final int CAVE_TOP = 0xFF7686;
    public static final int CAVE_BOTTOM = 0x8B424A;

    public static final int FOREST_TOP = 0x00FF94;
    public static final int FOREST_BOTTOM = 0x048252;

    public static final int SEA_TOP = 0x5DC9FF;
    public static final int SEA_BOTTOM = 0x316D90;

    public record RGB(float red, float green, float blue){
        public static RGB of(int rgb) {
            float red = (rgb >> 16) & 0xFF;
            float green = (rgb >> 8) & 0xFF;
            float blue = rgb & 0xFF;
            return new RGB(red / 255f, green / 255f, blue / 255f);
        }
    }

    public static MutableComponent dynamicValue(final Object value) {
        return withColor(value, BLUE);
    }

    public static MutableComponent withColor(final Object value, int color) {
        if (value instanceof MutableComponent mutable) {
            return mutable.withColor(color);
        }

        return Component.literal(String.valueOf(value)).withColor(color);
    }

    public static int withoutAlpha(int rgba) {
        return rgba >>> 8;
    }

    /** Returns a color in the format of {@link FastColor.ARGB32} */
    public static int withAlpha(int rgb, float alpha) {
        return FastColor.ARGB32.color((int) (255 * alpha), rgb);
    }

    /** Returns a color in the format of {@link FastColor.ARGB32} */
    public static int toARGB(final TextColor color) {
        return FastColor.ARGB32.color(255, color.getValue());
    }

    /** Returns a color in the format of {@link FastColor.ARGB32} */
    public static int toARGB(final ColorRGBA color) {
        int rgba = color.rgba();

        int red = (rgba >> 24) & 0xFF;
        int green = (rgba >> 16) & 0xFF;
        int blue = (rgba >> 8) & 0xFF;
        int alpha = rgba & 0xFF;

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }
}
