package by.dragonsurvivalteam.dragonsurvival.util;

import java.util.Optional;

public record Triple<A, B, C>(Optional<A> first, Optional<B> second, Optional<C> third) {
    public static <A, B, C> Triple<A, B, C> of(final A first, final B second, final C third) {
        return new Triple<>(Optional.ofNullable(first), Optional.ofNullable(second), Optional.ofNullable(third));
    }
}