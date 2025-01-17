package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Immediate full visibility in water when water vision is active (+ relevant for brightness increase) */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @ModifyExpressionValue(method = "getWaterVision", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;waterVisionTime:I", ordinal = 0))
    private int dragonSurvival$handleWaterVision(int original) {
        if (VisionHandler.hasWaterVision()) {
            return 600;
        }

        return original;
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isShiftKeyDown()Z", ordinal = 0))
    private boolean dragonSurvival$DisallowCrouchingWhenFlying(boolean original) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if(DragonStateProvider.isDragon(self) && FlightData.getData(self).isWingsSpread()) {
            return false;
        }

        return original;
    }
}
