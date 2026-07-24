package by.dragonsurvivalteam.dragonsurvival.network.compat;

import net.minecraftforge.network.NetworkDirection;

import java.util.function.BiConsumer;

public final class DirectionalPayloadHandler<T extends CustomPacketPayload> implements BiConsumer<T, PayloadContext> {
    private final BiConsumer<T, PayloadContext> clientHandler;
    private final BiConsumer<T, PayloadContext> serverHandler;

    public DirectionalPayloadHandler(
            final BiConsumer<T, PayloadContext> clientHandler,
            final BiConsumer<T, PayloadContext> serverHandler
    ) {
        this.clientHandler = clientHandler;
        this.serverHandler = serverHandler;
    }

    @Override
    public void accept(final T payload, final PayloadContext context) {
        if (context.direction() == NetworkDirection.PLAY_TO_CLIENT) {
            clientHandler.accept(payload, context);
        } else {
            serverHandler.accept(payload, context);
        }
    }
}
