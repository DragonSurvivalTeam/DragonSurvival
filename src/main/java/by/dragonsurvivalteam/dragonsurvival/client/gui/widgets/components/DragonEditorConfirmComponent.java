package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonEditorConfirmComponent extends AbstractContainerEventHandler implements Renderable {
    @Translation(type = Translation.Type.MISC, comments = "\nWith your current config settings all progress will be lost when changing species.\n\nWould you still like to continue?")
    private final static String CONFIRM_LOSE_ALL = Translation.Type.GUI.wrap("dragon_editor.confirm.all");

    @Translation(type = Translation.Type.MISC, comments = "\nWith your current config settings your growth progress will be lost when changing species or body types.\n\nWould you still like to continue?")
    private final static String CONFIRM_LOSE_GROWTH = Translation.Type.GUI.wrap("dragon_editor.confirm.growth");

    @Translation(type = Translation.Type.MISC, comments = "\nWith your current config settings your ability progress will be lost when changing species.\n\nWould you still like to continue?")
    private final static String CONFIRM_LOSE_ABILITIES = Translation.Type.GUI.wrap("dragon_editor.confirm.abilities");

    private static final ResourceLocation WARNING_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/warning_main.png");
    private static final ResourceLocation WARNING_ACCEPT = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/warning_accept.png");
    private static final ResourceLocation WARNING_CANCEL = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/warning_cancel.png");

    private final AbstractWidget confirmButton;
    private final AbstractWidget cancelButton;

    public boolean visible;
    public boolean isBodyTypeChange;

    private final int x;
    private final int y;
    private final int xSize;
    private final int ySize;

    public DragonEditorConfirmComponent(DragonEditorScreen screen, int x, int y, int xSize, int ySize) {
        this.x = x;
        this.y = y;
        this.xSize = xSize;
        this.ySize = ySize;
        this.isBodyTypeChange = false;

        confirmButton = new ExtendedButton(x + 3, y + 132, 60, 19, CommonComponents.GUI_YES, action -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, getFGColor());

                if (isHovered()) {
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable(LangKey.GUI_CONFIRM), mouseX, mouseY);
                }
            }

            @Override
            public void onPress() {
                screen.confirm();
            }
        };

        cancelButton = new ExtendedButton(x + 66, y + 132, 60, 19, CommonComponents.GUI_NO, action -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, getFGColor());

                if (isHovered) {
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable(LangKey.GUI_CANCEL), mouseX, mouseY);
                }
            }

            @Override
            public void onPress() {
                screen.confirmation = false;
                screen.showUi = true;
            }
        };
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return ImmutableList.of(confirmButton, cancelButton);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTicks) {
        graphics.pose().pushPose();
        // Render above the rendered dragon
        graphics.pose().translate(0, 0, 100);
        graphics.fillGradient(0, 0, Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), -1072689136, -804253680);

        String key = "";

        if (!ServerConfig.saveAllAbilities && (!ServerConfig.saveGrowthStage && !isBodyTypeChange)) {
            key = CONFIRM_LOSE_ALL;
        } else if ((ServerConfig.saveAllAbilities || isBodyTypeChange) && !ServerConfig.saveGrowthStage) {
            key = CONFIRM_LOSE_GROWTH;
        } else if (!ServerConfig.saveAllAbilities) {
            key = CONFIRM_LOSE_ABILITIES;
        }

        String text = Component.translatable(key).getString();
        if (confirmButton.isHovered()) {
            graphics.blit(WARNING_ACCEPT, x, y, 0, 0, xSize, ySize);
        } else if (cancelButton.isHovered()) {
            graphics.blit(WARNING_CANCEL, x, y, 0, 0, xSize, ySize);
        } else {
            graphics.blit(WARNING_MAIN, x, y, 0, 0, xSize, ySize);
        }

        TextRenderUtil.drawCenteredScaledTextSplit(graphics, x + xSize / 2, y + 42, 1f, text, DyeColor.WHITE.getTextColor(), xSize - 10, 150);

        confirmButton.render(graphics, pMouseX, pMouseY, pPartialTicks);
        cancelButton.render(graphics, pMouseX, pMouseY, pPartialTicks);
        graphics.pose().popPose();
    }
}