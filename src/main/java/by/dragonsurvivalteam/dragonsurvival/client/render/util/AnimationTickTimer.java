package by.dragonsurvivalteam.dragonsurvival.client.render.util;

import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.model.GeoModel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;

public class AnimationTickTimer {
    // Needed to keep track of the true start/stop time for the animation, so that
    // it plays correctly when tick warping/during a replay
    private record AnimationTime(double start, double end) {}

    private final ConcurrentHashMap<String, AnimationTime> animationTimes = new ConcurrentHashMap<>();
    private final DoubleSupplier clock;

    public AnimationTickTimer(final DoubleSupplier clock) {
        this.clock = clock;
    }

    public boolean isPresent(final String animation) {
        return getDuration(animation) > 0;
    }

    public boolean isPresent(final RawAnimation animation) {
        assert(animation.getAnimationStages().size() == 1);

        return isPresent(animation.getAnimationStages().getFirst().animationName());
    }

    public double getDuration(final String animation) {
        AnimationTime time = animationTimes.get(animation);
        if (time == null) {
            return 0;
        }
        double now = clock.getAsDouble();
        if (!Double.isFinite(now) || now < time.start()) {
            return 0;
        }

        return Math.max(0, time.end() - now);
    }

    public double getDuration(final RawAnimation animation) {
        assert(animation.getAnimationStages().size() == 1);

        return getDuration(animation.getAnimationStages().getFirst().animationName());
    }

    // Needed specifically for keeping track of emote timings, which don't actually directly reference their animation names
    public void putAnimation(final String animation, final Double ticks) {
        if (Double.isFinite(ticks) && ticks > 0) {
            double start = clock.getAsDouble();
            animationTimes.put(animation, new AnimationTime(start, start + ticks));
        } else {
            stopAnimation(animation);
        }
    }

    public void stopAnimation(final String animation) {
        animationTimes.remove(animation);
    }

    public <A extends GeoAnimatable, T extends GeoModel<A>> void putAnimation(final T model, final A animatable, final String animation) {
        putAnimation(animation, AnimationUtils.animationDuration(model, animatable, animation));
    }

    public <A extends GeoAnimatable, T extends GeoModel<A>> void putAnimation(final T model, final A animatable, final RawAnimation animation) {
        assert (animation.getAnimationStages().size() == 1);

        putAnimation(model, animatable, animation.getAnimationStages().getFirst().animationName());
    }
}
