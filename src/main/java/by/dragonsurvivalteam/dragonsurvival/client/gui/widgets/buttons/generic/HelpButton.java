package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HelpButton extends ExtendedButton {
    private static final Identifier MAIN = Identifier.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_main.png");
    private static final Identifier HOVER = Identifier.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_hover.png");
    private static final int TEXTURE_SIZE = 16;
    private static final int UV = 13;

    private final Identifier hover;
    private final Identifier main;

    public HelpButton(int x, int y, int sizeX, int sizeY, final String tooltip) {
        this(x, y, sizeX, sizeY, Tooltip.create(Component.translatable(tooltip)));
    }

    public HelpButton(int x, int y, int sizeX, int sizeY, final Tooltip tooltip) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        setTooltip(tooltip);
        this.main = MAIN;
        this.hover = HOVER;
    }

    public HelpButton(int x, int y, int sizeX, int sizeY, final String tooltip, final Identifier main, final Identifier hover) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        setTooltip(Tooltip.create(Component.translatable(tooltip)));
        this.main = main;
        this.hover = hover;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Identifier resource;

        if (isHovered()) {
            resource = hover;
        } else {
            resource = main;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, resource, getX(), getY(), width, height, 0, 0, UV, UV, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }
}