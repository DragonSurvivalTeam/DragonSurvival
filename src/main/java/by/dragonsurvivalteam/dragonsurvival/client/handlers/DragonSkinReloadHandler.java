package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@EventBusSubscriber(Dist.CLIENT)
public class DragonSkinReloadHandler{
    @SubscribeEvent
    public static void onReloadEvent(AddReloadListenerEvent reloadEvent) {
        reloadEvent.addListener(DragonSkinReloadHandler::reload);
    }

    public static CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
            DragonSkins.init(true);
        }, gameExecutor);
    }
}
