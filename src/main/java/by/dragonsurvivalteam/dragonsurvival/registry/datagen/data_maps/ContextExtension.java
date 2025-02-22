package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import net.minecraft.core.RegistryAccess;

public interface ContextExtension {
    RegistryAccess dragonSurvival$getRegistryAccess();

    void dragonSurvival$setRegistryAccess(final RegistryAccess registryAccess);
}
