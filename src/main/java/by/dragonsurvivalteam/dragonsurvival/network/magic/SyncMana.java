package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMana(int playerId, float mana) implements CustomPacketPayload {
    public static final Type<SyncMana> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_mana"));

    public static final StreamCodec<FriendlyByteBuf, SyncMana> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SyncMana::playerId,
        ByteBufCodecs.FLOAT, SyncMana::mana,
        SyncMana::new
    );

    public static void handleClient(final SyncMana packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId) instanceof Player player) {
                MagicData.getData(player).setCurrentMana(packet.mana());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
