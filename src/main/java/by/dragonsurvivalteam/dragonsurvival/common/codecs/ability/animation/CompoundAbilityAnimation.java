package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * A compound ability animation is for when the user wants to play a starting animation that leads into a looping animation after.
 * In practice, this would be used if you want an initial cast animation that leads into a looping charge animation before an ability is cast.
 */
public record CompoundAbilityAnimation(String startingAnimationKey, String loopingAnimationKey, AnimationLayer layer, int transitionLength, boolean locksNeck,
                                       boolean locksTail) implements AbilityAnimation {

    public static final Codec<CompoundAbilityAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("starting_animation_key").forGetter(CompoundAbilityAnimation::startingAnimationKey),
            Codec.STRING.fieldOf("looping_animation_key").forGetter(CompoundAbilityAnimation::loopingAnimationKey),
            Codec.STRING.xmap(AnimationLayer::valueOf, AnimationLayer::name).fieldOf("layer").forGetter(CompoundAbilityAnimation::layer),
            Codec.INT.optionalFieldOf("transition_length", 0).forGetter(CompoundAbilityAnimation::transitionLength),
            Codec.BOOL.fieldOf("locks_neck").forGetter(CompoundAbilityAnimation::locksNeck),
            Codec.BOOL.fieldOf("locks_tail").forGetter(CompoundAbilityAnimation::locksTail)
    ).apply(instance, CompoundAbilityAnimation::new));

    @Override
    public void play(AnimationState<?> state, AnimationType animationType) {
        state.getController().transitionLength(transitionLength);
        state.setAndContinue(getRawAnimation());
    }

    @Override
    public boolean locksHead() {
        return locksNeck;
    }

    @Override
    public AnimationLayer getLayer() {
        return layer();
    }

    private RawAnimation getRawAnimation() {
        return RawAnimation.begin().thenPlay(startingAnimationKey).thenLoop(loopingAnimationKey);
    }

    @Override
    public String getName() {
        return loopingAnimationKey;
    }
}
