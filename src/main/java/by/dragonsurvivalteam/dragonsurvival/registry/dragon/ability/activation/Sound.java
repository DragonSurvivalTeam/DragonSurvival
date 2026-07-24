package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;

/**
 * Sound effects for the ability
 *
 * @param start    Sound effect that plays when the ability finishes charging
 * @param charging Sound effect that loops while the ability is being charged
 * @param looping  Sound effect that loops while the ability is active
 * @param end      Sound effect that plays when the ability ends
 */
public record Sound(Optional<SoundEvent> start, Optional<SoundEvent> charging, Optional<SoundEvent> looping,
                    Optional<SoundEvent> end) {
    public static final Codec<Sound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("start").forGetter(Sound::start),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("charging").forGetter(Sound::charging),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("looping").forGetter(Sound::looping),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("end").forGetter(Sound::end)
    ).apply(instance, Sound::new));

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private SoundEvent start;
        private SoundEvent charging;
        private SoundEvent looping;
        private SoundEvent end;

        public Builder start(final SoundEvent start) {
            this.start = start;
            return this;
        }

        public Builder charging(final SoundEvent charging) {
            this.charging = charging;
            return this;
        }

        public Builder looping(final SoundEvent looping) {
            this.looping = looping;
            return this;
        }

        public Builder end(final SoundEvent end) {
            this.end = end;
            return this;
        }

        public Sound build() {
            return new Sound(Optional.ofNullable(start), Optional.ofNullable(charging), Optional.ofNullable(looping), Optional.ofNullable(end));
        }

        public Optional<Sound> optional() {
            return Optional.of(build());
        }
    }
}