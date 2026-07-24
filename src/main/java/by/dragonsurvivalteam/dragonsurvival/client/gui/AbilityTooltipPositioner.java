package by.dragonsurvivalteam.dragonsurvival.client.gui;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class AbilityTooltipPositioner implements ClientTooltipPositioner {
    private final int tooltipLeftWidth;

    public AbilityTooltipPositioner(int tooltipLeftWidth) {
        this.tooltipLeftWidth = tooltipLeftWidth;
    }

    @Override
    public @NotNull Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
        Vector2i vector2i = new Vector2i(mouseX, mouseY).add(12, -12);
        this.positionTooltip(screenWidth, screenHeight, vector2i, tooltipWidth, tooltipHeight);
        return vector2i;
    }

    private void positionTooltip(int screenWidth, int screenHeight, Vector2i tooltipPos, int tooltipRightWidth, int tooltipHeight) {
        final int padding = 3;

        // Use tooltipLeftWidth, tooltipLeftHeight, tooltipRightWidth, and tooltipRightHeight to determine the position of the tooltip
        if (tooltipPos.x + tooltipRightWidth + padding > screenWidth) {
            tooltipPos.x = screenWidth - (tooltipRightWidth + padding);
        } else if (tooltipPos.x - tooltipLeftWidth - padding < 0) {
            tooltipPos.x = padding + tooltipLeftWidth;
        }

        if (tooltipPos.y + tooltipHeight + padding > screenHeight) {
            tooltipPos.y = screenHeight - (tooltipHeight + padding);
        }
    }
}
