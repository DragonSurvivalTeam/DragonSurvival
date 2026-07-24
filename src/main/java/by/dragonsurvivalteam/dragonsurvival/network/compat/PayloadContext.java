package by.dragonsurvivalteam.dragonsurvival.network.compat;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 1.20.1 transport wrapper matching the payload context used by packet handlers.
 */
public final class PayloadContext {
    private final NetworkEvent.Context context;

    public PayloadContext(final NetworkEvent.Context context) {
        this.context = context;
    }

    public @Nullable Player player() {
        return context.getDirection() == NetworkDirection.PLAY_TO_SERVER
                ? context.getSender()
                : DragonSurvival.PROXY.getLocalPlayer();
    }

    public CompletableFuture<Void> enqueueWork(final Runnable work) {
        return context.enqueueWork(work);
    }

    public <T> CompletableFuture<T> enqueueWork(final Supplier<T> work) {
        CompletableFuture<T> result = new CompletableFuture<>();
        context.enqueueWork(() -> {
            try {
                result.complete(work.get());
            } catch (Throwable throwable) {
                result.completeExceptionally(throwable);
            }
        });
        return result;
    }

    public void reply(final CustomPacketPayload payload) {
        NetworkHandler.reply(payload, context);
    }

    NetworkDirection direction() {
        return context.getDirection();
    }
}
