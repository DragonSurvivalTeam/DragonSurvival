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

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HelpButton extends ExtendedButton {
    private static final ResourceLocation MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_main.png");
    private static final ResourceLocation HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_hover.png");
    private static final int TEXTURE_SIZE = 16;
    private static final int UV = 13;

    private final ResourceLocation hover;
    private final ResourceLocation main;

    private List<Either<FormattedText, TooltipComponent>> tooltip;

    public HelpButton(int x, int y, int sizeX, int sizeY, final String tooltip) {
        this(x, y, sizeX, sizeY, new ArrayList<>(List.of(Either.left(Component.translatable(tooltip)))));
    }

    public HelpButton(int x, int y, int sizeX, int sizeY, final List<Either<FormattedText, TooltipComponent>> tooltip) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        this.tooltip = tooltip;
        this.main = MAIN;
        this.hover = HOVER;
    }

    public HelpButton(int x, int y, int sizeX, int sizeY, final String tooltip, final ResourceLocation main, final ResourceLocation hover) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        this.tooltip = new ArrayList<>(List.of(Either.left(Component.translatable(tooltip))));
        this.main = main;
        this.hover = hover;
    }

    public void setTooltip(final List<Either<FormattedText, TooltipComponent>> tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation resource;

        if (isHovered()) {
            graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, tooltip, mouseX, mouseY, ItemStack.EMPTY);
            resource = hover;
        } else {
            resource = main;
        }

        graphics.blit(resource, getX(), getY(), width, height, 0, 0, UV, UV, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}