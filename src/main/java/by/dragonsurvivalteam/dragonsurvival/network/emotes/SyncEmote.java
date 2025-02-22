package by.dragonsurvivalteam.dragonsurvival.network.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncEmote(int playerId, DragonEmote emote, boolean stop) implements CustomPacketPayload {
    public static final Type<SyncEmote> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_emote"));

    public static final StreamCodec<FriendlyByteBuf, SyncEmote> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncEmote::playerId,
            DragonEmote.STREAM_CODEC, SyncEmote::emote,
            ByteBufCodecs.BOOL, SyncEmote::stop,
            SyncEmote::new
    );

    public static void handleServer(final SyncEmote packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                PacketDistributor.sendToPlayersTrackingEntity(player, packet);
            }
        });
    }

    public static void handleClient(final SyncEmote packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                if (!packet.stop) {
                    DragonSurvival.PROXY.beginPlayingEmote(player.getId(), packet.emote);
                } else {
                    DragonSurvival.PROXY.stopEmote(player.getId(), packet.emote);
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
