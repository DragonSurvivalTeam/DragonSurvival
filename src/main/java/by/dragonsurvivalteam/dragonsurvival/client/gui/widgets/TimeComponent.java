package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;

public record TimeComponent(Item item, int ticks) implements TooltipComponent { /* Nothing to do */ }
