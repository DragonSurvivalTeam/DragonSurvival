package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorAnimationController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class AnimationUtils {
    private static final float MS_FOR_60FPS = 1.f / 60.f * 1000.f;
    private static final float MS_PER_TICK = 1000f / 20f;

    public static RawAnimation createAnimation(@Nullable final RawAnimation builder, @NotNull final RawAnimation staticAnimation) {
        if (builder == null) {
            return staticAnimation;
        }

        assert staticAnimation.getAnimationStages().size() == 1;
        RawAnimation.Stage stage = staticAnimation.getAnimationStages().get(0);
        builder.then(stage.animationName(), stage.loopType());

        return builder;
    }

    public static <E extends GeoAnimatable> void setAnimationSpeed(double speed, double currentAnimationTick, AnimationController<E> controller) {

        if(speed == controller.getAnimationSpeed()) {
            return;
        }

        if(controller.getCurrentAnimation() != null) {
            double distance = currentAnimationTick - ((AccessorAnimationController)controller).dragonSurvival$getTickOffset();
            ((AccessorAnimationController) controller).dragonSurvival$setTickOffset(currentAnimationTick - distance * (controller.getAnimationSpeed() / speed));
            controller.setAnimationSpeed(speed);
        }
    }

    // TODO: This is a hack since GeckoLib's state.isCurrentAnimation() doesn't work. If they ever fix that, we can remove this.
    public static boolean isAnimationPlaying(AnimationController<?> controller, RawAnimation animation) {
        String animationName = animation.getAnimationStages().get(0).animationName();
        return controller.getCurrentAnimation() != null && controller.getCurrentAnimation().animation().name().equals(animationName);
    }

    public static double getMovementSpeed(LivingEntity of) {
        return Math.sqrt(Math.pow(of.getX() - of.xo, 2) + Math.pow(of.getZ() - of.zo, 2));
    }

    public static float getDeltaTickFor60FPS() {
        float deltaTick = Minecraft.getInstance().getDeltaFrameTime();
        return deltaTick / (MS_FOR_60FPS / MS_PER_TICK);
    }

    public static float getRealtimeDeltaTicks() {
        return Minecraft.getInstance().getDeltaFrameTime();
    }
}
