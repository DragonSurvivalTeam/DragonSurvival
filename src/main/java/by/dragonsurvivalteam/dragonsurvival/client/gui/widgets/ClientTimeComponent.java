package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class ClientTimeComponent implements ClientTooltipComponent {
    private static final int ICON_SIZE = 20;

    private final TimeComponent component;
    private final Component tooltip;

    public ClientTimeComponent(final TimeComponent component) {
        this.component = component;
        this.tooltip = component.description().apply(component.item(), component.ticks());
    }

    @Override
    public int getHeight() {
        return ICON_SIZE - 2;
    }

    @Override
    public int getWidth(@NotNull final Font font) {
        return ICON_SIZE + font.width(tooltip);
    }

    @Override
    public void renderImage(@NotNull final Font font, int x, int y, @NotNull final GuiGraphics graphics) {
        graphics.renderFakeItem(component.item().getDefaultInstance(), x, y);
    }

    @Override
    public void renderText(@NotNull final Font font, int mouseX, int mouseY, @NotNull final Matrix4f matrix, @NotNull final MultiBufferSource.BufferSource bufferSource) {
        // Y offset to align the height of the text with the center of the item
        font.drawInBatch(tooltip, mouseX + ICON_SIZE, mouseY + 4, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }
}
