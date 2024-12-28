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

/** Synchronizes size */
public record SyncSize(int playerId, double size) implements CustomPacketPayload {
    public static final Type<SyncSize> TYPE = new Type<>(DragonSurvival.res("sync_size"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSize> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncSize::playerId,
            ByteBufCodecs.DOUBLE, SyncSize::size,
            SyncSize::new
    );

    public static void handleClient(final SyncSize packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonStateHandler data = DragonStateProvider.getData(player);
                data.setSize(player, packet.size());
                player.refreshDimensions();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}