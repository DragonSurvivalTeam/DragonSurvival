package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DietEntryMerger implements DataMapValueMerger<DragonSpecies, List<DietEntry>> {
    @Override
    public @NotNull List<DietEntry> merge(@NotNull final Registry<DragonSpecies> registry, @NotNull final Either<TagKey<DragonSpecies>, ResourceKey<DragonSpecies>> first, @NotNull final List<DietEntry> firstValue, @NotNull final Either<TagKey<DragonSpecies>, ResourceKey<DragonSpecies>> second, @NotNull final List<DietEntry> secondValue) {
        List<DietEntry> newDiet = new ArrayList<>(firstValue);
        newDiet.removeIf(secondValue::contains);
        newDiet.addAll(secondValue);
        return newDiet;
    }
}
