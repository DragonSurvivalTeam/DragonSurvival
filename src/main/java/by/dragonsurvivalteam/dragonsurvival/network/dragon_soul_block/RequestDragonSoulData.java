package by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record RequestDragonSoulData(BlockPos position) implements CustomPacketPayload {
    public static final Type<RequestDragonSoulData> TYPE = new Type<>(DragonSurvival.res("request_dragon_soul_data"));

    public static final StreamCodec<FriendlyByteBuf, RequestDragonSoulData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BLOCK_POS, RequestDragonSoulData::position,
            RequestDragonSoulData::new
    );

    public static void handleServer(final RequestDragonSoulData packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                return soul.saveComponentData();
            }

            return new CompoundTag();
        }).thenAccept(data -> context.reply(new SyncDragonSoulData(packet.position(), data)));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
