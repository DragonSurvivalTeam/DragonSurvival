package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonPassengerID(int playerId, int passengerId) implements CustomPacketPayload {
    public static final Type<SyncDragonPassengerID> TYPE = new Type<>(DragonSurvival.res("sync_dragon_passenger_id"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonPassengerID> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDragonPassengerID::playerId,
            ByteBufCodecs.VAR_INT, SyncDragonPassengerID::passengerId,
            SyncDragonPassengerID::new
    );

    public static void handleClient(final SyncDragonPassengerID packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonStateProvider.getData(player).setPassengerId(packet.passengerId());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
