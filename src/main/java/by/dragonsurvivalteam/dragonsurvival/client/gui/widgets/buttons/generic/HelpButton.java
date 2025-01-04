package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HelpButton extends ExtendedButton {
    private static final ResourceLocation MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_main.png");
    private static final ResourceLocation HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_hover.png");
    private static final int TEXTURE_SIZE = 16;
    private static final int UV = 13;

    private final String tooltip;
    private final ResourceLocation hover;
    private final ResourceLocation main;

    public HelpButton(int x, int y, int sizeX, int sizeY, final String tooltip) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        this.tooltip = tooltip;
        this.main = MAIN;
        this.hover = HOVER;
    }

    public HelpButton(int x, int y, int sizeX, int sizeY, final String tooltip, final ResourceLocation main, final ResourceLocation hover) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        this.tooltip = tooltip;
        this.main = main;
        this.hover = hover;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered()) {
            // Render the tooltip manually since minecraft's tooltip positioner often fails with this button type
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, List.of(Component.translatable(tooltip)), mouseX, mouseY);
        }

        if (!isHovered()) {
            guiGraphics.blit(main, getX(), getY(), width, height, 0, 0, UV, UV, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            guiGraphics.blit(hover, getX(), getY(), width, height, 0, 0, UV, UV, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}