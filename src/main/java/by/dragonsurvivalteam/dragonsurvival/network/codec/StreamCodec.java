package by.dragonsurvivalteam.dragonsurvival.network.codec;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import io.netty.buffer.ByteBuf;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 1.20.1-compatible equivalent of the stream codec API introduced after 1.20.1.
 */
public interface StreamCodec<B, V> {
    V decode(B buffer);

    void encode(B buffer, V value);

    static <B, V> StreamCodec<B, V> unit(final V expectedValue) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B buffer) {
                return expectedValue;
            }

            @Override
            public void encode(final B buffer, final V value) {
                if (!expectedValue.equals(value)) {
                    throw new IllegalStateException("Can't encode '" + value + "', expected '" + expectedValue + "'");
                }
            }
        };
    }

    default <O> StreamCodec<B, O> apply(final CodecOperation<B, V, O> operation) {
        return operation.apply(this);
    }

    default <O> StreamCodec<B, O> map(final Function<? super V, ? extends O> factory, final Function<? super O, ? extends V> getter) {
        return new StreamCodec<>() {
            @Override
            public O decode(final B buffer) {
                return factory.apply(StreamCodec.this.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final O value) {
                StreamCodec.this.encode(buffer, getter.apply(value));
            }
        };
    }

    default <O extends ByteBuf> StreamCodec<O, V> mapStream(final Function<O, ? extends B> bufferFactory) {
        return new StreamCodec<>() {
            @Override
            public V decode(final O buffer) {
                return StreamCodec.this.decode(bufferFactory.apply(buffer));
            }

            @Override
            public void encode(final O buffer, final V value) {
                StreamCodec.this.encode(bufferFactory.apply(buffer), value);
            }
        };
    }

    default <U> StreamCodec<B, U> dispatch(
            final Function<? super U, ? extends V> keyGetter,
            final Function<? super V, ? extends StreamCodec<? super B, ? extends U>> codecGetter
    ) {
        return new StreamCodec<>() {
            @Override
            public U decode(final B buffer) {
                V key = StreamCodec.this.decode(buffer);
                return codecGetter.apply(key).decode(buffer);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void encode(final B buffer, final U value) {
                V key = keyGetter.apply(value);
                StreamCodec.this.encode(buffer, key);
                ((StreamCodec<B, U>) codecGetter.apply(key)).encode(buffer, value);
            }
        };
    }

    static <B, C, T1> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final Function<T1, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(final B buffer) {
                return factory.apply(codec1.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final C value) {
                codec1.encode(buffer, getter1.apply(value));
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final BiFunction<T1, T2, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(final B buffer) {
                return factory.apply(codec1.decode(buffer), codec2.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final Function3<T1, T2, T3, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(final B buffer) {
                return factory.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final Function4<T1, T2, T3, T4, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(final B buffer) {
                return factory.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer), codec4.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final Function5<T1, T2, T3, T4, T5, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(final B buffer) {
                return factory.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer), codec4.decode(buffer), codec5.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6,
            final Function<C, T6> getter6,
            final Function6<T1, T2, T3, T4, T5, T6, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(final B buffer) {
                return factory.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer), codec4.decode(buffer), codec5.decode(buffer), codec6.decode(buffer));
            }

            @Override
            public void encode(final B buffer, final C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
            }
        };
    }

    @SuppressWarnings("unchecked")
    default <S extends B> StreamCodec<S, V> cast() {
        return (StreamCodec<S, V>) this;
    }

    @FunctionalInterface
    interface CodecOperation<B, S, T> {
        StreamCodec<B, T> apply(StreamCodec<B, S> codec);
    }
}
