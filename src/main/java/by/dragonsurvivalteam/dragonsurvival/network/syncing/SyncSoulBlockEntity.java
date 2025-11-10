package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncSoulBlockEntity(BlockPos position, CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncSoulBlockEntity> TYPE = new Type<>(DragonSurvival.res("sync_soul_block_entity"));

    public static final StreamCodec<FriendlyByteBuf, SyncSoulBlockEntity> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncSoulBlockEntity::position,
            ByteBufCodecs.COMPOUND_TAG, SyncSoulBlockEntity::data,
            SyncSoulBlockEntity::new
    );

    public static void handleClient(final SyncSoulBlockEntity packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                soul.loadAdditional(packet.data(), context.player().level().registryAccess());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
