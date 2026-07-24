package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.animation.AnimationController;

@Mixin(AnimationController.class)
public interface AnimationControllerAccessor {
    @Dynamic
    @Accessor("tickOffset")
    void dragonSurvival$setTickOffset(double tickOffset);

    @Dynamic
    @Accessor("tickOffset")
    double dragonSurvival$getTickOffset();
}
