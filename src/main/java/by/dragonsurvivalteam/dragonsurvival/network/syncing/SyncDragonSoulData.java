package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSoulData(BlockPos position, Tag data) implements CustomPacketPayload {
    public static final Type<SyncDragonSoulData> TYPE = new Type<>(DragonSurvival.res("sync_dragon_soul_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonSoulData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncDragonSoulData::position,
            ByteBufCodecs.TAG, SyncDragonSoulData::data,
            SyncDragonSoulData::new
    );

    public static void handleClient(final SyncDragonSoulData packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                soul.setComponents(DataComponentMap.CODEC.decode(context.player().registryAccess().createSerializationContext(NbtOps.INSTANCE), packet.data()).getOrThrow().getFirst());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}