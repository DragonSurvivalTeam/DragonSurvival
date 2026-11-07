package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Mixin(value = GeoModel.class, remap = false)
public abstract class GeoModelMixin {
    @Shadow private double animTime;
    @Shadow private double lastGameTickTime;

    // The DragonModel GeoModel is shared between many different entities,
    // which means the timer can be shared between many different entities.
    //
    // Use the timer from the AnimatableManager instead, which is unique per entity.
    // This Mixin fixes some buggy behavior involving pausing the game whilst other dragons are rendering on screen.
    @Inject(method = "handleAnimations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isPaused()Z", remap = true), require = 1)
    private void dragonSurvival$restoreAnimationClock(final GeoAnimatable animatable, final long instanceId, final AnimationState<?> animationState, final CallbackInfo callback) {
        if (!(animatable instanceof DragonEntity)) {
            return;
        }

        // Each AnimatableManager stores its own lastUpdateTime.
        AnimatableManager<?> manager = animatable.getAnimatableInstanceCache().getManagerForId(instanceId);

        // If it is the firstTick, we need something in lastUpdateTime. So just put firstTickTime in there.
        if (manager.isFirstTick()) {
            manager.updatedAt(manager.getFirstTickTime());
        }

        // Use the animatableManager time instead of what is used in the GeoModel.
        animTime = manager.getLastUpdateTime();
        lastGameTickTime = animTime;
    }
}
