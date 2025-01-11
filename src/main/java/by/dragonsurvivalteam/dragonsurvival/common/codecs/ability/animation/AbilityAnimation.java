package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation;

import software.bernie.geckolib.animation.AnimationState;

public interface AbilityAnimation {
    void play(AnimationState<?> state, AnimationType animationType);
    boolean locksHead();
    boolean locksTail();
    AnimationLayer getLayer();
    String getName();
}
