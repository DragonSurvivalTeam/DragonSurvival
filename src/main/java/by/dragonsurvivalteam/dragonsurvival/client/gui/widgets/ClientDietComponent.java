package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ClientDietComponent implements ClientTooltipComponent {
    private static final int ICON_SIZE = 20;

    private final DietComponent component;
    private final Component tooltip;

    public ClientDietComponent(final DietComponent component) {
        this.component = component;
        this.tooltip = Component.translatable(component.item().getDescriptionId()).append(": ").append(ToolTipHandler.getFoodTooltipData(component.species(), component.item()));
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return ICON_SIZE - 2;
    }

    @Override
    public int getWidth(@NotNull final Font font) {
        return ICON_SIZE + font.width(tooltip);
    }

    @Override
    public void extractImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphicsExtractor graphics) {
        graphics.fakeItem(component.item().getDefaultInstance(), x, y);
    }

    @Override
    public void extractText(@NotNull GuiGraphicsExtractor graphics, Font font, int x, int y) {
        // Offset the text slightly so it centers against the 16x16 item icon.
        graphics.text(font, tooltip, x + ICON_SIZE, y + 4, 0xFFFFFFFF, true);
    }
}
