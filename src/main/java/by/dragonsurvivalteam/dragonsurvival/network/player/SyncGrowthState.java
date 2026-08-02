package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncGrowthState(boolean isGrowing) implements CustomPacketPayload {
    public static final Type<SyncGrowthState> TYPE = new Type<>(DragonSurvival.res("sync_growth_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncGrowthState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncGrowthState::isGrowing,
            SyncGrowthState::new
    );

    public static void handleClient(final SyncGrowthState packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateProvider.getData(context.player()).isGrowing = packet.isGrowing();
        });
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}