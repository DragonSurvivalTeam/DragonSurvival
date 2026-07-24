package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

/** Jump animation length is 20.8 ticks */
public record SyncPlayerJump(int playerId, boolean state) implements CustomPacketPayload {
    public static final Type<SyncPlayerJump> TYPE = new Type<>(DragonSurvival.res("sync_player_jump"));

    public static final StreamCodec<FriendlyByteBuf, SyncPlayerJump> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPlayerJump::playerId,
            ByteBufCodecs.BOOL, SyncPlayerJump::state,
            SyncPlayerJump::new
    );

    public static void handleClient(final SyncPlayerJump packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player) {
                DragonEntity.DRAGONS_JUMPING.put(packet.playerId(), packet.state());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}