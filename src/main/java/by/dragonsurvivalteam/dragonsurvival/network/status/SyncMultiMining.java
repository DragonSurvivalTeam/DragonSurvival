package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMultiMining(DragonStateHandler.MultiMining multiMining) implements CustomPacketPayload {
    public static final Type<SyncMultiMining> TYPE = new Type<>(DragonSurvival.res("sync_multi_mining"));

    public static final StreamCodec<FriendlyByteBuf, SyncMultiMining> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(DragonStateHandler.MultiMining.class), SyncMultiMining::multiMining,
            SyncMultiMining::new
    );

    public static void handleServer(final SyncMultiMining packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateProvider.getData(context.player()).multiMining = packet.multiMining();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
