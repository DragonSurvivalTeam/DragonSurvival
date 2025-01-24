package by.dragonsurvivalteam.dragonsurvival.compat.jei;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.EffectRenderingInventoryScreenAccessor;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class ModifierRenderingGUIHandler implements IGlobalGuiHandler {
    @Override
    public @NotNull Collection<Rect2i> getGuiExtraAreas() {
        if(Minecraft.getInstance().screen instanceof EffectRenderingInventoryScreen<?> containerScreen) {
            return ((EffectRenderingInventoryScreenAccessor)containerScreen).dragonSurvival$areasBlockedByModifierUIForJEI();
        }

        return Collections.emptyList();
    }
}
