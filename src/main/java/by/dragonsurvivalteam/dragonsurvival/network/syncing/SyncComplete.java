package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncComplete(int playerId, CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncComplete> TYPE = new Type<>(DragonSurvival.res("sync_complete"));

    public static final StreamCodec<FriendlyByteBuf, SyncComplete> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncComplete::playerId,
            ByteBufCodecs.COMPOUND_TAG, SyncComplete::data,
            SyncComplete::new
    );

    public static void handleDragonSync(final ServerPlayer player, boolean refreshMagicData) {
        DSModifiers.updateAllModifiers(player);
        player.refreshDimensions();

        DragonStateHandler handler = DragonStateProvider.getData(player);
        handler.setGrowth(player, handler.getGrowth(), true);

        if (refreshMagicData) {
            handler.refreshMagicData(player, true);
        } else {
            // The server gets more ticks while joining the world than the client, causing the mana to desync when logging in
            PacketDistributor.sendToPlayer(player, new SyncMana(MagicData.getData(player).getCurrentMana(), true));
        }

        if (handler.isDragon()) {
            DSAdvancementTriggers.BE_DRAGON.get().trigger(player);
        }
    }

    public static void handleClient(final SyncComplete packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                handler.deserializeNBT(player.level().registryAccess(), packet.data());
                player.refreshDimensions();
            }
        });
    }

    public static void handleServer(final SyncComplete packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            DragonStateHandler handler = DragonStateProvider.getData(player);
            Holder<DragonSpecies> previousType = handler.species();
            handler.deserializeNBT(player.level().registryAccess(), packet.data());
            handleDragonSync(player, false);

            if (!handler.isDragon()) {
                PenaltySupply.clear(player);
                DSModifiers.clearModifiers(player);
                handler.refreshMagicData(player, false);
                return;
            }

            // When we are processing a complete sync on the server, the client has requested it. This happens in two cases:
            // 1. When the player changes dragon species in the dragon selection screen
            // 2. When the player reverts to human in the dragon altar screen
            // In both of these cases, we want to make sure to refresh the magic data and penalty supply if the server isn't set to save it

            if (previousType == null || !previousType.value().equals(handler.species().value())) {
                PenaltySupply.clear(player);
                handler.refreshMagicData(player, false);
            }
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), packet)).thenAccept(ignored -> context.reply(RequestClientData.INSTANCE));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
