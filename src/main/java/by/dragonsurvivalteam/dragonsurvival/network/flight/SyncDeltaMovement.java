package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDeltaMovement(int playerId, Vec3 movement) implements CustomPacketPayload {
    public static final Type<SyncDeltaMovement> TYPE = new Type<>(DragonSurvival.res("sync_delta_movement"));

    public static final StreamCodec<FriendlyByteBuf, SyncDeltaMovement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDeltaMovement::playerId,
            MiscCodecs.VEC3_STREAM_CODEC, SyncDeltaMovement::movement,
            SyncDeltaMovement::new
    );

    public static void handleClient(final SyncDeltaMovement packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Local player already has the correct values of themselves
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player && player != DragonSurvival.PROXY.getLocalPlayer()) {
                player.setDeltaMovement(packet.movement());
            }
        });
    }

    public static void handleServer(final SyncDeltaMovement packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                player.setDeltaMovement(packet.movement());
            }
        }).thenRun(() -> {
            // Update whoever is tracking this entity
            PacketDistributor.sendToPlayersTrackingEntity(context.player(), new SyncDeltaMovement(context.player().getId(), packet.movement()));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}