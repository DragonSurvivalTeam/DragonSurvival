package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ClientTimeComponent implements ClientTooltipComponent {
    private static final int ICON_SIZE = 20;

    private final TimeComponent component;
    private final Component tooltip;

    public ClientTimeComponent(final TimeComponent component) {
        this.component = component;
        this.tooltip = component.description().apply(component.item(), component.ticks());
    }

    @Override
    public int getHeight(@NotNull final Font font) {
        return ICON_SIZE - 2;
    }

    @Override
    public int getWidth(@NotNull final Font font) {
        return ICON_SIZE + font.width(tooltip);
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphics graphics) {
        graphics.renderFakeItem(component.item().getDefaultInstance(), x, y);
    }

    @Override
    public void renderText(@NotNull GuiGraphics graphics, @NotNull Font font, int x, int y) {
        // Offset the text slightly so it centers against the 16x16 item icon.
        graphics.drawString(font, tooltip, x + ICON_SIZE, y + 4, 0xFFFFFFFF, true);
    }
}
