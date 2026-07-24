package by.dragonsurvivalteam.dragonsurvival.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class TooltipUtils {
    private static final String EMPTY = Component.empty().getString();

    public static Tooltip createTooltip(final Component tooltip, int maxWidth) {
        List<FormattedText> formattedTexts = Minecraft.getInstance().font.getSplitter().splitLines(tooltip, maxWidth, Style.EMPTY);
        MutableComponent base = Component.empty();

        for (FormattedText formattedText : formattedTexts) {
            base.append(formattedText.getString());
        }

        return Tooltip.create(base);
    }

    public static boolean needsTooltip(final AbstractWidget widget) {
        Tooltip tooltip = widget.getTooltip();

        if (tooltip == null) {
            return true;
        }

        List<FormattedCharSequence> message = tooltip.toCharSequence(Minecraft.getInstance());
        return message.isEmpty();
    }
}