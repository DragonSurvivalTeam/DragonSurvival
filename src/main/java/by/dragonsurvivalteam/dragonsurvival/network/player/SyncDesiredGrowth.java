package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDesiredGrowth(int playerId, double desiredGrowth) implements CustomPacketPayload {
    public static final Type<SyncDesiredGrowth> TYPE = new Type<>(DragonSurvival.res("sync_desired_growth"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDesiredGrowth> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDesiredGrowth::playerId,
            ByteBufCodecs.DOUBLE, SyncDesiredGrowth::desiredGrowth,
            SyncDesiredGrowth::new
    );

    public static void handleClient(final SyncDesiredGrowth packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonStateHandler data = DragonStateProvider.getData(player);
                data.setDesiredGrowth(player, packet.desiredGrowth());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
