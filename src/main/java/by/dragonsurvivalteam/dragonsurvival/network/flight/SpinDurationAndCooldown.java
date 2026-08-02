package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SpinDurationAndCooldown(int playerId, int duration, int cooldown) implements CustomPacketPayload {
    public static final Type<SpinDurationAndCooldown> TYPE = new Type<>(DragonSurvival.res("spin_duration_and_cooldown"));

    public static final StreamCodec<FriendlyByteBuf, SpinDurationAndCooldown> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SpinDurationAndCooldown::playerId,
            ByteBufCodecs.INT, SpinDurationAndCooldown::duration,
            ByteBufCodecs.INT, SpinDurationAndCooldown::cooldown,
            SpinDurationAndCooldown::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final SpinDurationAndCooldown packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData spin = FlightData.getData(player);
                spin.cooldown = packet.cooldown();
                spin.duration = packet.duration();
            }
        });
    }

    public static void handleServer(final SpinDurationAndCooldown packet, final PayloadContext context) {
        Player sender = context.player();

        context.enqueueWork(() -> {
            FlightData spin = FlightData.getData(sender);
            spin.cooldown = packet.cooldown();
            spin.duration = packet.duration();
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(sender, packet));
    }
}
