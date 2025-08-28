package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationType;
import by.dragonsurvivalteam.dragonsurvival.network.animation.SyncAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StartTickingSound;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber
public interface Activation {
    ResourceKey<Registry<MapCodec<? extends Activation>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("activation"));
    Registry<MapCodec<? extends Activation>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<Activation> CODEC = REGISTRY.byNameCodec().dispatch("activation_type", Activation::codec, Function.identity());

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("passive"), () -> PassiveActivation.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("simple"), () -> SimpleActivation.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("channeled"), () -> ChanneledActivation.CODEC);
        }
    }

    Type type();

    MapCodec<? extends Activation> codec();

    default float getInitialManaCost(int level) {
        return 0;
    }

    default Optional<ManaCost> continuousManaCost() {
        return Optional.empty();
    }

    default int getCastTime(int level) {
        return 0;
    }

    default int getCooldown(int level) {
        return 0;
    }

    default Notification notification() {
        return Notification.NONE;
    }

    default boolean canMoveWhileCasting() {
        return true;
    }

    default Optional<Sound> sound() {
        return Optional.empty();
    }

    default Optional<Animations> animations() {
        return Optional.empty();
    }

    default void playStartAndLoopingSound(final Player dragon, DragonAbilityInstance instance) {
        sound().flatMap(Sound::start).ifPresent(start -> {
            if (dragon.level().isClientSide()) {
                DragonSurvival.PROXY.playSoundAtEyeLevel(dragon, start);
            } else {
                dragon.level().playSound(dragon, dragon.blockPosition(), start, SoundSource.PLAYERS, 1, 1);
            }
        });

        sound().flatMap(Sound::looping).ifPresent(looping -> {
            if (dragon.level().isClientSide()) {
                instance.queueTickingSound(looping, SoundSource.PLAYERS, dragon);
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new StartTickingSound(dragon.getId(), looping, instance.location().withSuffix(dragon.getStringUUID())));
            }
        });
    }

    default void playChargingSound(final Player dragon, DragonAbilityInstance instance) {
        sound().flatMap(Sound::charging).ifPresent(charging -> {
            if (dragon.level().isClientSide()) {
                instance.queueTickingSound(charging, SoundSource.PLAYERS, dragon);
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new StartTickingSound(dragon.getId(), charging, instance.location().withSuffix(dragon.getStringUUID())));
            }
        });
    }

    default void playEndSound(final Player dragon) {
        sound().flatMap(Sound::end).ifPresent(end -> {
            if (dragon.level().isClientSide()) {
                DragonSurvival.PROXY.playSoundAtEyeLevel(dragon, end);
            } else {
                dragon.level().playSound(dragon, dragon.blockPosition(), end, SoundSource.PLAYERS, 1, 1);
            }
        });
    }

    default void playStartAndChargingAnimation(final Player dragon) {
        animations().flatMap(Animations::startAndCharging).ifPresent(startAndCharging -> {
            if (dragon.level().isClientSide()) {
                AbilityAnimation abilityAnimation = startAndCharging.map(
                        simple -> simple,
                        compound -> compound
                );
                // If it is simple, we just loop. If it is compound, then we ignore the AnimationType anyway, and go from a single play of start into looping charging.
                DragonSurvival.PROXY.setCurrentAbilityAnimation(dragon, new Pair<>(abilityAnimation, AnimationType.LOOPING));
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new SyncAbilityAnimation(dragon.getId(), AnimationType.LOOPING, startAndCharging));
            }
        });
    }

    default void playLoopingAnimation(final Player dragon) {
        animations().flatMap(Animations::looping).ifPresent(looping -> {
            if (dragon.level().isClientSide()) {
                DragonSurvival.PROXY.setCurrentAbilityAnimation(dragon, new Pair<>(looping, AnimationType.LOOPING));
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new SyncAbilityAnimation(dragon.getId(), AnimationType.LOOPING, Either.right(looping)));
            }
        });
    }

    default void playEndAnimation(final Player dragon) {
        animations().flatMap(Animations::end).ifPresent(end -> {
            if (dragon.level().isClientSide()) {
                DragonSurvival.PROXY.setCurrentAbilityAnimation(dragon, new Pair<>(end, AnimationType.PLAY_ONCE));
            } else {
                PacketDistributor.sendToPlayersTrackingEntity(dragon, new SyncAbilityAnimation(dragon.getId(), AnimationType.PLAY_ONCE, Either.right(end)));
            }
        });
    }

    enum Type implements StringRepresentable {
        PASSIVE("passive"),
        SIMPLE("simple"),
        CHANNELED("channeled");

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
}
