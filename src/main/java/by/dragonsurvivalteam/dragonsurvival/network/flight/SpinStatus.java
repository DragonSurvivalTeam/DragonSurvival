package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public record SpinStatus(int playerId, boolean hasSpin, Optional<ResourceKey<FluidType>> swimSpinFluid) implements CustomPacketPayload {
    public static final Type<SpinStatus> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "spin_status"));

    public static final StreamCodec<FriendlyByteBuf, SpinStatus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SpinStatus::playerId,
            ByteBufCodecs.BOOL, SpinStatus::hasSpin,
            ByteBufCodecs.optional(ResourceKey.streamCodec(NeoForgeRegistries.FLUID_TYPES.key())), SpinStatus::swimSpinFluid,
            SpinStatus::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final SpinStatus packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientProxy.handleSyncSpinStatus(packet));
    }

    public static void handleServer(final SpinStatus packet, final IPayloadContext context) {
        Player sender = context.player();

        context.enqueueWork(() -> {
                FlightData spin = FlightData.getData(sender);
                spin.hasSpin = packet.hasSpin();
                if(packet.swimSpinFluid().isPresent()) {
                    spin.swimSpinFluid = Optional.of(sender.registryAccess().holderOrThrow(packet.swimSpinFluid().get()));
                } else {
                    spin.swimSpinFluid = Optional.empty();
                }
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(sender, packet));
    }
}