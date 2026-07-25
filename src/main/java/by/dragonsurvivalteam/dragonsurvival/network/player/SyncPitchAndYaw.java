package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

// While this data is being sent over to the server every client tick, it is only used when players begin tracking or change dimensions
// This is because otherwise we wouldn't know the facing direction of dragon players and maintain them correctly on login or during a dimension transition
public record SyncPitchAndYaw(int playerId, double headYaw, double headPitch, double bodyYaw) implements  CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPitchAndYaw> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_pitch_and_yaw"));

    public static final StreamCodec<FriendlyByteBuf, SyncPitchAndYaw> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPitchAndYaw::playerId,
            ByteBufCodecs.DOUBLE, SyncPitchAndYaw::headYaw,
            ByteBufCodecs.DOUBLE, SyncPitchAndYaw::headPitch,
            ByteBufCodecs.DOUBLE, SyncPitchAndYaw::bodyYaw,
            SyncPitchAndYaw::new
    );

    public static void handleClient(final SyncPitchAndYaw packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                MovementData movementData = AttachmentManager.getData(player, DSDataAttachments.MOVEMENT);
                movementData.headYaw = packet.headYaw();
                movementData.headPitch = packet.headPitch();
                movementData.bodyYaw = packet.bodyYaw();
            }
        });
    }

    public static void handleServer(final SyncPitchAndYaw packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                MovementData movementData = AttachmentManager.getData(player, DSDataAttachments.MOVEMENT);
                movementData.headYaw = packet.headYaw();
                movementData.headPitch = packet.headPitch();
                movementData.bodyYaw = packet.bodyYaw();
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
