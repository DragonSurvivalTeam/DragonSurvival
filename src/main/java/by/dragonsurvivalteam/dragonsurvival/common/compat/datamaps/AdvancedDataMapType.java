package by.dragonsurvivalteam.dragonsurvival.common.compat.datamaps;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class AdvancedDataMapType<R, T, VR extends DataMapValueRemover<R, T>> extends DataMapType<R, T> {
    private final Codec<VR> remover;
    private final DataMapValueMerger<R, T> merger;

    private AdvancedDataMapType(
            final ResourceKey<Registry<R>> registryKey,
            final ResourceLocation id,
            final Codec<T> codec,
            final @Nullable Codec<T> networkCodec,
            final boolean mandatorySync,
            final Codec<VR> remover,
            final DataMapValueMerger<R, T> merger
    ) {
        super(registryKey, id, codec, networkCodec, mandatorySync);
        this.remover = remover;
        this.merger = merger;
    }

    public Codec<VR> remover() {
        return remover;
    }

    public DataMapValueMerger<R, T> merger() {
        return merger;
    }

    public static <T, R> Builder<T, R, DataMapValueRemover.Default<T, R>> builder(
            final ResourceLocation id,
            final ResourceKey<Registry<R>> registry,
            final Codec<T> codec
    ) {
        return new Builder<T, R, DataMapValueRemover.Default<T, R>>(registry, id, codec)
                .remover(DataMapValueRemover.Default.codec());
    }

    public static final class Builder<T, R, VR extends DataMapValueRemover<R, T>>
            extends DataMapType.Builder<T, R> {
        private Codec<VR> remover;
        private DataMapValueMerger<R, T> merger = DataMapValueMerger.defaultMerger();

        private Builder(
                final ResourceKey<Registry<R>> registryKey,
                final ResourceLocation id,
                final Codec<T> codec
        ) {
            super(registryKey, id, codec);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <VR1 extends DataMapValueRemover<R, T>> Builder<T, R, VR1> remover(final Codec<VR1> remover) {
            this.remover = (Codec) remover;
            return (Builder<T, R, VR1>) (Builder) this;
        }

        public Builder<T, R, VR> merger(final DataMapValueMerger<R, T> merger) {
            this.merger = merger;
            return this;
        }

        @Override
        public Builder<T, R, VR> synced(final Codec<T> networkCodec, final boolean mandatory) {
            super.synced(networkCodec, mandatory);
            return this;
        }

        @Override
        public AdvancedDataMapType<R, T, VR> build() {
            return new AdvancedDataMapType<>(
                    registryKey, id, codec, networkCodec, mandatorySync, remover, merger
            );
        }
    }
}
