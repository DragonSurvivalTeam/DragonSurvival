package by.dragonsurvivalteam.dragonsurvival.client.render.util;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(Dist.CLIENT)
public class AnimationTickTimer {
    public static final CopyOnWriteArrayList<AnimationTickTimer> TIMERS = new CopyOnWriteArrayList<>();
    protected final ConcurrentHashMap<String, Double> animationTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onTick(final ClientTickEvent.Pre event) {
        for (AnimationTickTimer timer : TIMERS) {
            timer.animationTimes.keySet().forEach(key -> {
                timer.animationTimes.computeIfPresent(key, (animation, tick) -> tick - 1);

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

    public double getDuration(final String animation) {
        return animationTimes.getOrDefault(animation, 0d);
    }

    /**
     * The RawAnimation must contain only one stage for this to work correctly
     */
    public double getDuration(final RawAnimation animation) {
        return getDuration(animation.getAnimationStages().getFirst().animationName());
    }

    public void putAnimation(final String animation, final Double ticks) {
        putDuration(animation, ticks);

        if (!TIMERS.contains(this)) {
            TIMERS.add(this);
        }
    }

    /**
     * The RawAnimation must contain only one stage for this to work correctly
     */
    public void putAnimation(final RawAnimation animation, final Double ticks) {
        putDuration(animation.getAnimationStages().getFirst().animationName(), ticks);

        if (!TIMERS.contains(this)) {
            TIMERS.add(this);
        }
    }

    protected void putDuration(final String animation, final Double ticks) {
        animationTimes.put(animation, ticks);
    }
}