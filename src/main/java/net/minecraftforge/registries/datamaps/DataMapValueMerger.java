package net.minecraftforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FunctionalInterface
public interface DataMapValueMerger<R, T> {
    T merge(
            Registry<R> registry,
            Either<TagKey<R>, ResourceKey<R>> first,
            T firstValue,
            Either<TagKey<R>, ResourceKey<R>> second,
            T secondValue
    );

    static <T, R> DataMapValueMerger<R, T> defaultMerger() {
        return (registry, first, firstValue, second, secondValue) -> secondValue;
    }

    static <T, R> DataMapValueMerger<R, List<T>> listMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            List<T> result = new ArrayList<>(firstValue);
            result.addAll(secondValue);
            return result;
        };
    }

    static <T, R> DataMapValueMerger<R, Set<T>> setMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            Set<T> result = new HashSet<>(firstValue);
            result.addAll(secondValue);
            return result;
        };
    }

    static <K, V, R> DataMapValueMerger<R, Map<K, V>> mapMerger() {
        return (registry, first, firstValue, second, secondValue) -> {
            Map<K, V> result = new HashMap<>(firstValue);
            result.putAll(secondValue);
            return result;
        };
    }
}
