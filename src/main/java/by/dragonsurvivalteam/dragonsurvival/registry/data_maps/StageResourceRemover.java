package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record StageResourceRemover(List<ResourceKey<DragonStage>> keys) implements DataMapValueRemover<DragonSpecies, Map<ResourceKey<DragonStage>, StageResources.StageResource>> {
    public static final Codec<StageResourceRemover> CODEC = ResourceKey.codec(DragonStage.REGISTRY).listOf().xmap(StageResourceRemover::new, StageResourceRemover::keys);

    @Override
    public @NotNull Optional<Map<ResourceKey<DragonStage>, StageResources.StageResource>> remove(@NotNull final Map<ResourceKey<DragonStage>, StageResources.StageResource> value, @NotNull final Registry<DragonSpecies> registry, @NotNull final Either<TagKey<DragonSpecies>, ResourceKey<DragonSpecies>> source, @NotNull final DragonSpecies species) {
        Map<ResourceKey<DragonStage>, StageResources.StageResource> newResources = new HashMap<>(value);
        keys.forEach(newResources::remove);

        if (newResources.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(newResources);
    }
}
