package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class DragonEditorSlotButton extends ExtendedButton {
    private static final Identifier SLOT_NUMBER_BACKGROUND = Identifier.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/slot_number_background.png");

    private final DragonEditorScreen screen;
    private final Function<Integer, Integer> setDragonSlotAction;
    private final Runnable action;
    private final int slot;

    public DragonEditorSlotButton(int x, int y, int slot, final DragonEditorScreen screen, final Runnable action) {
        super(x, y, 12, 12, Component.empty(), button -> { /* Nothing to do */ }, DEFAULT_NARRATION);
        this.slot = slot;
        this.screen = screen;
        this.action = action;

        setDragonSlotAction = selectedSlot -> {
            int prevSlot = this.screen.selectedSaveSlot;
            this.screen.selectedSaveSlot = selectedSlot;
            this.screen.update();
            DragonEditorScreen.HANDLER.recompileCurrentSkin();
            return prevSlot;
        };
    }

    @Override
    public void onPress(@NotNull InputWithModifiers inputWithModifiers) {
        screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(setDragonSlotAction, slot));
        action.run();
    }

    @Override
    public void extractWidgetRenderState(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        active = visible = screen.showUi;

        if (screen.selectedSaveSlot == slot) {
            GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, SLOT_NUMBER_BACKGROUND, getX(), getY(), 0, 0, 20, 20, 20, 20);
        }

        if (screen.selectedSaveSlot == slot) {
            TextRenderUtil.drawScaledText(GuiGraphicsExtractor, getX() + 4.5f, getY() + 3.5f, 1F, Integer.toString(slot), DyeColor.WHITE.getTextColor());
        } else {
            TextRenderUtil.drawScaledText(GuiGraphicsExtractor, getX() + 4.5f, getY() + 3.5f, 1F, Integer.toString(slot), DyeColor.GRAY.getTextColor());
        }
    }
}