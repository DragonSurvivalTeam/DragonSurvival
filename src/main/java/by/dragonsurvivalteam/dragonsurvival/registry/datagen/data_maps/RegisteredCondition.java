package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record RegisteredCondition<T>(ResourceKey<T> registryKey) implements ICondition {
    public static final MapCodec<RegisteredCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Identifier.CODEC.fieldOf("registry").forGetter(condition -> condition.registryKey().registry()),
                    Identifier.CODEC.fieldOf("value").forGetter(condition -> condition.registryKey().identifier()))
            .apply(instance, RegisteredCondition::new));

    private RegisteredCondition(final Identifier registryType, final Identifier registryName) {
        this(ResourceKey.create(ResourceKey.createRegistryKey(registryType), registryName));
    }

    @Override
    public boolean test(@NotNull final IContext context) {
        HolderLookup.RegistryLookup<T> lookup = null;

        if (context.registryAccess() != null && context.registryAccess() != net.minecraft.core.RegistryAccess.EMPTY) {
            lookup = context.registryAccess().lookup(registryKey.registryKey()).orElse(null);
        }

        if (lookup == null) {
            lookup = CommonHooks.resolveLookup(registryKey.registryKey());
        }

        if (lookup == null) {
            // During some early reload phases NeoForge has not exposed the registry lookup yet.
            // In that case, keep the entry rather than crashing or stripping built-in data maps.
            return true;
        }

        return lookup.get(registryKey).isPresent();
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
