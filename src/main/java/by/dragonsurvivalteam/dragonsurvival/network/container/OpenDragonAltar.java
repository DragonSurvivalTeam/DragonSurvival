package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.UnlockableBehavior;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record OpenDragonAltar(List<UnlockableBehavior.SpeciesEntry> entries) implements CustomPacketPayload {
    public static final Type<OpenDragonAltar> TYPE = new Type<>(DragonSurvival.res("open_dragon_altar"));

    public static final StreamCodec<FriendlyByteBuf, OpenDragonAltar> STREAM_CODEC = StreamCodec.composite(
            UnlockableBehavior.SpeciesEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenDragonAltar::entries,
            OpenDragonAltar::new
    );

    public static void handleServer(final OpenDragonAltar ignored, final PayloadContext context) {
        context.enqueueWork(() -> DragonSpecies.getSpecies((ServerPlayer) context.player(), true))
                .thenAccept(unlockedSpecies -> PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new OpenDragonAltar(unlockedSpecies)));
    }

    public static void handleClient(final OpenDragonAltar packet, final PayloadContext context) {
        context.enqueueWork(() -> ClientProxy.openDragonAltar(packet.entries()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
