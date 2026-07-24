package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.ContextExtension;
import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.common.conditions.ConditionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ConditionContext.class)
public abstract class ConditionContextMixin implements ContextExtension {
    @Unique private RegistryAccess dragonSurvival$registryAccess;

    @Override
    public void dragonSurvival$setRegistryAccess(final RegistryAccess registryAccess) {
        this.dragonSurvival$registryAccess = registryAccess;
    }

    @Override
    public RegistryAccess dragonSurvival$getRegistryAccess() {
        return dragonSurvival$registryAccess;
    }
}
