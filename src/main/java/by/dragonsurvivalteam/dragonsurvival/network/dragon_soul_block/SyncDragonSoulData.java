package by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSoulData(BlockPos position, CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncDragonSoulData> TYPE = new Type<>(DragonSurvival.res("sync_dragon_soul_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonSoulData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BLOCK_POS, SyncDragonSoulData::position,
            ByteBufCodecs.COMPOUND_TAG, SyncDragonSoulData::data,
            SyncDragonSoulData::new
    );

    public static void handleClient(final SyncDragonSoulData packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                soul.loadComponentData(packet.data());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
