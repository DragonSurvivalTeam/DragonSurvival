package by.dragonsurvivalteam.dragonsurvival.client.render.util;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber
public class AnimationTimer {
    public static CopyOnWriteArrayList<AnimationTimer> timers = new CopyOnWriteArrayList<>();
    protected ConcurrentHashMap<String, Double> animationTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onTick(final ClientTickEvent.Post event) {
        for (AnimationTimer timer : timers) {
            timer.animationTimes.keySet().forEach(key -> {
                timer.animationTimes.computeIfPresent(key, (animation, tick) -> tick - 1);

                if (timer.animationTimes.get(key) <= 0) {
                    timer.animationTimes.remove(key);
                }
            });

            if (timer.animationTimes.isEmpty()) {
                timers.remove(timer);
            }
        }
    }

    public double getDuration(final String animation) {
        return animationTimes.getOrDefault(animation, 0.0);
    }

    /**
     * The RawAnimation must contain only one stage for this to work correctly
     */
    public double getDuration(final RawAnimation animation) {
        return getDuration(animation.getAnimationStages().getFirst().animationName());
    }

    public void putAnimation(final String animation, final Double ticks) {
        putDuration(animation, ticks);

        if (!timers.contains(this)) {
            timers.add(this);
        }
    }

    /**
     * The RawAnimation must contain only one stage for this to work correctly
     */
    public void putAnimation(final RawAnimation animation, final Double ticks) {
        putDuration(animation.getAnimationStages().getFirst().animationName(), ticks);

        if (!timers.contains(this)) {
            timers.add(this);
        }
    }

    protected void putDuration(final String animation, final Double ticks) {
        animationTimes.put(animation, ticks);
    }
}