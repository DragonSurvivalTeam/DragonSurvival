package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationPoint;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimationController.class)
public interface AnimationControllerAccessor {
    @Dynamic
    @Accessor("timelineTime")
    double dragonSurvival$getTimelineTime();

    @Dynamic
    @Accessor("transitionFromPoint")
    AnimationPoint dragonSurvival$getTransitionFromPoint();

    @Dynamic
    @Accessor("animationPoint")
    AnimationPoint dragonSurvival$getAnimationPoint();
}
