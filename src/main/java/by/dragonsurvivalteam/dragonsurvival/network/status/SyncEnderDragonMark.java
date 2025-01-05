package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncEnderDragonMark(boolean isMarked) implements CustomPacketPayload {
    public static final Type<SyncEnderDragonMark> TYPE = new Type<>(DragonSurvival.res("ender_dragon_mark"));

    public static final StreamCodec<FriendlyByteBuf, SyncEnderDragonMark> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncEnderDragonMark::isMarked,
            SyncEnderDragonMark::new
    );

    public static void handleClient(final SyncEnderDragonMark packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateProvider.getData(context.player()).markedByEnderDragon = packet.isMarked();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}