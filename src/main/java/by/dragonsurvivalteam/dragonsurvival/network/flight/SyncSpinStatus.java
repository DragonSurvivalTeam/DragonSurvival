package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import net.minecraft.core.HolderSet;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidType;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SyncSpinStatus(int playerId, boolean hasSpin, Optional<HolderSet<FluidType>> swimSpinFluid) implements CustomPacketPayload {
    public static final Type<SyncSpinStatus> TYPE = new Type<>(DragonSurvival.res("sync_spin_status"));

    public static final StreamCodec<FriendlyByteBuf, SyncSpinStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncSpinStatus::playerId,
            ByteBufCodecs.BOOL, SyncSpinStatus::hasSpin,
            ByteBufCodecs.optional(ByteBufCodecs.holderSet(ForgeRegistries.Keys.FLUID_TYPES)), SyncSpinStatus::swimSpinFluid,
            SyncSpinStatus::new
    );

    public static void handleClient(final SyncSpinStatus packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData spin = FlightData.getData(player);
                spin.hasSpin = packet.hasSpin();
                spin.inFluid = packet.swimSpinFluid().orElse(null);
                ClientFlightHandler.lastSync = player.tickCount;
            }
        });
    }

    public static void handleServer(final SyncSpinStatus packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                FlightData spin = FlightData.getData(player);
                spin.hasSpin = packet.hasSpin();
                spin.inFluid = packet.swimSpinFluid().orElse(null);
            }
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}