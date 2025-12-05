package by.dragonsurvivalteam.dragonsurvival.mixins.curios;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import by.dragonsurvivalteam.dragonsurvival.compat.curios.CuriosButtonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.gui.CuriosButton;

@Mixin(CuriosButton.class)
public abstract class CuriosButtonMixin {
    @Inject(method = "lambda$new$0", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/PacketDistributor;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;[Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private static void dragonSurvival$rememberParent(final AbstractContainerScreen<?> parent, final Button button, final CallbackInfo callback) {
        if (parent instanceof DragonInventoryScreen screen) {
            MouseHandler handler = Minecraft.getInstance().mouseHandler;
            CuriosButtonHandler.previousMouseX = (int) handler.xpos();
            CuriosButtonHandler.previousMouseY = (int) handler.ypos();
            CuriosButtonHandler.previousGuiLeft = screen.getGuiLeft();
        }
    }
}
