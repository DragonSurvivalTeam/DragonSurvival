package by.dragonsurvivalteam.dragonsurvival.util;

import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.function.Function;

/** Does not contain the dynamic emote- / continuous animations */
public enum DragonAnimations {
    BITE("bite", name -> RawAnimation.begin().thenLoop(name)),
    USE_ITEM_RIGHT("use_item_right", name -> RawAnimation.begin().thenLoop(name)),
    USE_ITEM_LEFT("use_item_left", name -> RawAnimation.begin().thenLoop(name)),
    EAT_ITEM_RIGHT("eat_item_right", name -> RawAnimation.begin().thenLoop(name)),
    EAT_ITEM_LEFT("eat_item_left", name -> RawAnimation.begin().thenLoop(name)),

    SIT_ON_MAGIC_SOURCE("sit_on_magic_source", name -> RawAnimation.begin().thenLoop(name)),
    SLEEP("sleep_left", name -> RawAnimation.begin().thenLoop(name)),
    SIT("sit", name -> RawAnimation.begin().thenLoop(name)),
    FLY("fly", name -> RawAnimation.begin().thenLoop(name)),
    FLY_SOARING("fly_soaring", name -> RawAnimation.begin().thenLoop(name)),
    FLY_DIVE("fly_dive", name -> RawAnimation.begin().thenLoop(name)),
    FLY_DIVE_ALT("fly_dive_alt", name -> RawAnimation.begin().thenLoop(name)),
    FLY_SPIN("fly_spin", name -> RawAnimation.begin().thenLoop(name)),
    FLY_LAND("fly_land", name -> RawAnimation.begin().thenLoop(name)),
    SWIM("swim", name -> RawAnimation.begin().thenLoop(name)),
    SWIM_FAST("swim_fast", name -> RawAnimation.begin().thenLoop(name)),
    FALL_LOOP("fall_loop", name -> RawAnimation.begin().thenLoop(name)),
    SNEAK("sneak", name -> RawAnimation.begin().thenLoop(name)),
    SNEAK_WALK("sneak_walk", name -> RawAnimation.begin().thenLoop(name)),
    DIG_SNEAK("dig_sneak", name -> RawAnimation.begin().thenLoop(name)),
    RUN("run", name -> RawAnimation.begin().thenLoop(name)),
    WALK("walk", name -> RawAnimation.begin().thenLoop(name)),
    IDLE("idle", name -> RawAnimation.begin().thenLoop(name)),
    DIG("dig", name -> RawAnimation.begin().thenLoop(name)),
    CLIMBING_UP("climbing_up", name -> RawAnimation.begin().thenLoop(name)),
    CLIMBING_DOWN("climbing_down", name -> RawAnimation.begin().thenLoop(name)),

    JUMP("jump", name -> RawAnimation.begin().then(name, Animation.LoopType.PLAY_ONCE)),
    FLY_LAND_END("fly_land_end", name -> RawAnimation.begin().then(name, Animation.LoopType.PLAY_ONCE)),

    // Special create animation
    CREATE_SKYHOOK_RIDING("create_skyhook_riding", name -> RawAnimation.begin().thenLoop(name));

    private final String animationName;
    private final RawAnimation animation;

    DragonAnimations(final String animationName, final Function<String, RawAnimation> animation) {
        this.animationName = animationName;
        this.animation = animation.apply(animationName);
    }

    public String getAnimationName() {
        return animationName;
    }

    public RawAnimation getAnimation() {
        return animation;
    }
}
