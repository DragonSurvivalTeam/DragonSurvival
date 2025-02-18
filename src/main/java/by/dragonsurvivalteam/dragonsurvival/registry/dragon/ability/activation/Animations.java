package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.CompoundAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * Animations for the ability
 *
 * @param startAndCharging Animations that play when casting the ability; can be a compound animation of a starting animation that leads into a looping animation after
 * @param looping          Animation that loops while the ability is active
 * @param end              Animation that plays when the ability ends (this is also useful for instant abilities, e.g. mouth opening to shoot out a fireball)
 */
public record Animations(
        Optional<Either<CompoundAbilityAnimation, SimpleAbilityAnimation>> startAndCharging,
        Optional<SimpleAbilityAnimation> looping,
        Optional<SimpleAbilityAnimation> end
) {
    public static final Codec<Animations> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(CompoundAbilityAnimation.CODEC, SimpleAbilityAnimation.CODEC).optionalFieldOf("start_and_charging").forGetter(Animations::startAndCharging),
            SimpleAbilityAnimation.CODEC.optionalFieldOf("looping").forGetter(Animations::looping),
            SimpleAbilityAnimation.CODEC.optionalFieldOf("end").forGetter(Animations::end)
    ).apply(instance, Animations::new));

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private Either<CompoundAbilityAnimation, SimpleAbilityAnimation> startAndCharging;
        private SimpleAbilityAnimation looping;
        private SimpleAbilityAnimation end;

        public Builder startAndCharging(final CompoundAbilityAnimation startAndCharging) {
            this.startAndCharging = Either.left(startAndCharging);
            return this;
        }

        public Builder startAndCharging(final SimpleAbilityAnimation startAndCharging) {
            this.startAndCharging = Either.right(startAndCharging);
            return this;
        }

        public Builder looping(final SimpleAbilityAnimation looping) {
            this.looping = looping;
            return this;
        }

        public Builder end(final SimpleAbilityAnimation end) {
            this.end = end;
            return this;
        }

        public Animations build() {
            return new Animations(Optional.ofNullable(startAndCharging), Optional.ofNullable(looping), Optional.ofNullable(end));
        }

        public Optional<Animations> optional() {
            return Optional.of(build());
        }
    }
}