package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.ConfirmableScreen;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonEditorConfirmComponent extends AbstractContainerEventHandler implements Renderable {
    @Translation(comments = "\n§4All progress will be lost!§r§f Use the Dragon Soul to avoid this.\n\nWould you still like to continue?")
    private final static String CONFIRM_LOSE_ALL = Translation.Type.GUI.wrap("dragon_editor.confirm.all");

    @Translation(comments = "\n§4Your growth progress will be lost!§r§f Use the Dragon Soul to avoid this.\n\nWould you still like to continue?")
    private final static String CONFIRM_LOSE_GROWTH = Translation.Type.GUI.wrap("dragon_editor.confirm.growth");

    @Translation(comments = "\n§4Your ability progress will be lost!§r§f Use the Dragon Soul to avoid this.\n\nWould you still like to continue?")
    private final static String CONFIRM_LOSE_ABILITIES = Translation.Type.GUI.wrap("dragon_editor.confirm.abilities");

    private static final Identifier WARNING_MAIN = Identifier.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/warning_main.png");
    private static final Identifier WARNING_ACCEPT = Identifier.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/warning_accept.png");
    private static final Identifier WARNING_CANCEL = Identifier.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/warning_cancel.png");

    private final AbstractWidget confirmButton;
    private final AbstractWidget cancelButton;

    public boolean visible;
    public boolean isBodyTypeChange;

    private final int x;
    private final int y;
    private final int xSize;
    private final int ySize;

    public DragonEditorConfirmComponent(ConfirmableScreen screen, int x, int y, int xSize, int ySize) {
        this.x = x;
        this.y = y;
        this.xSize = xSize;
        this.ySize = ySize;
        this.isBodyTypeChange = false;

        confirmButton = new ExtendedButton(x + 3, y + 132, 60, 19, CommonComponents.GUI_YES, action -> { /* Nothing to do */ }) {
            @Override
            public void extractWidgetRenderState(@NotNull final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
                graphics.centeredText(Minecraft.getInstance().font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, getFGColor());
            }

            @Override
            public void onPress(@NotNull InputWithModifiers inputWithModifiers) {
                screen.confirm();
            }
        };
        confirmButton.setTooltip(Tooltip.create(Component.translatable(LangKey.GUI_CONFIRM)));

        cancelButton = new ExtendedButton(x + 66, y + 132, 60, 19, CommonComponents.GUI_NO, action -> { /* Nothing to do */ }) {
            @Override
            public void extractWidgetRenderState(@NotNull final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
                graphics.centeredText(Minecraft.getInstance().font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, getFGColor());
            }

            @Override
            public void onPress(@NotNull InputWithModifiers inputWithModifiers) {
                screen.cancel();
            }
        };
        cancelButton.setTooltip(Tooltip.create(Component.translatable(LangKey.GUI_CANCEL)));
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return ImmutableList.of(confirmButton, cancelButton);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void extractRenderState(@NotNull final GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTicks) {
        graphics.pose().pushMatrix();
        // Render above the rendered dragon
        // graphics.pose().translate(0, 0, 100);
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
            graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_ACCEPT, x, y, 0, 0, xSize, ySize, 256, 256);
        } else if (cancelButton.isHovered()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_CANCEL, x, y, 0, 0, xSize, ySize, 256, 256);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_MAIN, x, y, 0, 0, xSize, ySize, 256, 256);
        }

        TextRenderUtil.drawCenteredScaledTextSplit(graphics, x + xSize / 2, y + 42, 1f, text, DyeColor.WHITE.getTextColor(), xSize - 10, 150);

        confirmButton.extractRenderState(graphics, pMouseX, pMouseY, pPartialTicks);
        cancelButton.extractRenderState(graphics, pMouseX, pMouseY, pPartialTicks);
        graphics.pose().popMatrix();
    }
}
