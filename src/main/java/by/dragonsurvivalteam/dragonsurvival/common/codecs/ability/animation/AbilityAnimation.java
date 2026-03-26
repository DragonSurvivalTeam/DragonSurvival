package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation;

import com.geckolib.animation.state.AnimationTest;

public interface AbilityAnimation {
    void play(AnimationTest<?> state, AnimationType animationType);

    boolean locksHead();

    boolean locksTail();

    AnimationLayer getLayer();

    String getName();
}
