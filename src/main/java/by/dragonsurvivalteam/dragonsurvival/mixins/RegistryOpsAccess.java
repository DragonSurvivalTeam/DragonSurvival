package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RegistryOps.class)
public interface RegistryOpsAccess {
    @Accessor("lookupProvider")
    RegistryOps.RegistryInfoLookup dragonSurvival$getLookupProvider();
}
