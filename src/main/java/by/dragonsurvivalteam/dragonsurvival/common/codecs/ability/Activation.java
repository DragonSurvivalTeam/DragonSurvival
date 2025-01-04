package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationType;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.CompoundAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.animation.SyncAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StartTickingSound;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Activation(
        Type type,
        Optional<LevelBasedValue> initialManaCost,
        Optional<ManaCost> continuousManaCost,
        Optional<LevelBasedValue> castTime,
        Optional<LevelBasedValue> cooldown,
        boolean canMoveWhileCasting,
        Optional<Sound> sound,
        Optional<Animations> animations
) {
    private static final Codec<Activation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(Activation::type),
            LevelBasedValue.CODEC.optionalFieldOf("initial_mana_cost").forGetter(Activation::initialManaCost),
            ManaCost.CODEC.optionalFieldOf("continuous_mana_cost").forGetter(Activation::continuousManaCost),
            LevelBasedValue.CODEC.optionalFieldOf("cast_time").forGetter(Activation::castTime),
            LevelBasedValue.CODEC.optionalFieldOf("cooldown").forGetter(Activation::cooldown),
            Codec.BOOL.optionalFieldOf("can_move_while_casting", true).forGetter(Activation::canMoveWhileCasting),
            Sound.CODEC.optionalFieldOf("sound").forGetter(Activation::sound),
            Animations.CODEC.optionalFieldOf("animations").forGetter(Activation::animations)
    ).apply(instance, Activation::new));

    public static Codec<Activation> codec() {
        return CODEC.validate(activation -> {
            switch (activation.type()) {
                case PASSIVE -> {
                    if (activation.castTime().isPresent()) {
                        return DataResult.error(() -> "[cast_time] is not applicable for [" + activation.type() + "] activation");
                    }

                    if (activation.cooldown().isPresent()) {
                        return DataResult.error(() -> "[cooldown] is not applicable for [" + activation.type() + "] activation");
                    }
                }
                case ACTIVE_CHANNELED -> {
                    if (activation.continuousManaCost().isPresent() && activation.continuousManaCost().get().manaCostType() == ManaCost.ManaCostType.RESERVED) {
                        return DataResult.error(() -> "[continuous_mana_cost] -> [reserved] is not applicable for [" + activation.type() + "] activation");
                    }
                }
                case ACTIVE_SIMPLE -> {
                    if (activation.continuousManaCost().isPresent()) {
                        return DataResult.error(() -> "[continuous_mana_cost] is not applicable for [" + activation.type() + "] activation");
                    }

                    if (activation.sound().isPresent() && activation.sound().get().looping().isPresent()) {
                        return DataResult.error(() -> "[sound] -> [looping] is not applicable for [" + activation.type() + "] activation");
                    }

                    if (activation.animations().isPresent() && activation.animations().get().looping().isPresent()) {
                        return DataResult.error(() -> "[animations] -> [looping] is not applicable for [" + activation.type() + "] activation");
                    }
                }
            }

            if (activation.castTime().isEmpty() && activation.sound().isPresent() && activation.sound().get().charging().isPresent()) {
                return DataResult.error(() -> "[sound] -> [charging] is not applicable when no [cast_time] is present");
            }

            if (activation.castTime().isEmpty() && activation.animations().isPresent() && activation.animations().get().startAndCharging().isPresent()) {
                return DataResult.error(() -> "[animations] -> [start_and_charging] is not applicable when no [cast_time] is present");
            }

            return DataResult.success(activation);
        });
    }

    public static Activation simple(final LevelBasedValue initialManaCost, final LevelBasedValue castTime, final LevelBasedValue cooldown, boolean canMoveWhileCasting, final Sound sound, final Animations animations) {
        return new Activation(Type.ACTIVE_SIMPLE, Optional.ofNullable(initialManaCost), Optional.empty(), Optional.ofNullable(castTime), Optional.ofNullable(cooldown), canMoveWhileCasting, Optional.ofNullable(sound), Optional.ofNullable(animations));
    }

    public static Activation passive() {
        return passive(null);
    }

    public static Activation passive(final ManaCost continuousManaCost) {
        return new Activation(Type.PASSIVE, Optional.empty(), Optional.ofNullable(continuousManaCost), Optional.empty(), Optional.empty(), true, Optional.empty(), Optional.empty());
    }

    public enum Type implements StringRepresentable {
        PASSIVE("passive"),
        ACTIVE_CHANNELED("active_channeled"),
        ACTIVE_SIMPLE("active_simple");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;

        Type(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public void playStartAndLoopingSound(final Player dragon, DragonAbilityInstance instance) {
        sound.flatMap(Sound::start).ifPresent(start -> {
            if(dragon.level().isClientSide()) {
                DragonSurvival.PROXY.playSoundAtEyeLevel(dragon, start);
            } else {
                dragon.level().playSound(dragon, dragon.blockPosition(), start, SoundSource.PLAYERS, 1, 1);
            }
        });

        sound.flatMap(Sound::looping).ifPresent(looping -> {
            if (dragon.level().isClientSide()) {
                instance.queueTickingSound(looping, SoundSource.PLAYERS, dragon);
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new StartTickingSound(dragon.getId(), looping, instance.location().withSuffix(dragon.getStringUUID())));
            }
        });
    }

    public void playChargingSound(final Player dragon, DragonAbilityInstance instance) {
        sound.flatMap(Sound::charging).ifPresent(charging -> {
            if (dragon.level().isClientSide()) {
                instance.queueTickingSound(charging, SoundSource.PLAYERS, dragon);
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new StartTickingSound(dragon.getId(), charging, instance.location().withSuffix(dragon.getStringUUID())));
            }
        });
    }

    public void playEndSound(final Player dragon) {
        sound.flatMap(Sound::end).ifPresent(end -> {
            if (dragon.level().isClientSide()) {
                DragonSurvival.PROXY.playSoundAtEyeLevel(dragon, end);
            } else {
                dragon.level().playSound(dragon, dragon.blockPosition(), end, SoundSource.PLAYERS, 1, 1);
            }
        });
    }

    public void playStartAndChargingAnimation(final Player dragon) {
        animations.flatMap(Animations::startAndCharging).ifPresent(startAndCharging -> {
            if(dragon.level().isClientSide()) {
                AbilityAnimation abilityAnimation = startAndCharging.map(
                        simple -> simple,
                        compound -> compound
                );
                DragonSurvival.PROXY.setCurrentAbilityAnimation(dragon.getId(), new Pair<>(abilityAnimation, AnimationType.PLAY_AND_HOLD));
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new SyncAbilityAnimation(dragon.getId(), AnimationType.PLAY_AND_HOLD, startAndCharging));
            }
        });
    }

    public void playLoopingAnimation(final Player dragon) {
        animations.flatMap(Animations::looping).ifPresent(looping -> {
            if(dragon.level().isClientSide()) {
                DragonSurvival.PROXY.setCurrentAbilityAnimation(dragon.getId(), new Pair<>(looping, AnimationType.LOOPING));
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new SyncAbilityAnimation(dragon.getId(), AnimationType.LOOPING, Either.right(looping)));
            }
        });
    }

    public void playEndAnimation(final Player dragon) {
        animations.flatMap(Animations::end).ifPresent(end -> {
            if(dragon.level().isClientSide()) {
                DragonSurvival.PROXY.setCurrentAbilityAnimation(dragon.getId(), new Pair<>(end, AnimationType.PLAY_ONCE));
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new SyncAbilityAnimation(dragon.getId(), AnimationType.PLAY_ONCE, Either.right(end)));
            }
        });
    }

    /** Sound effects for the ability
     *
     * @param start Sound effect that plays when the ability finishes charging
     * @param charging Sound effect that loops while the ability is being charged
     * @param looping Sound effect that loops while the ability is active
     * @param end Sound effect that plays when the ability ends
     */
    public record Sound(Optional<SoundEvent> start, Optional<SoundEvent> charging, Optional<SoundEvent> looping, Optional<SoundEvent> end) {
        public static final Codec<Sound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("start").forGetter(Sound::start),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("charging").forGetter(Sound::charging),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("looping").forGetter(Sound::looping),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("end").forGetter(Sound::end)
        ).apply(instance, Sound::new));

        public static Optional<Sound> of(final SoundEvent start, final SoundEvent charging, final SoundEvent looping, final SoundEvent end) {
            return Optional.of(new Sound(Optional.ofNullable(start), Optional.ofNullable(charging), Optional.ofNullable(looping), Optional.ofNullable(end)));
        }
    }

    /** Animations for the ability
     *
     * @param startAndCharging Animations that play when casting the ability; can be a compound animation of a starting animation that leads into a looping animation after
     * @param looping Animation that loops while the ability is active
     * @param end Animation that plays when the ability ends (this is also useful for instant abilities, e.g. mouth opening to shoot out a fireball)
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
    }

    // TODO :: move 'ManaCost' here? it probably won't be used by anything else
}