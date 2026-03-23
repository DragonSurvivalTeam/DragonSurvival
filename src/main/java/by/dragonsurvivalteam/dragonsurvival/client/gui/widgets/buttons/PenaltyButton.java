package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.render.AbilityAndPenaltyTooltipRenderer;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class PenaltyButton extends ExtendedButton {
    private static final int SIZE = 35;

    private final Holder<DragonPenalty> penalty;

    public PenaltyButton(int xPos, int yPos, final Holder<DragonPenalty> penalty) {
        super(xPos, yPos, SIZE, SIZE, Component.empty(), action -> { /* Nothing to do */ });
        this.penalty = penalty;

        if (penalty.value().icon().isEmpty()) {
            //noinspection DataFlowIssue -> key is present
            Functions.logOrThrow("Penalties with no icon should not be added as button - [" + penalty.getKey().identifier() + "] is invalid");
        }
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Identifier icon = penalty.value().icon().orElse(MissingTextureAtlasSprite.getLocation());
        blit(graphics, icon, getX(), getY(), SIZE);

        if (isHovered()) {
            graphics.pose().pushMatrix();
            graphics.nextStratum();
            AbilityAndPenaltyTooltipRenderer.drawPenaltyTooltip(graphics, mouseX, mouseY, penalty);
            graphics.pose().popMatrix();
        }
    }

    // TODO :: add in generic helper method
    private void blit(final GuiGraphics graphics, final Identifier texture, int x, int y, int size) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, size, size);
    }
}
