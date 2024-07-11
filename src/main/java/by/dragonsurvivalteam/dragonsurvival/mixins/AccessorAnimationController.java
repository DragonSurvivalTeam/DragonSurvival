package by.dragonsurvivalteam.dragonsurvival.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.core.animation.AnimationController;

@Mixin(value = AnimationController.class, remap = false)
public interface AccessorAnimationController {
    @Accessor(value = "tickOffset")
    void dragonSurvival$setTickOffset(double tickOffset);

    @Accessor(value = "tickOffset")
    double dragonSurvival$getTickOffset();
}
