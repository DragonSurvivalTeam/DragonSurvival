package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class HoverButton extends ExtendedButton {
    private final ResourceLocation main;
    private final ResourceLocation hover;
    private final Supplier<List<Either<FormattedText, TooltipComponent>>> customTooltip;

    private final int textureWidth;
    private final int textureHeight;
    private final int uOffset;
    private final int vOffset;

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
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = isHovered() ? hover : main;
        graphics.blit(texture, getX(), getY(), uOffset, vOffset, width, height, textureWidth, textureHeight);

        if (customTooltip != null && isHovered()) {
            graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, customTooltip.get(), mouseX, mouseY, ItemStack.EMPTY);
        }
    }
}
