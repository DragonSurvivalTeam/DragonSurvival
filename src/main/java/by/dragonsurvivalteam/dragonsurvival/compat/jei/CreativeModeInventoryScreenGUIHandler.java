package by.dragonsurvivalteam.dragonsurvival.compat.jei;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.EffectRenderingInventoryScreenAccessor;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreativeModeInventoryScreenGUIHandler implements IGuiContainerHandler<CreativeModeInventoryScreen> {
    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull CreativeModeInventoryScreen containerScreen) {
        return ((EffectRenderingInventoryScreenAccessor)containerScreen).dragonSurvival$areasBlockedByModifierUIForJEI();
    }
}
