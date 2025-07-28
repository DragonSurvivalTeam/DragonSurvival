package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BodyIconProvider extends DataMapProvider {
    public BodyIconProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void gather(HolderLookup.@NotNull Provider provider) {
        builder(DSDataMaps.BODY_ICONS)
                .add(DragonBodies.CENTER, Map.of(
                        BuiltInDragonSpecies.CAVE_DRAGON, DragonSurvival.res("textures/gui/custom/body/center/cave_dragon.png"),
                        BuiltInDragonSpecies.FOREST_DRAGON, DragonSurvival.res("textures/gui/custom/body/center/forest_dragon.png"),
                        BuiltInDragonSpecies.SEA_DRAGON, DragonSurvival.res("textures/gui/custom/body/center/sea_dragon.png")
                ), false, new RegisteredCondition<>(DragonBodies.CENTER))
                .add(DragonBodies.EAST, Map.of(
                        BuiltInDragonSpecies.CAVE_DRAGON, DragonSurvival.res("textures/gui/custom/body/east/cave_dragon.png"),
                        BuiltInDragonSpecies.FOREST_DRAGON, DragonSurvival.res("textures/gui/custom/body/east/forest_dragon.png"),
                        BuiltInDragonSpecies.SEA_DRAGON, DragonSurvival.res("textures/gui/custom/body/east/sea_dragon.png")
                ), false, new RegisteredCondition<>(DragonBodies.EAST))
                .add(DragonBodies.NORTH, Map.of(
                        BuiltInDragonSpecies.CAVE_DRAGON, DragonSurvival.res("textures/gui/custom/body/north/cave_dragon.png"),
                        BuiltInDragonSpecies.FOREST_DRAGON, DragonSurvival.res("textures/gui/custom/body/north/forest_dragon.png"),
                        BuiltInDragonSpecies.SEA_DRAGON, DragonSurvival.res("textures/gui/custom/body/north/sea_dragon.png")
                ), false, new RegisteredCondition<>(DragonBodies.NORTH))
                .add(DragonBodies.SOUTH, Map.of(
                        BuiltInDragonSpecies.CAVE_DRAGON, DragonSurvival.res("textures/gui/custom/body/south/cave_dragon.png"),
                        BuiltInDragonSpecies.FOREST_DRAGON, DragonSurvival.res("textures/gui/custom/body/south/forest_dragon.png"),
                        BuiltInDragonSpecies.SEA_DRAGON, DragonSurvival.res("textures/gui/custom/body/south/sea_dragon.png")
                ), false, new RegisteredCondition<>(DragonBodies.SOUTH))
                .add(DragonBodies.WEST, Map.of(
                        BuiltInDragonSpecies.CAVE_DRAGON, DragonSurvival.res("textures/gui/custom/body/west/cave_dragon.png"),
                        BuiltInDragonSpecies.FOREST_DRAGON, DragonSurvival.res("textures/gui/custom/body/west/forest_dragon.png"),
                        BuiltInDragonSpecies.SEA_DRAGON, DragonSurvival.res("textures/gui/custom/body/west/sea_dragon.png")
                ), false, new RegisteredCondition<>(DragonBodies.EAST));
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Body Icons";
    }
}
