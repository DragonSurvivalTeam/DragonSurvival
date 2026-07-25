package by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ManaHandling;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.data.worldgen.BootstapContext;

public class DisableExperienceConversionDatapack {
    public static void register(final BootstapContext<DragonSpecies> context) {
        BuiltInDragonSpecies.registerTypes(context, ManaHandling.NONE);
    }
}
