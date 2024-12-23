package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Function;

public class DragonEditorSlotButton extends Button {
    private final DragonEditorScreen screen;
    private final Function<Integer, Integer> setDragonSlotAction;

    public int num;

    public DragonEditorSlotButton(int x, int y, int num, DragonEditorScreen screen) {
        super(x, y, 12, 12, Component.empty(), button -> { /* Nothing to do */ }, DEFAULT_NARRATION);

        this.num = num;
        this.screen = screen;

        setDragonSlotAction = slot -> {
            int prevSlot = this.screen.selectedSaveSlot;

            this.screen.selectedSaveSlot = slot;
            this.screen.update();
            DragonEditorScreen.HANDLER.getSkinData().compileSkin(screen.dragonStage);
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
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, new Color(1, 1, 1, isHovered ? 0.95F : 0.75F).getRGB());
            guiGraphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, new Color(0.05F, 0.05F, 0.05F, isHovered ? 0.95F : 0.75F).getRGB());
        }

        TextRenderUtil.drawScaledText(guiGraphics, getX() + 2.5f, getY() + 1f, 1.5F, Integer.toString(num), DyeColor.WHITE.getTextColor());
    }
}