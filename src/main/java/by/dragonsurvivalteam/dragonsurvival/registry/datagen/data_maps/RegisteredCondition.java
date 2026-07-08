package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record RegisteredCondition<T>(ResourceKey<T> registryKey, HolderLookup.RegistryLookup<T> registryLookup) implements ICondition {
    private static final MapCodec<RegisteredCondition<?>> BASE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Identifier.CODEC.fieldOf("registry").forGetter(condition -> condition.registryKey().registry()),
                    Identifier.CODEC.fieldOf("value").forGetter(condition -> condition.registryKey().identifier()))
            .apply(instance, RegisteredCondition::new));

    public static final MapCodec<RegisteredCondition<?>> CODEC = new MapCodec<>() {
        @Override
        public <O> DataResult<RegisteredCondition<?>> decode(final DynamicOps<O> ops, final MapLike<O> input) {
            return BASE_CODEC.decode(ops, input).flatMap(condition -> condition.withRegistryLookup(ops, input));
        }

        @Override
        public <O> RecordBuilder<O> encode(final RegisteredCondition<?> input, final DynamicOps<O> ops, final RecordBuilder<O> prefix) {
            return BASE_CODEC.encode(input, ops, prefix);
        }

        @Override
        public <O> Stream<O> keys(final DynamicOps<O> ops) {
            return BASE_CODEC.keys(ops);
        }
    };

    public RegisteredCondition(final ResourceKey<T> registryKey) {
        this(registryKey, null);
    }

    private RegisteredCondition(final Identifier registryType, final Identifier registryName) {
        this(ResourceKey.create(ResourceKey.createRegistryKey(registryType), registryName), null);
    }

    private <O> DataResult<RegisteredCondition<?>> withRegistryLookup(final DynamicOps<O> ops, final MapLike<O> input) {
        return RegistryOps.retrieveRegistryLookup(registryKey.registryKey())
                .decode(ops, input)
                .map(lookup -> (RegisteredCondition<?>) new RegisteredCondition<>(registryKey, lookup));
    }

    @Override
    public boolean test(@NotNull final IContext context) {
        if (registryLookup == null) {
            // During some early reload phases NeoForge has not exposed the registry lookup yet.
            // In that case, keep the entry rather than crashing or stripping built-in data maps.
            return true;
        }

        return registryLookup.get(registryKey).isPresent();
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
