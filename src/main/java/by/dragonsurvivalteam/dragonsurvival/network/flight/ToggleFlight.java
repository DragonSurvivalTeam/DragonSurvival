package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.FlightEffect;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ToggleFlight(Activation activation, Result result) implements CustomPacketPayload {
    public static final Type<ToggleFlight> TYPE = new Type<>(DragonSurvival.res("toggle_flight"));

    public static final StreamCodec<FriendlyByteBuf, ToggleFlight> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Activation.class), ToggleFlight::activation,
            NeoForgeStreamCodecs.enumCodec(Result.class), ToggleFlight::result,
            ToggleFlight::new
    );

    public static void handleClient(final ToggleFlight packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientFlightHandler.handleToggleResult(packet.activation(), packet.result()));
    }

    public static void handleServer(final ToggleFlight packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            FlightData flight = FlightData.getData(player);
            MagicData magic = MagicData.getData(player);

            if (!magic.checkAbility(player, FlightEffect.class, MagicData.AbilityCheck.HAS_EFFECT)) {
                return Result.NO_WINGS;
            }

            if (packet.activation() == Activation.JUMP && flight.areWingsSpread) {
                return Result.ALREADY_ENABLED;
            }

            if (!flight.areWingsSpread) {
                // Attempting to open wings

                if (!player.isCreative() && !hasEnoughFoodToStartFlight(player)) {
                    return Result.NO_HUNGER;
                }

                if (player.hasEffect(DSEffects.TRAPPED) || player.hasEffect(DSEffects.BROKEN_WINGS)) {
                    return Result.WINGS_BLOCKED;
                }

                if (!magic.checkAbility(player, FlightEffect.class, MagicData.AbilityCheck.IS_EFFECT_UNLOCKED)) {
                    return Result.DISABLED;
                }
            }

            flight.areWingsSpread = !flight.areWingsSpread;
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncWingsSpread(player.getId(), flight.areWingsSpread));

            if (flight.areWingsSpread) {
                return Result.SUCCESS_ENABLED;
            } else {
                return Result.SUCCESS_DISABLED;
            }
        }).thenAccept(result -> PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ToggleFlight(packet.activation(), result)));
    }

    public static boolean hasEnoughFoodToStartFlight(final Player player) {
        return player.getFoodData().getFoodLevel() > ServerFlightHandler.flightHungerThreshold;
    }

    public enum Activation {
        MANUAL,
        JUMP
    }

    public enum Result {
        SUCCESS_ENABLED,
        SUCCESS_DISABLED,
        ALREADY_ENABLED,
        DISABLED,
        WINGS_BLOCKED,
        NO_WINGS,
        NO_HUNGER,
        NONE
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
