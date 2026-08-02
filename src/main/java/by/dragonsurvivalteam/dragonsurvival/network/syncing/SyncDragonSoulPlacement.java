package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSoulPlacement(boolean isEnabled) implements CustomPacketPayload {
    public static final Type<SyncDragonSoulPlacement> TYPE = new Type<>(DragonSurvival.res("sync_dragon_soul_placement"));

    public static final StreamCodec<ByteBuf, SyncDragonSoulPlacement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncDragonSoulPlacement::isEnabled,
            SyncDragonSoulPlacement::new
    );

    public static void handleServer(final SyncDragonSoulPlacement packet, final PayloadContext context) {
        context.enqueueWork(() -> AttachmentManager.getData(context.player(), DSDataAttachments.PLAYER_DATA).enabledDragonSoulPlacement = packet.isEnabled());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
