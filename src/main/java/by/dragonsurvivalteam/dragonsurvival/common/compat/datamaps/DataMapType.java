package by.dragonsurvivalteam.dragonsurvival.common.compat.datamaps;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Backport of the 1.21.1 registry data-map descriptor and runtime storage.
 */
public class DataMapType<R, T> {
    private final ResourceKey<Registry<R>> registryKey;
    private final ResourceLocation id;
    private final Codec<T> codec;
    private final @Nullable Codec<T> networkCodec;
    private final boolean mandatorySync;
    private volatile Map<ResourceKey<R>, T> values = Map.of();

    protected DataMapType(
            final ResourceKey<Registry<R>> registryKey,
            final ResourceLocation id,
            final Codec<T> codec,
            final @Nullable Codec<T> networkCodec,
            final boolean mandatorySync
    ) {
        if (networkCodec == null && mandatorySync) {
            throw new IllegalArgumentException("Mandatory sync requires a network codec");
        }
        this.registryKey = Objects.requireNonNull(registryKey);
        this.id = Objects.requireNonNull(id);
        this.codec = Objects.requireNonNull(codec);
        this.networkCodec = networkCodec;
        this.mandatorySync = mandatorySync;
    }

    public static <T, R> Builder<T, R> builder(
            final ResourceLocation id,
            final ResourceKey<Registry<R>> registry,
            final Codec<T> codec
    ) {
        return new Builder<>(registry, id, codec);
    }

    public ResourceKey<Registry<R>> registryKey() {
        return registryKey;
    }

    public ResourceLocation id() {
        return id;
    }

    public Codec<T> codec() {
        return codec;
    }

    public @Nullable Codec<T> networkCodec() {
        return networkCodec;
    }

    public boolean mandatorySync() {
        return mandatorySync;
    }

    public @Nullable T get(final Holder<R> holder) {
        return holder.unwrapKey().map(values::get).orElse(null);
    }

    public @Nullable T get(final ResourceKey<R> key) {
        return values.get(key);
    }

    public Map<ResourceKey<R>, T> values() {
        return values;
    }

    public void replaceValues(final Map<ResourceKey<R>, T> values) {
        this.values = Map.copyOf(values);
    }

    public static class Builder<T, R> {
        protected final ResourceKey<Registry<R>> registryKey;
        protected final ResourceLocation id;
        protected final Codec<T> codec;
        protected @Nullable Codec<T> networkCodec;
        protected boolean mandatorySync;

        protected Builder(
                final ResourceKey<Registry<R>> registryKey,
                final ResourceLocation id,
                final Codec<T> codec
        ) {
            this.registryKey = registryKey;
            this.id = id;
            this.codec = codec;
        }

        public Builder<T, R> synced(final Codec<T> networkCodec, final boolean mandatory) {
            this.networkCodec = networkCodec;
            this.mandatorySync = mandatory;
            return this;
        }

        public DataMapType<R, T> build() {
            return new DataMapType<>(registryKey, id, codec, networkCodec, mandatorySync);
        }
    }
}
