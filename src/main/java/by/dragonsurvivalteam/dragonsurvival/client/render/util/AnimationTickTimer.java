package by.dragonsurvivalteam.dragonsurvival.client.render.util;

import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.model.GeoModel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(Dist.CLIENT)
public class AnimationTickTimer {
    public static final CopyOnWriteArrayList<AnimationTickTimer> TIMERS = new CopyOnWriteArrayList<>();
    protected final ConcurrentHashMap<String, Double> animationTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onTick(final RenderFrameEvent.Pre event) {
        for (AnimationTickTimer timer : TIMERS) {
            timer.animationTimes.keySet().forEach(key -> {
                timer.animationTimes.computeIfPresent(key, (animation, tick) -> tick - event.getPartialTick().getRealtimeDeltaTicks());

                if (timer.animationTimes.get(key) <= 0) {
                    timer.animationTimes.remove(key);
                }
            });

            if (timer.animationTimes.isEmpty()) {
                TIMERS.remove(timer);
            }
        }
    }

    public boolean isPresent(final String animation) {
        return animationTimes.containsKey(animation);
    }

    public boolean isPresent(final RawAnimation animation) {
        assert(animation.getAnimationStages().size() == 1);

        return animationTimes.containsKey(animation.getAnimationStages().getFirst().animationName());
    }

    public double getDuration(final String animation) {
        return animationTimes.getOrDefault(animation, 0d);
    }

    public double getDuration(final RawAnimation animation) {
        assert(animation.getAnimationStages().size() == 1);

        return getDuration(animation.getAnimationStages().getFirst().animationName());
    }

    // Needed specifically for keeping track of emote timings, which don't actually directly reference their animation names
    public void putAnimation(final String animation, final Double ticks) {
        animationTimes.put(animation, ticks);

        if (!TIMERS.contains(this)) {
            TIMERS.add(this);
        }
    }

    public void stopAnimation(final String animation) {
        animationTimes.remove(animation);
    }

    public <A extends GeoAnimatable, T extends GeoModel<A>> void putAnimation(final T model, final A animatable, final String animation) {
        animationTimes.put(animation, AnimationUtils.animationDuration(model, animatable, animation));

        if (!TIMERS.contains(this)) {
            TIMERS.add(this);
        }
    }

    public <A extends GeoAnimatable, T extends GeoModel<A>> void putAnimation(final T model, final A animatable, final RawAnimation animation) {
        assert (animation.getAnimationStages().size() == 1);

        putAnimation(model, animatable, animation.getAnimationStages().getFirst().animationName());
    }
}