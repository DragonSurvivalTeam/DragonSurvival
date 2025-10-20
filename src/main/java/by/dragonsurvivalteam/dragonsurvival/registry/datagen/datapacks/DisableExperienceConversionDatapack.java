package by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ManaHandling;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;

public class DisableExperienceConversionDatapack {

    public static void register(final BootstrapContext<DragonSpecies> context) {
        context.register(BuiltInDragonSpecies.CAVE_DRAGON, speciesWithNoExperienceConversion(context.lookup(DragonSpecies.REGISTRY).getOrThrow(BuiltInDragonSpecies.CAVE_DRAGON)));
        context.register(BuiltInDragonSpecies.SEA_DRAGON, speciesWithNoExperienceConversion(context.lookup(DragonSpecies.REGISTRY).getOrThrow(BuiltInDragonSpecies.SEA_DRAGON)));
        context.register(BuiltInDragonSpecies.FOREST_DRAGON, speciesWithNoExperienceConversion(context.lookup(DragonSpecies.REGISTRY).getOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)));
    }

    private static DragonSpecies speciesWithNoExperienceConversion(final Holder<DragonSpecies> species) {
        return new DragonSpecies(
                species.value().startingGrowth(),
                species.value().unlockableBehavior(),
                ManaHandling.NONE,
                species.value().stages(),
                species.value().bodies(),
                species.value().abilities(),
                species.value().penalties(),
                species.value().miscResources()
        );
    }
}
