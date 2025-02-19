package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DataMapBandaid;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// TODO :: Remove when https://github.com/neoforged/NeoForge/issues/1867 is fixed
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin {
    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("RETURN"))
    private void dragonSurvival$restoreDataMaps(final ResourceKey<?> key, final Lifecycle registryLifecycle, final boolean hasIntrusiveHolders, final CallbackInfo callback) {
        // Our registry classes cannot be accessed here
        // Since that will try to initialize the codec, which use registries which are just getting initialized here
        // Leading to weird and unrelated errors which don't trace back to this point

        var bandaid = DataMapBandaid.BANDAID.remove(key);
        var current = ((BaseMappedRegistryAccess) this).dragonSurvival$getDataMaps();

        if (current.isEmpty() && bandaid != null) {
            current.putAll(bandaid);
        }
    }
}
