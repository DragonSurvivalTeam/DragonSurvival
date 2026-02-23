package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

// TODO :: Remove in 1.21.4
public record RegisteredCondition<T>(ResourceKey<T> registryKey) implements ICondition {
    public static final MapCodec<RegisteredCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Identifier.CODEC.fieldOf("registry").forGetter(condition -> condition.registryKey().registry()),
                    Identifier.CODEC.fieldOf("value").forGetter(condition -> condition.registryKey().identifier()))
            .apply(instance, RegisteredCondition::new));

    private RegisteredCondition(final Identifier registryType, final Identifier registryName) {
        this(ResourceKey.create(ResourceKey.createRegistryKey(registryType), registryName));
    }

    // FIXME
    @Override
    public boolean test(@NotNull final IContext context) {
        return false;//((ContextExtension) context).dragonSurvival$getRegistryAccess().holder(registryKey).map(Holder::isBound).orElse(false);
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}