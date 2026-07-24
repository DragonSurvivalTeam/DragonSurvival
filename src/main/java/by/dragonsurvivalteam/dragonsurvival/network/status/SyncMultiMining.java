package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMultiMining(DragonStateHandler.MultiMining multiMining) implements CustomPacketPayload {
    public static final Type<SyncMultiMining> TYPE = new Type<>(DragonSurvival.res("sync_multi_mining"));

    public static final StreamCodec<FriendlyByteBuf, SyncMultiMining> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.enumCodec(DragonStateHandler.MultiMining.class), SyncMultiMining::multiMining,
            SyncMultiMining::new
    );

    public static void handleServer(final SyncMultiMining packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateProvider.getData(context.player()).multiMining = packet.multiMining();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
