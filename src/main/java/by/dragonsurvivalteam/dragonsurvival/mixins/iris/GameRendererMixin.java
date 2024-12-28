package by.dragonsurvivalteam.dragonsurvival.mixins.iris;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Inject at HEAD because Oculus will willingly run into a NullPointerException in case the entity does not have night vision */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getNightVisionScale", at = @At(value = "HEAD"), cancellable = true)
    private static void modifyNightVisionScale(final LivingEntity entity, float nanoTime, final CallbackInfoReturnable<Float> callback) {
        if (entity.isUnderWater() && VisionHandler.hasWaterVision()) {
            callback.setReturnValue(1f);
        }
    }
}