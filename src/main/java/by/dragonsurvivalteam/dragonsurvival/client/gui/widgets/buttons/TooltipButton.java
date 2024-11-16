package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.List;

public class TooltipButton extends ExtendedButton implements TooltipProvider {
    public TooltipButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
        super(xPos, yPos, width, height, displayString, handler);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of();
    }
}
