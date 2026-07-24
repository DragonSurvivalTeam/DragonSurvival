package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncGrowth(int playerId, double growth) implements CustomPacketPayload {
    public static final Type<SyncGrowth> TYPE = new Type<>(DragonSurvival.res("sync_growth"));

    public static final StreamCodec<FriendlyByteBuf, SyncGrowth> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncGrowth::playerId,
            ByteBufCodecs.DOUBLE, SyncGrowth::growth,
            SyncGrowth::new
    );

    public static void handleClient(final SyncGrowth packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonStateProvider.getData(player).setGrowth(player, packet.growth());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}