package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record SyncMountingBonePosition(int mountId, Vec3 offset) implements CustomPacketPayload {
    private static final double MAX_OFFSET = 64;

    public static final Type<SyncMountingBonePosition> TYPE = new Type<>(DragonSurvival.res("sync_mounting_bone_position"));

    public static final StreamCodec<FriendlyByteBuf, SyncMountingBonePosition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncMountingBonePosition::mountId,
            MiscCodecs.VEC3_STREAM_CODEC, SyncMountingBonePosition::offset,
            SyncMountingBonePosition::new
    );

    public static void handleServer(final SyncMountingBonePosition packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();
            Player mount;
            Player rider;
            boolean sentByMount;

            if (sender.getId() == packet.mountId()) {
                mount = sender;
                Entity passenger = mount.getFirstPassenger();

                if (!(passenger instanceof Player playerPassenger)) {
                    return;
                }

                rider = playerPassenger;
                sentByMount = true;
            } else if (sender.getVehicle() instanceof Player vehicle && vehicle.getId() == packet.mountId()) {
                mount = vehicle;
                rider = sender;
                sentByMount = false;
            } else {
                return;
            }

            if (rider.getVehicle() != mount || !mount.hasPassenger(rider)) {
                return;
            }

            DragonStateHandler handler = DragonStateProvider.getData(mount);
            boolean trackedPassenger = handler.getPassengerId() == rider.getId();
            boolean untrackedMountPassenger = sentByMount && handler.getPassengerId() == DragonRidingHandler.NO_PASSENGER;

            if (!handler.isDragon()
                    || (!trackedPassenger && !untrackedMountPassenger)
                    || handler.body().value().noDragonModelRendering()
                    || handler.body().value().mountingOffsets().isPresent()
                    || !isValidOffset(packet.offset())) {
                return;
            }

            handler.updateMountingBoneOffset(packet.offset(), mount.level().getGameTime(), sentByMount);
        });
    }

    static boolean isValidOffset(final Vec3 offset) {
        return Double.isFinite(offset.x)
                && Double.isFinite(offset.y)
                && Double.isFinite(offset.z)
                && offset.lengthSqr() <= MAX_OFFSET * MAX_OFFSET;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
