package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.AnimationControllerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;

public class AnimationUtils {
    /** Time in MS of 1 frame for 60 FPS */
    private static final float MS_FOR_60FPS = 1.f / 60.f * 1000.f;

    public static <E extends GeoAnimatable> void setAnimationSpeed(double speed, double currentAnimationTick, AnimationController<E> controller) {

        if (speed == controller.getAnimationSpeed()) {
            return;
        }

        if (controller.getCurrentAnimation() != null) {
            double distance = currentAnimationTick - ((AnimationControllerAccessor) controller).dragonSurvival$getTickOffset();
            ((AnimationControllerAccessor) controller).dragonSurvival$setTickOffset(currentAnimationTick - distance * (controller.getAnimationSpeed() / speed));
            controller.setAnimationSpeed(speed);
        }
    }

    // TODO: This is a hack since GeckoLib's state.isCurrentAnimation() doesn't work. If they ever fix that, we can remove this.
    public static boolean isAnimationPlaying(AnimationController<?> controller, RawAnimation animation) {
        String animationName = animation.getAnimationStages().getFirst().animationName();
        return controller.getCurrentAnimation() != null && controller.getCurrentAnimation().animation().name().equals(animationName);
    }

    public static double getMovementSpeed(LivingEntity of) {
        return Math.sqrt(Math.pow(of.getX() - of.xo, 2) + Math.pow(of.getZ() - of.zo, 2));
    }

    public static float getDeltaTickFor60FPS() {
        float deltaTick = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
        //noinspection DataFlowIssue -> level is present
        return deltaTick / (MS_FOR_60FPS / Minecraft.getInstance().level.tickRateManager().millisecondsPerTick());
    }

    public static float getDeltaSeconds() {
        //noinspection DataFlowIssue -> level is present
        return (Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() * Minecraft.getInstance().level.tickRateManager().millisecondsPerTick()) / 1000f;
    }

    public static boolean doesAnimationExist(final Player player, final String animation) {
        BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(DragonModel.getAnimationResource(player));
        if (bakedAnimations == null) {
            return false;
        }

        return bakedAnimations.getAnimation(animation) != null;
    }

    public static boolean doesAnimationExist(final Player player, final RawAnimation animation) {
        assert (animation.getAnimationStages().size() == 1);

        return doesAnimationExist(player, animation.getAnimationStages().getFirst().animationName());
    }

    public static double animationDuration(final Player player, final String animation) {
        if (!doesAnimationExist(player, animation)) {
            return 0;
        }

        return GeckoLibCache.getBakedAnimations().get(DragonModel.getAnimationResource(player)).getAnimation(animation).length();
    }

    public static double animationDuration(final Player player, final RawAnimation animation)
    {
        assert (animation.getAnimationStages().size() == 1);

        return animationDuration(player, animation.getAnimationStages().getFirst().animationName());
    }
}
