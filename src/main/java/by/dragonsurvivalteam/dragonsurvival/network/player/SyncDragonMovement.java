package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonMovement(int playerId, boolean isFirstPerson, boolean bite, boolean dig, boolean isFreeLook, Vec3 movement) implements CustomPacketPayload {
    public static final Type<SyncDragonMovement> TYPE = new Type<>(DragonSurvival.res("sync_dragon_movement"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonMovement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDragonMovement::playerId,
            ByteBufCodecs.BOOL, SyncDragonMovement::isFirstPerson,
            ByteBufCodecs.BOOL, SyncDragonMovement::bite,
            ByteBufCodecs.BOOL, SyncDragonMovement::dig,
            ByteBufCodecs.BOOL, SyncDragonMovement::isFreeLook,
            MiscCodecs.VEC3_STREAM_CODEC, SyncDragonMovement::movement,
            SyncDragonMovement::new
    );

    public static void handleClient(final SyncDragonMovement packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player && player != DragonSurvival.PROXY.getLocalPlayer()) {
                // Local player already has the correct values
                handle(packet, player);
            }
        });
    }

    public static void handleServer(final SyncDragonMovement packet, final IPayloadContext context) {
        context.enqueueWork(() -> handle(packet, context.player()))
                .thenRun(() -> PacketDistributor.sendToPlayersTrackingEntity(context.player(), packet));
    }

    private static void handle(final SyncDragonMovement packet, final Player player) {
        MovementData data = MovementData.getData(player);
        data.setFirstPerson(packet.isFirstPerson());
        data.setBite(packet.bite());
        data.setDig(packet.dig());
        data.setFreeLook(packet.isFreeLook());
        data.setDesiredMoveVec(packet.movement());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}