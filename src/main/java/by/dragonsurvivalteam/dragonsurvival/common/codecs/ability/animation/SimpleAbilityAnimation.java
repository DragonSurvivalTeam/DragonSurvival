package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

public record SimpleAbilityAnimation(String animationKey, AnimationLayer layer, int transitionLength, boolean locksNeck, boolean locksTail) implements AbilityAnimation {
    public static final String CAST_MASS_BUFF = "cast_mass_buff";
    public static final String MASS_BUFF = "mass_buff";

    public static final String CAST_SELF_BUFF = "cast_self_buff";
    public static final String SELF_BUFF = "self_buff";

    public static final Codec<SimpleAbilityAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("animation_key").forGetter(SimpleAbilityAnimation::animationKey),
            Codec.STRING.xmap(AnimationLayer::valueOf, AnimationLayer::name).fieldOf("layer").forGetter(SimpleAbilityAnimation::layer),
            Codec.INT.optionalFieldOf("transition_length", 0).forGetter(SimpleAbilityAnimation::transitionLength),
            Codec.BOOL.fieldOf("locks_neck").forGetter(SimpleAbilityAnimation::locksNeck),
            Codec.BOOL.fieldOf("locks_tail").forGetter(SimpleAbilityAnimation::locksTail)
    ).apply(instance, SimpleAbilityAnimation::new));

    @Override
    public void play(final AnimationState<?> state, final AnimationType animationType) {
        state.getController().transitionLength(transitionLength);
        state.setAndContinue(getRawAnimation(animationType));
    }

    @Override
    public boolean locksHead() {
        return locksNeck;
    }

    @Override
    public boolean locksTail() {
        return locksTail;
    }

    @Override
    public AnimationLayer getLayer() {
        return layer();
    }

    private RawAnimation getRawAnimation(final AnimationType type) {
        RawAnimation rawAnimation = RawAnimation.begin();

        if (type == AnimationType.PLAY_AND_HOLD) {
            rawAnimation = rawAnimation.thenPlayAndHold(animationKey);
        } else if (type == AnimationType.LOOPING) {
            rawAnimation = rawAnimation.thenLoop(animationKey);
        } else if (type == AnimationType.PLAY_ONCE) {
            //noinspection DataFlowIssue -> probably not the same value due to a different animation key
            rawAnimation = rawAnimation.then(animationKey, Animation.LoopType.PLAY_ONCE);
        }

        return rawAnimation;
    }

    @Override
    public String getName() {
        return animationKey;
    }
}
