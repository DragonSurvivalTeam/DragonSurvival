package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMana(float currentMana) implements CustomPacketPayload {
    public static final Type<SyncMana> TYPE = new Type<>(DragonSurvival.res("sync_mana"));

    public static final StreamCodec<FriendlyByteBuf, SyncMana> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncMana::currentMana,
            SyncMana::new
    );

    public static void handleClient(final SyncMana packet, final PayloadContext context) {
        context.enqueueWork(() -> MagicData.getData(context.player()).setCurrentMana(packet.currentMana()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}