package by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSoulLock(BlockPos position, Boolean locked) implements CustomPacketPayload {
    public static final Type<SyncDragonSoulLock> TYPE = new Type<>(DragonSurvival.res("sync_dragon_soul_lock"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonSoulLock> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncDragonSoulLock::position,
            ByteBufCodecs.BOOL, SyncDragonSoulLock::locked,
            SyncDragonSoulLock::new
    );

    public static void handleClient(final SyncDragonSoulLock packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                soul.locked = packet.locked();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}