package by.dragonsurvivalteam.dragonsurvival.network.claw;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonClawRender(int playerId, boolean shouldRender) implements CustomPacketPayload {
    public static final Type<SyncDragonClawRender> TYPE = new Type<>(DragonSurvival.res("sync_dragon_claw_render"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonClawRender> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDragonClawRender::playerId,
            ByteBufCodecs.BOOL, SyncDragonClawRender::shouldRender,
            SyncDragonClawRender::new
    );

    public static void handleClient(final SyncDragonClawRender packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ClawInventoryData.getData(player).shouldRenderClaws = packet.shouldRender();
            }
        });
    }

    public static void handleServer(final SyncDragonClawRender packet, final PayloadContext context) {
        if (ServerConfig.syncClawRender) {
            context.enqueueWork(() -> {
                if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                    ClawInventoryData.getData(player).shouldRenderClaws = packet.shouldRender();
                }
            }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), packet));
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}