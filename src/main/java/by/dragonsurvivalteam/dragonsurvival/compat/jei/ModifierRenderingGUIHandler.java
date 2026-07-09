package by.dragonsurvivalteam.dragonsurvival.compat.jei;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.CreativeModeInventoryScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.EffectsInInventoryAccessor;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.InventoryScreenAccessor;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class ModifierRenderingGUIHandler implements IGlobalGuiHandler {
    @Override
    public @NotNull Collection<Rect2i> getGuiExtraAreas() {
        EffectsInInventory effects = getEffectsInInventory(Minecraft.getInstance().screen);

        if (effects == null) {
            return Collections.emptyList();
        }

        return ((EffectsInInventoryAccessor) effects).dragonSurvival$areasBlockedByModifierUIForJEI();
    }

    private static EffectsInInventory getEffectsInInventory(final Screen screen) {
        if (screen instanceof InventoryScreen inventoryScreen) {
            return ((InventoryScreenAccessor) inventoryScreen).dragonSurvival$getEffectsInInventory();
        }

        if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
            return ((CreativeModeInventoryScreenAccessor) creativeScreen).dragonSurvival$getEffectsInInventory();
        }

        return null;
    }
}
