package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSoulPlacement(boolean isEnabled) implements CustomPacketPayload {
    public static final Type<SyncDragonSoulPlacement> TYPE = new Type<>(DragonSurvival.res("sync_dragon_soul_placement"));

    public static final StreamCodec<ByteBuf, SyncDragonSoulPlacement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncDragonSoulPlacement::isEnabled,
            SyncDragonSoulPlacement::new
    );

    public static void handleServer(final SyncDragonSoulPlacement packet, final IPayloadContext context) {
        context.enqueueWork(() -> context.player().getData(DSDataAttachments.PLAYER_DATA).enabledDragonSoulPlacement = packet.isEnabled());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
