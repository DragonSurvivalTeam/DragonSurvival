package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncLargeDragonDestruction(DragonStateHandler.LargeDragonDestruction largeDragonDestruction) implements CustomPacketPayload {
    public static final Type<SyncLargeDragonDestruction> TYPE = new Type<>(DragonSurvival.res("sync_large_dragon_destruction"));

    public static final StreamCodec<FriendlyByteBuf, SyncLargeDragonDestruction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.enumCodec(DragonStateHandler.LargeDragonDestruction.class), SyncLargeDragonDestruction::largeDragonDestruction,
            SyncLargeDragonDestruction::new
    );

    public static void handleServer(final SyncLargeDragonDestruction packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateProvider.getData(context.player()).largeDragonDestruction = packet.largeDragonDestruction();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
