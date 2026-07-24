package by.dragonsurvivalteam.dragonsurvival.network.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record StopAllEmotes(int playerId) implements CustomPacketPayload {
    public static final Type<StopAllEmotes> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("stop_all_emotes"));

    public static final StreamCodec<FriendlyByteBuf, StopAllEmotes> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, StopAllEmotes::playerId,
            StopAllEmotes::new
    );

    public static void handleServer(final StopAllEmotes packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                PacketDistributor.sendToPlayersTrackingEntity(player, packet);
            }
        });
    }

    public static void handleClient(final StopAllEmotes packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonSurvival.PROXY.stopAllEmotes(player);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
