package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncGrowth(int playerId, double growth) implements CustomPacketPayload {
    public static final Type<SyncGrowth> TYPE = new Type<>(DragonSurvival.res("sync_growth"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGrowth> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncGrowth::playerId,
            ByteBufCodecs.DOUBLE, SyncGrowth::growth,
            SyncGrowth::new
    );

    public static void handleClient(final SyncGrowth packet, final IPayloadContext context) {
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