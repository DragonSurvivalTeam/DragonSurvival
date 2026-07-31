package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

// Since the client manually adjusts the mana as well it's probably safer to send the delta from the server
// Instead of overriding it completely, in case the server data is a couple of ticks older
public record SyncMana(float delta) implements CustomPacketPayload {
    public static final Type<SyncMana> TYPE = new Type<>(DragonSurvival.res("sync_mana"));

    public static final StreamCodec<FriendlyByteBuf, SyncMana> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncMana::delta,
            SyncMana::new
    );

    public static void handleClient(final SyncMana packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            MagicData magic = MagicData.getData(context.player());
            magic.setCurrentMana(context.player(), magic.getCurrentMana() + packet.delta());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
