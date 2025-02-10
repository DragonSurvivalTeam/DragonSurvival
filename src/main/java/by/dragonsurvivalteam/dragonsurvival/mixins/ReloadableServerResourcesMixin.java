package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.ContextExtension;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Shadow @Final private ICondition.IContext context;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void dragonSurvival$setRegistryAccess(final RegistryAccess.Frozen registryAccess, final FeatureFlagSet enabledFeatures, final Commands.CommandSelection commandSelection, final int functionCompilationLevel, final CallbackInfo callback) {
        ((ContextExtension) context).dragonSurvival$setRegistryAccess(registryAccess);
    }
}
