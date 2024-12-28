package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.magic.AbilityAndPenaltyTooltipRenderer;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class PenaltyButton extends ExtendedButton  {
    private static final int SIZE = 35;

    private final Holder<DragonPenalty> penalty;

    public PenaltyButton(int xPos, int yPos, final Holder<DragonPenalty> penalty) {
        super(xPos, yPos, SIZE, SIZE, Component.empty(), action -> { /* Nothing to do */ });
        this.penalty = penalty;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX() - 1, getY() - 1, getX() + SIZE + 1, getY() + SIZE + 1, 0xFF000000);
        blit(graphics, penalty.value().icon(), getX(), getY(), SIZE);

        if (isHovered()) {
            graphics.pose().pushPose();
            // Render above the other UI elements
            graphics.pose().translate(0, 0, 250);
            AbilityAndPenaltyTooltipRenderer.drawPenaltyTooltip(graphics, mouseX, mouseY, penalty);
            graphics.pose().popPose();
        }
    }

    private void blit(final GuiGraphics graphics, final ResourceLocation texture, int x, int y, int size) {
        graphics.blit(x, y, 0, size, size, Minecraft.getInstance().getGuiSprites().getSprite(texture), 1, 1, 1, alpha);
    }
}
