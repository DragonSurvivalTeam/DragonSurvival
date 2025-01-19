package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DietEntryRemover(List<String> key) implements DataMapValueRemover<DragonSpecies, List<DietEntry>> {
    public static final Codec<DietEntryRemover> CODEC = ResourceLocationWrapper.validatedCodec().listOf().xmap(DietEntryRemover::new, DietEntryRemover::key);

    @Override
    public @NotNull Optional<List<DietEntry>> remove(@NotNull final List<DietEntry> value, @NotNull final Registry<DragonSpecies> registry, @NotNull final Either<TagKey<DragonSpecies>, ResourceKey<DragonSpecies>> source, @NotNull final DragonSpecies species) {
        // According to the documentation the original value should not be modified
        List<DietEntry> newDiet = new ArrayList<>(value);
        newDiet.removeIf(entry -> key.contains(entry.items()));

        if (newDiet.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(newDiet);
    }
}
