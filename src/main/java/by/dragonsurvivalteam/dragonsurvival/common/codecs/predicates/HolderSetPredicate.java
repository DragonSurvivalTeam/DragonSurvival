package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.function.Function;

public record HolderSetPredicate<T>(List<Either<TagKey<T>, ResourceKey<T>>> selectors) {
    public static <T> Codec<HolderSetPredicate<T>> codec(final ResourceKey<? extends Registry<T>> registry) {
        Codec<Either<TagKey<T>, ResourceKey<T>>> selectorCodec = Codec.either(
                TagKey.hashedCodec(registry),
                ResourceKey.codec(registry)
        );
        Codec<List<Either<TagKey<T>, ResourceKey<T>>>> selectorsCodec = Codec.either(
                selectorCodec,
                selectorCodec.listOf()
        ).xmap(
                either -> either.map(List::of, Function.identity()),
                selectors -> selectors.size() == 1 ? Either.left(selectors.get(0)) : Either.right(selectors)
        );
        return selectorsCodec.xmap(HolderSetPredicate::new, HolderSetPredicate::selectors);
    }

    public static <T> HolderSetPredicate<T> of(final Holder<T> holder) {
        return new HolderSetPredicate<>(List.of(Either.right(holder.unwrapKey().orElseThrow())));
    }

    public static <T> HolderSetPredicate<T> of(final HolderSet<T> holders) {
        List<Either<TagKey<T>, ResourceKey<T>>> selectors = holders.unwrap().map(
                tag -> List.of(Either.left(tag)),
                entries -> entries.stream()
                        .map(holder -> Either.<TagKey<T>, ResourceKey<T>>right(holder.unwrapKey().orElseThrow()))
                        .toList()
        );
        return new HolderSetPredicate<>(selectors);
    }

    public boolean contains(final Holder<T> holder) {
        return selectors.stream().anyMatch(selector -> selector.map(holder::is, holder::is));
    }
}
