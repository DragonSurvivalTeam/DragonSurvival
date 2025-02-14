package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public record OpenDragonAltar(List<Holder<DragonSpecies>> unlockedSpecies) implements CustomPacketPayload {
    public static final Type<OpenDragonAltar> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "open_dragon_altar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDragonAltar> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(DragonSpecies.REGISTRY).apply(ByteBufCodecs.list()), OpenDragonAltar::unlockedSpecies,
            OpenDragonAltar::new
    );

    public static void handleServer(final OpenDragonAltar ignored, final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        context.enqueueWork(() -> DragonSpecies.getUnlockedSpecies(serverPlayer))
                .thenAccept(unlockedSpecies -> PacketDistributor.sendToPlayer(serverPlayer, new OpenDragonAltar(unlockedSpecies)));
    }

    public static void handleClient(final OpenDragonAltar packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientProxy.openDragonAltar(packet.unlockedSpecies()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}