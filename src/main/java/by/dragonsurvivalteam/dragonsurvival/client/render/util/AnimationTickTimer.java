package by.dragonsurvivalteam.dragonsurvival.client.render.util;

import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.model.GeoModel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(Dist.CLIENT)
public class AnimationTickTimer {
    public static final CopyOnWriteArrayList<AnimationTickTimer> TIMERS = new CopyOnWriteArrayList<>();
    protected final ConcurrentHashMap<String, Double> animationTimes = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onTick(final RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        float deltaTicks = Minecraft.getInstance().getDeltaFrameTime();

        for (AnimationTickTimer timer : TIMERS) {
            timer.animationTimes.keySet().forEach(key -> {
                timer.animationTimes.computeIfPresent(key, (animation, tick) -> tick - deltaTicks);

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

        return animationTimes.containsKey(animation.getAnimationStages().get(0).animationName());
    }

    public double getDuration(final String animation) {
        return animationTimes.getOrDefault(animation, 0d);
    }

    public double getDuration(final RawAnimation animation) {
        assert(animation.getAnimationStages().size() == 1);

        return getDuration(animation.getAnimationStages().get(0).animationName());
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

        putAnimation(model, animatable, animation.getAnimationStages().get(0).animationName());
    }
}
