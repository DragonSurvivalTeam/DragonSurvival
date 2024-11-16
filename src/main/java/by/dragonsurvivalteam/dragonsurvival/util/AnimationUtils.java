package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.client.Minecraft;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;

public class AnimationUtils {
    private static final float MS_FOR_60FPS = 1.f / 60.f * 1000.f;
    private static final float MS_PER_TICK = 1000f / 20f;

    // FIXME: This is a hack since GeckoLib's state.isCurrentAnimation() doesn't work. If they ever fix that, we can remove this.
    public static boolean isAnimationPlaying(AnimationController<?> controller, String animationName) {
        return controller.getCurrentAnimation() != null && controller.getCurrentAnimation().animationName.equals(animationName);
    }

    public static <E extends IAnimatable> void addAnimation(AnimationBuilder builder, String animationName, ILoopType loopType, float transitionLength, AnimationController<E> controller){
        builder.addAnimation(animationName, loopType);
        controller.transitionLengthTicks = transitionLength;
    }

    public static <E extends IAnimatable> void setAnimationSpeed(double speed, double currentAnimationTick, AnimationController<E> controller) {

        if(speed == controller.animationSpeed) {
            return;
        }

        if(controller.getCurrentAnimation() != null) {
            double distance = currentAnimationTick - controller.tickOffset;
            controller.tickOffset = currentAnimationTick - distance * (controller.animationSpeed / speed);
            controller.animationSpeed = speed;
        }
    }

    public static float getDeltaTickFor60FPS() {
        float deltaTick = Minecraft.getInstance().getDeltaFrameTime();
        return deltaTick / (MS_FOR_60FPS / MS_PER_TICK);
    }

    public static float getRealtimeDeltaTicks() {
        return Minecraft.getInstance().getDeltaFrameTime();
    }
}
