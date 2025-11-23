package by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ManaHandling;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.BootstrapContext;

public class DisableExperienceConversionDatapack {
    public static void register(final BootstrapContext<DragonSpecies> context, final RegistrySetBuilder.PatchedRegistries patched) {
        context.register(BuiltInDragonSpecies.CAVE_DRAGON, speciesWithNoExperienceConversion(context, patched.patches().holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON)));
        context.register(BuiltInDragonSpecies.SEA_DRAGON, speciesWithNoExperienceConversion(context, patched.patches().holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)));
        context.register(BuiltInDragonSpecies.FOREST_DRAGON, speciesWithNoExperienceConversion(context, patched.patches().holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)));
    }

    private static DragonSpecies speciesWithNoExperienceConversion(final BootstrapContext<DragonSpecies> context, final Holder<DragonSpecies> species) {
        return new DragonSpecies(
                species.value().startingGrowth(),
                species.value().unlockableBehavior(),
                ManaHandling.NONE,
                species.value().stages(),
                species.value().bodies(),
                // For some reason we have to re-fetch these holders
                // Otherwise data generation has a problem to find them...?
                context.lookup(DragonAbility.REGISTRY).getOrThrow(species.value().abilities().unwrapKey().orElseThrow()),
                context.lookup(DragonPenalty.REGISTRY).getOrThrow(species.value().penalties().unwrapKey().orElseThrow()),
                species.value().miscResources()
        );
    }
}
