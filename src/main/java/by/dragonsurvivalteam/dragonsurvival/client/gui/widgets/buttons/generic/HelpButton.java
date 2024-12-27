package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HelpButton extends ExtendedButton {
    private static final ResourceLocation INFO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/info_hover.png");
    private static final ResourceLocation INFO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/info_main.png");

    private final String text;

    private boolean usesVanillaTooltip;

    private ResourceLocation infoHover = INFO_HOVER;
    private ResourceLocation infoMain = INFO_MAIN;
    private int uWidth = -1;
    private int vHeight = -1;
    private int textureWidth = -1;
    private int textureHeight = -1;

    public HelpButton(int x, int y, int sizeX, int sizeY, String text) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        this.text = text;
    }

    // This is needed for the DragonScreen, as otherwise we'll get cut out by the scissoring used for the rendering of the player entity in the window
    public HelpButton(int x, int y, int sizeX, int sizeY, String text, boolean usesVanillaTooltip) {
        this(x, y, sizeX, sizeY, text);
        if (usesVanillaTooltip) {
            setTooltip(Tooltip.create(Component.translatable(text)));
        }
        this.usesVanillaTooltip = usesVanillaTooltip;
    }

    public HelpButton(int x, int y, int sizeX, int sizeY, String text, ResourceLocation infoHover, ResourceLocation infoMain, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        this.text = text;
        this.infoHover = infoHover;
        this.infoMain = infoMain;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() && !usesVanillaTooltip) {
            // Render the tooltip manually since minecraft's tooltip positioner often fails with this button type
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, List.of(Component.translatable(text)), mouseX, mouseY);
        }

        if(!isHovered()) {
            guiGraphics.blit(infoMain, getX(), getY(), width, height, 0, 0, 13, 13, 16, 16);
        } else {
            guiGraphics.blit(infoHover, getX(), getY(), width, height, 0, 0, 13, 13, 16, 16);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}