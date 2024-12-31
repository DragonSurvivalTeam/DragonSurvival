package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class DragonEditorSlotButton extends Button {
    private static final ResourceLocation SLOT_NUMBER_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/slot_number_background.png");

    public int slot;

    private final DragonEditorScreen screen;
    private final Function<Integer, Integer> setDragonSlotAction;
    private final Runnable action;

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
    public void onPress() {
        screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(setDragonSlotAction, slot));
        action.run();
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        active = visible = screen.showUi;

        if (screen.selectedSaveSlot == slot) {
            guiGraphics.blit(SLOT_NUMBER_BACKGROUND, getX(), getY(), 0, 0, 20, 20, 20, 20);
        }

        if (screen.selectedSaveSlot == slot) {
            TextRenderUtil.drawScaledText(guiGraphics, getX() + 4.5f, getY() + 3.5f, 1F, Integer.toString(slot), DyeColor.WHITE.getTextColor());
        } else {
            TextRenderUtil.drawScaledText(guiGraphics, getX() + 4.5f, getY() + 3.5f, 1F, Integer.toString(slot), DyeColor.GRAY.getTextColor());
        }
    }
}