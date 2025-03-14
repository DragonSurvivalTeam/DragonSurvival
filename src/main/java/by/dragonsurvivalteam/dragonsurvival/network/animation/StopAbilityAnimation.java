package by.dragonsurvivalteam.dragonsurvival.network.animation;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


public record StopAbilityAnimation(int playerId) implements CustomPacketPayload {
    public static final Type<StopAbilityAnimation> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("stop_ability_animation"));

    public static final StreamCodec<FriendlyByteBuf, StopAbilityAnimation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StopAbilityAnimation::playerId,
            StopAbilityAnimation::new
    );

    public static void handleClient(final StopAbilityAnimation packet, final IPayloadContext context) {
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
