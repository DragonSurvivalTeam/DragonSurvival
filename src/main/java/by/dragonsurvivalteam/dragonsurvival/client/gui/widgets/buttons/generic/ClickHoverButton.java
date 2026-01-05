package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class ClickHoverButton extends ExtendedButton {
    private final Identifier click;
    private final Identifier hover;
    private final Identifier main;

    private final int uOffset;
    private final int vOffset;
    private final int textureWidth;
    private final int textureHeight;

    private boolean isClicking;

    public ClickHoverButton(int xPos, int yPos, int width, int height, int uOffset, int vOffset, int textureWidth, int textureHeight, Component displayString, OnPress handler, Identifier click, Identifier hover, Identifier main) {
        super(xPos, yPos, width, height, displayString, handler);
        this.click = click;
        this.hover = hover;
        this.main = main;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Identifier texture = isHovered() ? isClicking ? click : hover : main;
        graphics.blit(texture, getX(), getY(), uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        isClicking = true;
    }

    public void onRelease(@NotNull MouseButtonEvent event) {
        super.onRelease(event);
        isClicking = false;
    }
}
