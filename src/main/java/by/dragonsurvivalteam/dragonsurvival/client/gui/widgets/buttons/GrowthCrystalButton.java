package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class GrowthCrystalButton extends ExtendedButton {
    private final Holder<DragonStage> stage;

    public GrowthCrystalButton(int xPos, int yPos, Holder<DragonStage> stage) {
        super(xPos, yPos, 8, 16, Component.empty(), action -> { /* Nothing to do */ });
        this.stage = stage;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setTooltip(Tooltip.create(DragonStage.growthStageTooltip(stage)));
        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
        double percentageFull = stage.value().getProgress(handler.getSize());
        if(percentageFull > 1.0) {
            guiGraphics.blit(handler.getType().value().miscResources().growthCrystal().full(), getX(), getY(), 0, 0, width, height, 8, 16);
        } else {
            guiGraphics.blit(handler.getType().value().miscResources().growthCrystal().empty(), getX(), getY(), 0, 0, width, height, 8, 16);
            if(percentageFull > 0.0) {
                int scissorHeight = (int) ((1.0 - percentageFull) * height);
                guiGraphics.enableScissor(getX(), getY() + (height - scissorHeight), getX() + width, getY() + height);
                guiGraphics.blit(handler.getType().value().miscResources().growthCrystal().full(), getX(), getY(), 0, 0, width, height, 8, 16);
                guiGraphics.disableScissor();
            }
        }
    }
}
