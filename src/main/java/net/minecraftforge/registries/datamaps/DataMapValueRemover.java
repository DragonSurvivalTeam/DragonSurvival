package net.minecraftforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;

@FunctionalInterface
public interface DataMapValueRemover<R, T> {
    Optional<T> remove(
            T value,
            Registry<R> registry,
            Either<TagKey<R>, ResourceKey<R>> source,
            R object
    );

    final class Default<T, R> implements DataMapValueRemover<R, T> {
        private static final Default<?, ?> INSTANCE = new Default<>();

        private Default() {}

        @SuppressWarnings("unchecked")
        public static <T, R> Default<T, R> defaultRemover() {
            return (Default<T, R>) INSTANCE;
        }

        public static <T, R> Codec<Default<T, R>> codec() {
            return Codec.unit(Default.<T, R>defaultRemover());
        }

        @Override
        public Optional<T> remove(
                final T value,
                final Registry<R> registry,
                final Either<TagKey<R>, ResourceKey<R>> source,
                final R object
        ) {
            return Optional.empty();
        }
    }
}
