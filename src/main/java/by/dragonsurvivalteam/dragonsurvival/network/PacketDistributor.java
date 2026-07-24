package by.dragonsurvivalteam.dragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility facade for the payload-oriented distributor helpers used in 1.21.1.
 */
public final class PacketDistributor {
    private PacketDistributor() {}

    public static void sendToPlayer(final ServerPlayer player, final CustomPacketPayload payload) {
        NetworkHandler.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToPlayersInDimension(final ServerLevel level, final CustomPacketPayload payload) {
        NetworkHandler.send(net.minecraftforge.network.PacketDistributor.DIMENSION.with(level::dimension), payload);
    }

    public static void sendToPlayersNear(
            final ServerLevel level,
            final @Nullable ServerPlayer excluded,
            final double x,
            final double y,
            final double z,
            final double radius,
            final CustomPacketPayload payload
    ) {
        net.minecraftforge.network.PacketDistributor.TargetPoint point =
                new net.minecraftforge.network.PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension());
        NetworkHandler.send(net.minecraftforge.network.PacketDistributor.NEAR.with(() -> point), payload);
    }

    public static void sendToAllPlayers(final CustomPacketPayload payload) {
        NetworkHandler.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), payload);
    }

    public static void sendToServer(final CustomPacketPayload payload) {
        NetworkHandler.sendToServer(payload);
    }

    public static void sendToPlayersTrackingEntity(final Entity entity, final CustomPacketPayload payload) {
        NetworkHandler.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> entity), payload);
    }

    public static void sendToPlayersTrackingEntityAndSelf(final Entity entity, final CustomPacketPayload payload) {
        NetworkHandler.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }
}
