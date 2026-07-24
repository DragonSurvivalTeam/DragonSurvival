package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncAbilityLevel;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/** The type cannot be a parameterized type because the parameter from the input cannot be validated to match (due to type erasure) */
@EventBusSubscriber
public interface UpgradeType<T> {
    ResourceKey<Registry<MapCodec<? extends UpgradeType<?>>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("upgrade_type"));
    Registry<MapCodec<? extends UpgradeType<?>>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<UpgradeType<?>> CODEC = REGISTRY.byNameCodec().dispatch("upgrade_type", UpgradeType::codec, Function.identity());

    Predicate<Optional<UpgradeType<?>>> IS_MANUAL = optional -> optional.isPresent() && optional.get() instanceof ExperiencePointsUpgrade;

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("experience_points"), () -> ExperiencePointsUpgrade.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("experience_levels"), () -> ExperienceLevelUpgrade.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("dragon_growth"), () -> DragonGrowthUpgrade.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("item_based"), () -> ItemUpgrade.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("condition_based"), () -> ConditionUpgrade.CODEC);
        }
    }

    static <V extends UpgradeType<?>> Products.P1<RecordCodecBuilder.Mu<V>, Integer> codecStart(final RecordCodecBuilder.Instance<V> instance) {
        return instance.group(ExtraCodecs.intRange(DragonAbilityInstance.MIN_LEVEL, DragonAbilityInstance.MAX_LEVEL).fieldOf("maximum_level").forGetter(UpgradeType::maxLevel));
    }

    @SuppressWarnings("unchecked") // ignore
    default boolean attempt(final ServerPlayer dragon, final DragonAbilityInstance ability, @Nullable final Object input) {
        // Need to find the 'UpgradeType' interface to check the parameter type
        Type[] interfaces = getClass().getGenericInterfaces();

        for (Type type : interfaces) {
            if (!(type instanceof ParameterizedType parameterized)) {
                continue;
            }

            if (parameterized.getRawType() != UpgradeType.class) {
                continue;
            }

            Class<?> parameterClass = (Class<?>) parameterized.getActualTypeArguments()[0];

            // 'Void' as type parameter means the upgrade logic is not dependent on any input
            if (input == null && parameterClass == Void.class || parameterClass.isInstance(input)) {
                if (apply(dragon, ability, (T) input)) {
                    PacketDistributor.sendToPlayer(dragon, new SyncAbilityLevel(ability.key(), ability.level()));
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean canUpgrade(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        return ability.level() < maxLevel();
    }

    default boolean canDowngrade(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        return ability.level() > minLevel();
    }

    default int minLevel() {
        return 0;
    }

    boolean apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final T input);

    MutableComponent getDescription(int abilityLevel);

    int maxLevel();

    MapCodec<? extends UpgradeType<?>> codec();
}
