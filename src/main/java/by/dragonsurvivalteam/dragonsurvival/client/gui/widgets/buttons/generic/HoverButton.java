package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import javax.tools.Tool;
import java.util.List;
import java.util.function.Supplier;

public class HoverButton extends ExtendedButton {
    private final ResourceLocation hover;
    private final ResourceLocation main;

    private final int uOffset;
    private final int vOffset;
    private final int textureWidth;
    private final int textureHeight;
    private Supplier<List<Either<FormattedText, TooltipComponent>>> customTooltip = null;

    public HoverButton(int xPos, int yPos, int width, int height, int uOffset, int vOffset, int textureWidth, int textureHeight, OnPress handler, ResourceLocation hover, ResourceLocation main) {
        super(xPos, yPos, width, height, Component.empty(), handler);
        this.hover = hover;
        this.main = main;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public HoverButton(int xPos, int yPos, int width, int height, int uOffset, int vOffset, int textureWidth, int textureHeight, Supplier<List<Either<FormattedText, TooltipComponent>>> customTooltip, OnPress handler, ResourceLocation hover, ResourceLocation main) {
        this(xPos, yPos, width, height, uOffset, vOffset, textureWidth, textureHeight, handler, hover, main);
        this.customTooltip = customTooltip;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = isHovered() ? hover : main;
        graphics.blit(texture, getX(), getY(), uOffset, vOffset, width, height, textureWidth, textureHeight);
        if(customTooltip != null && isHovered()) {
            graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, customTooltip.get(), mouseX, mouseY, ItemStack.EMPTY);
        }
    }
}
