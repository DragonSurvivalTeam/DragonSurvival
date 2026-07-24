package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncFlyingPlayerAbility(int playerId, boolean state) implements CustomPacketPayload {
    public static final Type<SyncFlyingPlayerAbility> TYPE = new Type<>(DragonSurvival.res("sync_flying_player_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncFlyingPlayerAbility> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncFlyingPlayerAbility::playerId,
            ByteBufCodecs.BOOL, SyncFlyingPlayerAbility::state,
            SyncFlyingPlayerAbility::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final SyncFlyingPlayerAbility packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                player.getAbilities().flying = packet.state();
            }
        });
    }
}
