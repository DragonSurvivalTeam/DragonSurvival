package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

// TODO :: Remove in 1.21.4
public record RegisteredCondition<T>(ResourceKey<T> registryKey) implements ICondition {
    public static final MapCodec<RegisteredCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf("registry").forGetter(condition -> condition.registryKey().registry()),
                    ResourceLocation.CODEC.fieldOf("value").forGetter(condition -> condition.registryKey().location()))
            .apply(instance, RegisteredCondition::new));

    private RegisteredCondition(final ResourceLocation registryType, final ResourceLocation registryName) {
        this(ResourceKey.create(ResourceKey.createRegistryKey(registryType), registryName));
    }

    @Override
    public boolean test(@NotNull final IContext context) {
        return ((ContextExtension) context).dragonSurvival$getRegistryAccess().holder(registryKey).map(Holder::isBound).orElse(false);
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}