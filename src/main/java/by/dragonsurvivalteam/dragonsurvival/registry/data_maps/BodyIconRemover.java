package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record BodyIconRemover(List<ResourceKey<DragonSpecies>> keys) implements DataMapValueRemover<DragonBody, Map<ResourceKey<DragonSpecies>, Identifier>> {
    public static final Codec<BodyIconRemover> CODEC = ResourceKey.codec(DragonSpecies.REGISTRY).listOf().xmap(BodyIconRemover::new, BodyIconRemover::keys);

    @Override
    public @NotNull Optional<Map<ResourceKey<DragonSpecies>, Identifier>> remove(@NotNull final Map<ResourceKey<DragonSpecies>, Identifier> value, @NotNull final Registry<DragonBody> registry, @NotNull final Either<TagKey<DragonBody>, ResourceKey<DragonBody>> source, @NotNull final DragonBody body) {
        Map<ResourceKey<DragonSpecies>, Identifier> newIcons = new HashMap<>(value);
        keys.forEach(newIcons::remove);

        if (newIcons.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(newIcons);
    }
}
