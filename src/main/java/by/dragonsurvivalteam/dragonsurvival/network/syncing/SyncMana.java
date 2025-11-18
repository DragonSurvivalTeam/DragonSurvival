package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMana(float currentMana) implements CustomPacketPayload {
    public static final Type<SyncMana> TYPE = new Type<>(DragonSurvival.res("sync_mana"));

    public static final StreamCodec<FriendlyByteBuf, SyncMana> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncMana::currentMana,
            SyncMana::new
    );

    public static void handleClient(final SyncMana packet, final IPayloadContext context) {
        context.enqueueWork(() -> MagicData.getData(context.player()).setCurrentMana(packet.currentMana()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}