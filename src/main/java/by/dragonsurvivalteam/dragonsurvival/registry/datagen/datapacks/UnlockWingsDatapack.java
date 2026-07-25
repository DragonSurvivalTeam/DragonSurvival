package by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import net.minecraft.data.worldgen.BootstapContext;

public class UnlockWingsDatapack {
    public static void register(final BootstapContext<DragonAbility> context) {
        CaveDragonAbilities.registerUnlockedWings(context);
        ForestDragonAbilities.registerUnlockedWings(context);
        SeaDragonAbilities.registerUnlockedWings(context);
    }
}
