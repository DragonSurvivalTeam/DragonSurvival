package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Increase brightness when water vision is active */
// FIXME
//@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
//    @ModifyExpressionValue(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 1))
//    private boolean dragonSurvival$handleWaterVision(boolean original) {
//        return original || VisionHandler.hasWaterVision();
//    }
}
