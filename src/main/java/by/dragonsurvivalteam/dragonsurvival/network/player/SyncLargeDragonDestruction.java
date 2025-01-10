package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncLargeDragonDestruction(DragonStateHandler.LargeDragonDestruction largeDragonDestruction) implements CustomPacketPayload {
    public static final Type<SyncLargeDragonDestruction> TYPE = new Type<>(DragonSurvival.res("sync_large_dragon_destruction"));

    public static final StreamCodec<FriendlyByteBuf, SyncLargeDragonDestruction> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(DragonStateHandler.LargeDragonDestruction.class), SyncLargeDragonDestruction::largeDragonDestruction,
            SyncLargeDragonDestruction::new
    );

    public static void handleServer(final SyncLargeDragonDestruction packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateProvider.getData(context.player()).largeDragonDestruction = packet.largeDragonDestruction();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}