package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

// Since the client manually adjusts the mana as well it's probably safer to send the delta from the server
// Instead of overriding it completely, in case the server data is a couple of ticks older
public record SyncMana(float amount, boolean fullSync) implements CustomPacketPayload {
    public static final Type<SyncMana> TYPE = new Type<>(DragonSurvival.res("sync_mana"));

    public static final StreamCodec<FriendlyByteBuf, SyncMana> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncMana::amount,
            ByteBufCodecs.BOOL, SyncMana::fullSync,
            SyncMana::new
    );

    public static void handleClient(final SyncMana packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicData magic = MagicData.getData(context.player());

            if (packet.fullSync()) {
                magic.setCurrentMana(context.player(), packet.amount());
            } else {
                magic.adjustMana(context.player(), packet.amount());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}