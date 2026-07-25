package net.minecraftforge.attachment;

import by.dragonsurvivalteam.dragonsurvival.common.serialization.INBTSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class AttachmentType<T> {
    private final Supplier<T> defaultValueSupplier;
    private final boolean serializable;
    private final boolean copyOnDeath;

    private AttachmentType(final Builder<T> builder) {
        defaultValueSupplier = builder.defaultValueSupplier;
        serializable = builder.serializable;
        copyOnDeath = builder.copyOnDeath;
    }

    public static <T> Builder<T> builder(final Supplier<T> defaultValueSupplier) {
        return new Builder<>(defaultValueSupplier);
    }

    public static <S extends Tag, T extends INBTSerializable<S>> Builder<T> serializable(final Supplier<T> defaultValueSupplier) {
        return new Builder<T>(defaultValueSupplier).serializable();
    }

    public T create() {
        return defaultValueSupplier.get();
    }

    public boolean isSerializable() {
        return serializable;
    }

    public boolean copyOnDeath() {
        return copyOnDeath;
    }

    @SuppressWarnings("unchecked")
    public @Nullable Tag serialize(final T value, final HolderLookup.Provider provider) {
        return serializable ? ((INBTSerializable<Tag>) value).serializeNBT(provider) : null;
    }

    @SuppressWarnings("unchecked")
    public T deserialize(final Tag tag, final HolderLookup.Provider provider) {
        T value = create();
        ((INBTSerializable<Tag>) value).deserializeNBT(provider, tag);
        return value;
    }

    public static final class Builder<T> {
        private final Supplier<T> defaultValueSupplier;
        private boolean serializable;
        private boolean copyOnDeath;

        private Builder(final Supplier<T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
        }

        private Builder<T> serializable() {
            serializable = true;
            return this;
        }

        public Builder<T> copyOnDeath() {
            if (!serializable) {
                throw new IllegalStateException("copyOnDeath requires a serializable attachment");
            }

            copyOnDeath = true;
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }
    }
}
