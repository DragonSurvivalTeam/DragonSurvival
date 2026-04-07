package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.AnimationControllerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.cache.GeckoLibResources;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.model.GeoModel;
import net.minecraft.world.level.Level;


public class AnimationUtils {
    /** Time in MS of 1 frame for 60 FPS */
    private static final float MS_FOR_60FPS = 1.f / 60.f * 1000.f;

    public static <E extends GeoAnimatable> void setAnimationSpeed(double speed, double currentAnimationTick, AnimationController<E> controller) {

        if (speed == controller.getAnimationSpeed()) {
            return;
        }

        // FIXME
//        if (controller.getCurrentRawAnimation() != null) {
//            double distance = currentAnimationTick - ((AnimationControllerAccessor) controller).dragonSurvival$getTickOffset();
//            ((AnimationControllerAccessor) controller).dragonSurvival$setTickOffset(currentAnimationTick - distance * (controller.getAnimationSpeed() / speed));
//            controller.setAnimationSpeed(speed);
//        }
    }

    // TODO: This is a hack since GeckoLib's state.isCurrentAnimation() doesn't work. If they ever fix that, we can remove this.
    public static boolean isAnimationPlaying(AnimationController<?> controller, RawAnimation animation) {
        String animationName = animation.getAnimationStages().getFirst().animationName();
        RawAnimation currentRawAnimation = controller.getCurrentRawAnimation();
        if (currentRawAnimation == null) return false;
        assert (currentRawAnimation.getAnimationStages().size() == 1);

        return currentRawAnimation.getAnimationStages().getFirst().animationName().equals(animationName);
    }

    public static double getMovementSpeed(LivingEntity of) {
        return Math.sqrt(Math.pow(of.getX() - of.xo, 2) + Math.pow(of.getZ() - of.zo, 2));
    }

    public static float getDeltaTickFor60FPS() {
        float deltaTick = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();

        Level level = Minecraft.getInstance().level;
        if (level != null)
        {
            return deltaTick / (MS_FOR_60FPS / level.tickRateManager().millisecondsPerTick());
        }

        return 0;
    }

    public static float getDeltaSeconds() {
        //noinspection DataFlowIssue -> level is present
        Level level = Minecraft.getInstance().level;
        if (level != null)
        {
            return (Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * level.tickRateManager().millisecondsPerTick()) / 1000f;
        }

        return 0;
    }

    public static <A extends GeoAnimatable, T extends GeoModel<A>> boolean doesAnimationExist(final T model, final A animatable, final String animation) {
        BakedAnimations bakedAnimations = GeckoLibResources.getBakedAnimations().cache().get(model.getAnimationResource(animatable));

        if (bakedAnimations == null) {
            return false;
        }

        return bakedAnimations.getAnimation(animation) != null;
    }

    public static <A extends GeoAnimatable, T extends GeoModel<A>> boolean doesAnimationExist(final T model, final A animatable, final RawAnimation animation) {
        assert (animation.getAnimationStages().size() == 1);

        return doesAnimationExist(model, animatable, animation.getAnimationStages().getFirst().animationName());
    }

    public static <A extends GeoAnimatable, T extends GeoModel<A>> double animationDuration(final T model, final A animatable, final String animation) {
        if (!doesAnimationExist(model, animatable, animation)) {
            return 0;
        }

        return GeckoLibResources.getBakedAnimations().cache().get(model.getAnimationResource(animatable)).getAnimation(animation).length();
    }
}
