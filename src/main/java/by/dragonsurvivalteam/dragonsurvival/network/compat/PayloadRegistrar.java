package by.dragonsurvivalteam.dragonsurvival.network.compat;

import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class PayloadRegistrar {
    private final Map<CustomPacketPayload.Type<?>, Registration<?>> registrations = new LinkedHashMap<>();

    public <T extends CustomPacketPayload> void playToClient(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super FriendlyByteBuf, T> codec,
            final BiConsumer<T, PayloadContext> handler
    ) {
        register(type, codec, Direction.CLIENTBOUND, handler);
    }

    public <T extends CustomPacketPayload> void playToServer(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super FriendlyByteBuf, T> codec,
            final BiConsumer<T, PayloadContext> handler
    ) {
        register(type, codec, Direction.SERVERBOUND, handler);
    }

    public <T extends CustomPacketPayload> void playBidirectional(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super FriendlyByteBuf, T> codec,
            final BiConsumer<T, PayloadContext> handler
    ) {
        register(type, codec, Direction.BIDIRECTIONAL, handler);
    }

    private <T extends CustomPacketPayload> void register(
            final CustomPacketPayload.Type<T> type,
            final StreamCodec<? super FriendlyByteBuf, T> codec,
            final Direction direction,
            final BiConsumer<T, PayloadContext> handler
    ) {
        Registration<?> previous = registrations.put(type, new Registration<>(type, codec, direction, handler));
        if (previous != null) {
            throw new IllegalStateException("Duplicate payload registration: " + type.id());
        }
    }

    public Collection<Registration<?>> registrations() {
        return registrations.values();
    }

    public enum Direction {
        CLIENTBOUND,
        SERVERBOUND,
        BIDIRECTIONAL;

        public boolean accepts(final NetworkDirection direction) {
            return this == BIDIRECTIONAL
                    || this == CLIENTBOUND && direction == NetworkDirection.PLAY_TO_CLIENT
                    || this == SERVERBOUND && direction == NetworkDirection.PLAY_TO_SERVER;
        }
    }

    public record Registration<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            Direction direction,
            BiConsumer<T, PayloadContext> handler
    ) {
        public CustomPacketPayload decode(final FriendlyByteBuf buffer) {
            return codec.decode(buffer);
        }

        @SuppressWarnings("unchecked")
        public void encode(final FriendlyByteBuf buffer, final CustomPacketPayload payload) {
            codec.encode(buffer, (T) payload);
        }

        @SuppressWarnings("unchecked")
        public void handle(final CustomPacketPayload payload, final PayloadContext context) {
            handler.accept((T) payload, context);
        }
    }
}
