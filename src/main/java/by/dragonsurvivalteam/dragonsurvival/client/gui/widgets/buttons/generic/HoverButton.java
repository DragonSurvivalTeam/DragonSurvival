package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class HoverButton extends ExtendedButton implements HoverDisableable {
    private final ResourceLocation main;
    private final ResourceLocation hover;
    private final Supplier<List<Either<FormattedText, TooltipComponent>>> customTooltip;

    private boolean disableHover;
    private final int originalWidth;
    private final int originalHeight;
    private final int textureWidth;
    private final int textureHeight;
    private final int uOffset;
    private final int vOffset;
    private Vec2 scale = new Vec2(1, 1);
    private Vec2 offset = new Vec2(0, 0);

    public HoverButton(int x, int y, int size, final ResourceLocation main, final ResourceLocation hover) {
        this(x, y, size, size, size, size, 0, 0, main, hover, button -> { /* Nothing to do */ }, null);
    }

    public HoverButton(int x, int y, int size, final ResourceLocation main, final ResourceLocation hover, final Supplier<List<Either<FormattedText, TooltipComponent>>> customTooltip) {
        this(x, y, size, size, size, size, 0, 0, main, hover, button -> { /* Nothing to do */ }, customTooltip);
    }

    public HoverButton(int x, int y, int width, int height, int textureWidth, int textureHeight, final ResourceLocation main, final ResourceLocation hover, final OnPress onPress) {
        this(x, y, width, height, textureWidth, textureHeight, 0, 0, main, hover, onPress, null);
    }
    
    public HoverButton(int x, int y, int width, int height, int textureWidth, int textureHeight, int uOffset, int vOffset, final ResourceLocation main, final ResourceLocation hover, final OnPress onPress, final Supplier<List<Either<FormattedText, TooltipComponent>>> customTooltip) {
        super(x, y, width, height, Component.empty(), onPress);
        this.main = main;
        this.hover = hover;
        this.customTooltip = customTooltip;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.originalWidth = width;
        this.originalHeight = height;
    }

    public void disableHover() {
        this.disableHover = true;
    }

    public void enableHover() {
        this.disableHover = false;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scale = new Vec2(scaleX, scaleY);
    }

    public void setOffset(float offsetX, float offsetY) {
        this.offset = new Vec2(offsetX, offsetY);
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
        graphics.pose().pushPose();
        graphics.pose().translate(getX(), getY(), 0);

        float scaleX = this.scale.x == 1 ? (float) width / (float) originalWidth : this.scale.x;
        float scaleY = this.scale.y == 1 ? (float) height / (float) originalHeight : this.scale.y;
        graphics.pose().scale(scaleX, scaleY, 1);
        graphics.pose().translate(-getX(), -getY(), 0);
        float scaleXDiff = (scale.x - 1) * originalWidth / 2;
        float scaleYDiff = (scale.y - 1) * originalHeight / 2;
        graphics.pose().translate(offset.x - scaleXDiff, offset.y - scaleYDiff, 0);

        ResourceLocation texture = isHovered() ? hover : main;
        graphics.blit(texture, getX(), getY(), uOffset, vOffset, originalWidth, originalHeight, textureWidth, textureHeight);
        graphics.pose().popPose();

        this.renderString(graphics, Minecraft.getInstance().font, getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24);
        renderTooltip(graphics, mouseX, mouseY);
    }

    public void renderTooltip(final GuiGraphics graphics, int mouseX, int mouseY) {
        if (customTooltip == null || !isHovered()) {
            return;
        }

        graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, customTooltip.get(), mouseX, mouseY, ItemStack.EMPTY);
    }
}
