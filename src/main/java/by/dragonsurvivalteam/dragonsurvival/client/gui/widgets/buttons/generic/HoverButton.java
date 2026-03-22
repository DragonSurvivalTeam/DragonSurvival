package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class HoverButton extends ExtendedButton implements HoverDisableable {
    private final Identifier main;
    private final Identifier hover;

    private boolean disableHover;
    private final int originalWidth;
    private final int originalHeight;
    private final int textureWidth;
    private final int textureHeight;
    private final int uOffset;
    private final int vOffset;
    private final Vec2 scale = new Vec2(1, 1);
    private final Vec2 offset = new Vec2(0, 0);

    public HoverButton(int x, int y, int size, final Identifier main, final Identifier hover) {
        this(x, y, size, size, size, size, 0, 0, main, hover, button -> { /* Nothing to do */ }, null);
    }

    public HoverButton(int x, int y, int size, final Identifier main, final Identifier hover, final Supplier<Tooltip> customTooltip) {
        this(x, y, size, size, size, size, 0, 0, main, hover, button -> { /* Nothing to do */ }, customTooltip);
    }

    public HoverButton(int x, int y, int width, int height, int textureWidth, int textureHeight, final Identifier main, final Identifier hover, final OnPress onPress) {
        this(x, y, width, height, textureWidth, textureHeight, 0, 0, main, hover, onPress, null);
    }

    public HoverButton(int x, int y, int width, int height, int textureWidth, int textureHeight, int uOffset, int vOffset, final Identifier main, final Identifier hover, final OnPress onPress, final Supplier<Tooltip> customTooltip) {
        super(x, y, width, height, Component.empty(), onPress);
        this.main = main;
        this.hover = hover;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.originalWidth = width;
        this.originalHeight = height;

        if (customTooltip != null) {
            setTooltip(customTooltip.get());
        }
    }

    public void disableHover() {
        this.disableHover = true;
    }

    public void enableHover() {
        this.disableHover = false;
    }

    public boolean isHovered() {
        return !disableHover && super.isHovered();
    }

    public boolean isFocused() {
        return !disableHover && super.isFocused();
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Scale about the center of the button
        graphics.pose().pushMatrix();
        graphics.pose().translate(getX(), getY());

        float scaleX = this.scale.x == 1 ? (float) width / (float) originalWidth : this.scale.x;
        float scaleY = this.scale.y == 1 ? (float) height / (float) originalHeight : this.scale.y;
        graphics.pose().scale(scaleX, scaleY);
        graphics.pose().translate(-getX(), -getY());
        float scaleXDiff = (scale.x - 1) * originalWidth / 2;
        float scaleYDiff = (scale.y - 1) * originalHeight / 2;
        graphics.pose().translate(offset.x - scaleXDiff, offset.y - scaleYDiff);

        Identifier texture = isHovered() ? hover : main;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), uOffset, vOffset, originalWidth, originalHeight, textureWidth, textureHeight);
        graphics.pose().popMatrix();

        this.renderDefaultLabel(graphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }
}
