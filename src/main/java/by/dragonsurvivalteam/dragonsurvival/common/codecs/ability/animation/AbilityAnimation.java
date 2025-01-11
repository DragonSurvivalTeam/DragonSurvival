package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation;

import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import software.bernie.geckolib.animation.AnimationState;

public interface AbilityAnimation {
    void play(AnimationState<?> state, AnimationType animationType);
    boolean locksHead();
    boolean locksTail();
    AnimationLayer getLayer();
    String getName();
}
