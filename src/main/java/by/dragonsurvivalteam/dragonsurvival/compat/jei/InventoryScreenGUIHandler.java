package by.dragonsurvivalteam.dragonsurvival.compat.jei;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.EffectRenderingInventoryScreenAccessor;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InventoryScreenGUIHandler implements IGuiContainerHandler<InventoryScreen> {
    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull InventoryScreen containerScreen) {
        return ((EffectRenderingInventoryScreenAccessor)containerScreen).dragonSurvival$areasBlockedByModifierUIForJEI();
    }
}
