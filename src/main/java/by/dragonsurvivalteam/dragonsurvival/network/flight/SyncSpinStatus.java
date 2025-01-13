package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SyncSpinStatus(int playerId, boolean hasSpin, Optional<Holder<FluidType>> swimSpinFluid) implements CustomPacketPayload {
    public static final Type<SyncSpinStatus> TYPE = new Type<>(DragonSurvival.res("sync_spin_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSpinStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncSpinStatus::playerId,
            ByteBufCodecs.BOOL, SyncSpinStatus::hasSpin,
            ByteBufCodecs.optional(ByteBufCodecs.holderRegistry(NeoForgeRegistries.Keys.FLUID_TYPES)), SyncSpinStatus::swimSpinFluid,
            SyncSpinStatus::new
    );

    public static void handleClient(final SyncSpinStatus packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData spin = FlightData.getData(player);
                spin.hasSpin = packet.hasSpin();
                spin.swimSpinFluid = packet.swimSpinFluid().orElse(null);
                ClientFlightHandler.lastSync = player.tickCount;
            }
        });
    }

    public static void handleServer(final SyncSpinStatus packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData spin = FlightData.getData(player);
                spin.hasSpin = packet.hasSpin();
                spin.swimSpinFluid = packet.swimSpinFluid().orElse(null);
            }
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}