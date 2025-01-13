package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncGrowthState(boolean isGrowing) implements CustomPacketPayload {
    public static final Type<SyncGrowthState> TYPE = new Type<>(DragonSurvival.res("sync_growth_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncGrowthState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncGrowthState::isGrowing,
            SyncGrowthState::new
    );

    public static void handleClient(final SyncGrowthState packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateProvider.getData(context.player()).isGrowing = packet.isGrowing();
        });
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}