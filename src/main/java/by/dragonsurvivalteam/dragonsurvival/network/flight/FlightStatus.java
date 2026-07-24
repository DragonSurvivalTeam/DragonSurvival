package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import io.netty.buffer.ByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record FlightStatus(int playerId, boolean hasFlight) implements CustomPacketPayload {
    public static final Type<FlightStatus> TYPE = new Type<>(DragonSurvival.res("sync_flight_status"));

    public static final StreamCodec<ByteBuf, FlightStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FlightStatus::playerId,
            ByteBufCodecs.BOOL, FlightStatus::hasFlight,
            FlightStatus::new
    );

    public static void handleClient(final FlightStatus packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData data = FlightData.getData(player);
                data.hasFlight = packet.hasFlight;
            }
        });
    }

    public static void handleServer(final FlightStatus packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData data = FlightData.getData(player);
                data.hasFlight = packet.hasFlight;
            }
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}