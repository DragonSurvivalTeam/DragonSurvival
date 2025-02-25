package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.EndPlatform;
import by.dragonsurvivalteam.dragonsurvival.registry.DSConditions;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EndPlatformProvider extends DataMapProvider {
    public EndPlatformProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void gather() {
        builder(DSDataMaps.END_PLATFORMS)
                .add(BuiltInDragonSpecies.CAVE_DRAGON, EndPlatform.from("end_spawn_platforms/cave_end_spawn_platform", -200, 50, 0), false, DSConditions.CAVE_DRAGON_LOADED)
                .add(BuiltInDragonSpecies.FOREST_DRAGON, EndPlatform.from("end_spawn_platforms/forest_end_spawn_platform", 0, 50, -200), false, DSConditions.FOREST_DRAGON_LOADED)
                .add(BuiltInDragonSpecies.SEA_DRAGON, EndPlatform.from("end_spawn_platforms/sea_end_spawn_platform", 0, 50, 200), false, DSConditions.SEA_DRAGON_LOADED);
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival End Platforms";
    }
}
