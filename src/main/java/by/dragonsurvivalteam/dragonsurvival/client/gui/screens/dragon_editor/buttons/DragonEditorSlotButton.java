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

import java.awt.*;
import java.util.function.Function;

public class DragonEditorSlotButton extends Button {
    private final DragonEditorScreen screen;
    private final Function<Integer, Integer> setDragonSlotAction;
    private final ResourceLocation SLOT_NUMBER_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/slot_number_background.png");

    public int num;

    public DragonEditorSlotButton(int x, int y, int num, DragonEditorScreen screen) {
        super(x, y, 12, 12, Component.empty(), button -> { /* Nothing to do */ }, DEFAULT_NARRATION);

        this.num = num;
        this.screen = screen;

        setDragonSlotAction = slot -> {
            int prevSlot = this.screen.selectedSaveSlot;

            this.screen.selectedSaveSlot = slot;
            this.screen.update();
            DragonEditorScreen.HANDLER.recompileCurrentSkin();
            return prevSlot;
        };
    }

    @Override
    public void onPress() {
        screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(setDragonSlotAction, num - 1));
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        active = visible = screen.showUi;

        if (screen.selectedSaveSlot == num - 1) {
            guiGraphics.blit(SLOT_NUMBER_BACKGROUND, getX(), getY(), 0, 0, 20, 20, 20, 20);
        }

        if(screen.selectedSaveSlot == num - 1) {
            TextRenderUtil.drawScaledText(guiGraphics, getX() + 4.5f, getY() + 3.5f, 1F, Integer.toString(num), DyeColor.WHITE.getTextColor());
        } else {
            TextRenderUtil.drawScaledText(guiGraphics, getX() + 4.5f, getY() + 3.5f, 1F, Integer.toString(num), DyeColor.GRAY.getTextColor());
        }
    }
}