package by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSoulAnimation(BlockPos position, String animation) implements CustomPacketPayload {
    public static final Type<SyncDragonSoulAnimation> TYPE = new Type<>(DragonSurvival.res("sync_dragon_soul_animation"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonSoulAnimation> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncDragonSoulAnimation::position,
            ByteBufCodecs.STRING_UTF8, SyncDragonSoulAnimation::animation,
            SyncDragonSoulAnimation::new
    );

    public static void handleServer(final SyncDragonSoulAnimation packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                soul.animation = packet.animation();
            }
        }).thenRun(() -> PacketDistributor.sendToPlayersInDimension(((ServerLevel) context.player().level()), packet));
    }

    public static void handleClient(final SyncDragonSoulAnimation packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.position()) instanceof DragonSoulBlockEntity soul) {
                soul.animation = packet.animation();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}