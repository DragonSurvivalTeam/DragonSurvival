package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value= DeferredHolder.class)
public abstract class DeferredHolderMixin<R> {
    @Shadow @Final protected ResourceKey<R> key;

    @ModifyReturnValue(method = "getRegistry", at = @At(value = "RETURN"))
    protected Registry<R> dragonSurvival$getRegistry(final @Nullable Registry<R> original) {
        if (original == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

            if (server != null) {
                return server.registryAccess().registry(key.registryKey()).orElse(null);
            }
        }
        return original;
    }
}
