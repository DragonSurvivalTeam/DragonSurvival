package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.magic.AbilityAndPenaltyTooltipRenderer;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
            Functions.logOrThrow("Penalties with no icon should not be added as button - [" + penalty.getKey().location() + "] is invalid");
        }
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation icon = penalty.value().icon().orElse(MissingTextureAtlasSprite.getLocation());
        blit(graphics, icon, getX(), getY(), SIZE);

        if (isHovered()) {
            graphics.pose().pushPose();
            // Render above the other UI elements
            graphics.pose().translate(0, 0, 250);
            AbilityAndPenaltyTooltipRenderer.drawPenaltyTooltip(graphics, mouseX, mouseY, penalty);
            graphics.pose().popPose();
        }
    }

    // TODO :: add in generic helper method
    private void blit(final GuiGraphics graphics, final ResourceLocation texture, int x, int y, int size) {
        graphics.blit(x, y, 0, size, size, Minecraft.getInstance().getGuiSprites().getSprite(texture), 1, 1, 1, alpha);
    }
}
