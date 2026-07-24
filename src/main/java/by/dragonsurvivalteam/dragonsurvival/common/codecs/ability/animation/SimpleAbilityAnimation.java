package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;

public record SimpleAbilityAnimation(String animationKey, AnimationLayer layer, int transitionLength, boolean locksNeck, boolean locksTail) implements AbilityAnimation {
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

    public static Builder create(final AnimationKey key, final AnimationLayer layer) {
        return new Builder(key.getName(), layer);
    }

    @Override
    public String getName() {
        return animationKey;
    }

    public static class Builder {
        private final String animationKey;
        private final AnimationLayer layer;
        private int transitionLength = 0; // optionalFieldOf default value
        private boolean locksNeck = false;
        private boolean locksTail = false;

        public Builder(final String animationKey, final AnimationLayer layer) {
            this.animationKey = animationKey;
            this.layer = layer;
        }

        public Builder transitionLength(final int transitionLength) {
            this.transitionLength = transitionLength;
            return this;
        }

        public Builder locksNeck() {
            this.locksNeck = true;
            return this;
        }

        public Builder locksTail() {
            this.locksTail = true;
            return this;
        }

        public SimpleAbilityAnimation build() {
            return new SimpleAbilityAnimation(animationKey, layer, transitionLength, locksNeck, locksTail);
        }
    }
}
