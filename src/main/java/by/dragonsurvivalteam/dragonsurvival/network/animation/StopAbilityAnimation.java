package by.dragonsurvivalteam.dragonsurvival.network.animation;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;


public record StopAbilityAnimation(int playerId) implements CustomPacketPayload {
    public static final Type<StopAbilityAnimation> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("stop_ability_animation"));

    public static final StreamCodec<FriendlyByteBuf, StopAbilityAnimation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StopAbilityAnimation::playerId,
            StopAbilityAnimation::new
    );

    public static void handleClient(final StopAbilityAnimation packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonSurvival.PROXY.setCurrentAbilityAnimation(player, null);
            }
        });
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
