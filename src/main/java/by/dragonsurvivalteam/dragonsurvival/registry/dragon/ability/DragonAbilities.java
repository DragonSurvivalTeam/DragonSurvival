package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DragonAbilities {
    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);
    }

    public static ResourceKey<DragonAbility> key(final ResourceLocation location) {
        return ResourceKey.create(DragonAbility.REGISTRY, location);
    }

    public static ResourceKey<DragonAbility> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
