package by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSoulLock(BlockPos position, Boolean locked) implements CustomPacketPayload {
    public static final Type<SyncDragonSoulLock> TYPE = new Type<>(DragonSurvival.res("sync_dragon_soul_lock"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonSoulLock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BLOCK_POS, SyncDragonSoulLock::position,
            ByteBufCodecs.BOOL, SyncDragonSoulLock::locked,
            SyncDragonSoulLock::new
    );

    public static void handleClient(final SyncDragonSoulLock packet, final PayloadContext context) {
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