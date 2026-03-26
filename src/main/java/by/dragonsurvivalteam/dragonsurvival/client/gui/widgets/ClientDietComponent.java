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
        // Y offset to align the height of the text with the center of the item
        // FIXME :: UI RENDERING
        //font.drawInBatch(tooltip, x + ICON_SIZE, y + 4, -1, true, graphics.pose(), bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }
}
