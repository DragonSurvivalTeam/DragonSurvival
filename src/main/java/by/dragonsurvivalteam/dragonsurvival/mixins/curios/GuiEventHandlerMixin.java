package by.dragonsurvivalteam.dragonsurvival.mixins.curios;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.theillusivec4.curios.client.gui.GuiEventHandler;

@Mixin(GuiEventHandler.class)
public abstract class GuiEventHandlerMixin {
    @Definition(id = "screen", local = @Local(type = Screen.class, name = "screen"))
    @Definition(id = "InventoryScreen", type = InventoryScreen.class)
    @Expression("screen instanceof InventoryScreen")
    @ModifyExpressionValue(method = "onInventoryGuiInit", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean dragonSurvival$addCuriosButtonToDragonInventory(final boolean original, @Local(name = "screen") final Screen screen) {
        if (screen instanceof DragonInventoryScreen) {
            return true;

        }

        return original;
    }
}
