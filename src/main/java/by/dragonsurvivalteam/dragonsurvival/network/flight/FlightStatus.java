package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record FlightStatus(int playerId, boolean hasFlight) implements CustomPacketPayload {
    public static final Type<FlightStatus> TYPE = new Type<>(DragonSurvival.res("sync_flight_status"));

    public static final StreamCodec<ByteBuf, FlightStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FlightStatus::playerId,
            ByteBufCodecs.BOOL, FlightStatus::hasFlight,
            FlightStatus::new
    );

    public static void handleClient(final FlightStatus packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData data = FlightData.getData(player);
                data.hasFlight = packet.hasFlight;
            }
        });
    }

    public static void handleServer(final FlightStatus packet, final IPayloadContext context) {
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