package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.loaders.CustomSoulIconLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

// FIXME :: in > 1.21.1 it might be doable using https://github.com/neoforged/NeoForge/pull/1915?
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {
    /** We need to make sure this loader runs before the model bakery (which triggers the event to register new models) */
    @ModifyVariable(method = "reload", at = @At("STORE"))
    private CompletableFuture<Map<ResourceLocation, BlockModel>> test(final CompletableFuture<Map<ResourceLocation, BlockModel>> future, final PreparableReloadListener.PreparationBarrier preparationBarrier, final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
        return CompletableFuture.runAsync(() -> CustomSoulIconLoader.reload(resourceManager)).thenCompose(v -> future);
    }
}
