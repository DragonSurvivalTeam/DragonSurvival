package by.dragonsurvivalteam.dragonsurvival.network.dragon_editor;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDragonSkinSettings(int playerId, boolean renderCustomSkin) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncDragonSkinSettings> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_dragon_skin_settings"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonSkinSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDragonSkinSettings::playerId,
            ByteBufCodecs.BOOL, SyncDragonSkinSettings::renderCustomSkin,
            SyncDragonSkinSettings::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final SyncDragonSkinSettings packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.playerId());
            DragonStateProvider.getOptional(entity).ifPresent(handler -> handler.getSkinData().renderCustomSkin = packet.renderCustomSkin());
        });
    }

    public static void handleServer(final SyncDragonSkinSettings packet, final IPayloadContext context) {
        Player sender = context.player();

        context.enqueueWork(() -> DragonStateProvider.getOptional(sender).ifPresent(handler -> handler.getSkinData().renderCustomSkin = packet.renderCustomSkin())
        ).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(sender, packet));
    }
}