package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.FlightEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
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
            FlightData flight = FlightData.getData(context.player());

            if (packet.activation() == Activation.JUMP && flight.areWingsSpread) {
                return Result.ALREADY_ENABLED;
            }

            if (!context.player().isCreative() && !ClientFlightHandler.hasEnoughFoodToStartFlight(context.player())) {
                return Result.NO_HUNGER;
            }

            MagicData magic = MagicData.getData(context.player());

            if (!magic.checkAbility(context.player(), FlightEffect.class, MagicData.AbilityCheck.HAS_EFFECT)) {
                return Result.NO_WINGS;
            }

            if (context.player().hasEffect(DSEffects.TRAPPED) || context.player().hasEffect(DSEffects.BROKEN_WINGS)) {
                return Result.WINGS_BLOCKED;
            }

            if (!magic.checkAbility(context.player(), FlightEffect.class, MagicData.AbilityCheck.IS_EFFECT_UNLOCKED)) {
                return Result.ALREADY_DISABLED;
            }

            flight.areWingsSpread = !flight.areWingsSpread;

            if (flight.areWingsSpread) {
                return Result.SUCCESS_ENABLED;
            } else {
                return Result.SUCCESS_DISABLED;
            }
        }).thenAccept(result -> PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ToggleFlight(packet.activation(), result)));
    }

    public enum Activation {
        MANUAL,
        JUMP
    }

    public enum Result {
        SUCCESS_ENABLED,
        SUCCESS_DISABLED,
        ALREADY_ENABLED,
        ALREADY_DISABLED,
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
