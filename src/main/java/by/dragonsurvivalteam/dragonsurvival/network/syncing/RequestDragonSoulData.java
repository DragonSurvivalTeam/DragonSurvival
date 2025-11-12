package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RequestDragonSoulData(BlockPos position) implements CustomPacketPayload {
    public static final Type<RequestDragonSoulData> TYPE = new Type<>(DragonSurvival.res("request_dragon_soul_data"));

    public static final StreamCodec<FriendlyByteBuf, RequestDragonSoulData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RequestDragonSoulData::position,
            RequestDragonSoulData::new
    );

    public static void handleServer(final RequestDragonSoulData packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                return soul.components();
            }

            return DataComponentMap.EMPTY;
        }).thenAccept(map -> context.reply(new SyncDragonSoulData(packet.position(), DataComponentMap.CODEC.encodeStart(context.player().registryAccess().createSerializationContext(NbtOps.INSTANCE), map).getOrThrow())));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}