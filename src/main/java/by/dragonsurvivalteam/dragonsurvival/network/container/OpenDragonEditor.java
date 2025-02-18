package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.UnlockableBehavior;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record OpenDragonEditor(ResourceKey<DragonSpecies> species, List<UnlockableBehavior.BodyEntry> entries, boolean fromAltar) implements CustomPacketPayload {
    public static final Type<OpenDragonEditor> TYPE = new Type<>(DragonSurvival.res("open_dragon_editor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDragonEditor> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(DragonSpecies.REGISTRY), OpenDragonEditor::species,
            UnlockableBehavior.BodyEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenDragonEditor::entries,
            ByteBufCodecs.BOOL, OpenDragonEditor::fromAltar,
            OpenDragonEditor::new
    );

    public static void handleServer(final OpenDragonEditor packet, final IPayloadContext context) {
        context.enqueueWork(() -> DragonBody.getBodies((ServerPlayer) context.player(), true))
                .thenAccept(unlockedBodies -> PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new OpenDragonEditor(packet.species(), unlockedBodies, packet.fromAltar())));
    }

    public static void handleClient(final OpenDragonEditor packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientProxy.openDragonEditor(packet.entries(), packet.species(), packet.fromAltar()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}