package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** Jump animation length is 20.8 ticks */
public record SyncPlayerJump(int playerId, int ticks) implements CustomPacketPayload {
    public static final Type<SyncPlayerJump> TYPE = new Type<>(DragonSurvival.res("sync_player_jump"));

    public static final StreamCodec<FriendlyByteBuf, SyncPlayerJump> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPlayerJump::playerId,
            ByteBufCodecs.VAR_INT, SyncPlayerJump::ticks,
            SyncPlayerJump::new
    );

    public static void handleClient(final SyncPlayerJump packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonEntity.DRAGON_JUMP_TICKS.put(player.getId(), packet.ticks());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}