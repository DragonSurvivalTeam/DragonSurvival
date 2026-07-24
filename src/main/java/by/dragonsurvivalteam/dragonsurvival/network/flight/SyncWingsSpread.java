package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncWingsSpread(int playerId, boolean areWingsSpread) implements CustomPacketPayload {
    public static final Type<SyncWingsSpread> TYPE = new Type<>(DragonSurvival.res("sync_wings_spread"));

    public static final StreamCodec<FriendlyByteBuf, SyncWingsSpread> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncWingsSpread::playerId,
            ByteBufCodecs.BOOL, SyncWingsSpread::areWingsSpread,
            SyncWingsSpread::new
    );

    public static void handleClient(final SyncWingsSpread packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData.getData(player).areWingsSpread = packet.areWingsSpread();
            }
        });
    }

    public static void handleServer(final SyncWingsSpread packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData.getData(player).areWingsSpread = packet.areWingsSpread();
            }
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntity(context.player(), packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}