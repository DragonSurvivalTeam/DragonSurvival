package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SyncComplete implements IMessage<SyncComplete.Data> {
    public static void handleClient(final Data message, final IPayloadContext context) {
        Entity entity = context.player().level().getEntity(message.playerId);

        if (entity instanceof Player player) {
            context.enqueueWork(() -> {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                handler.deserializeNBT(player.registryAccess(), message.nbt);
                player.refreshDimensions();
            });
        }
    }

    public static void handleDragonSync(final ServerPlayer player, boolean refreshMagicData) {
        DSModifiers.updateAllModifiers(player);
        player.refreshDimensions();

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (refreshMagicData) {
            handler.refreshMagicData(player, true);
        }

        if (handler.isDragon()) {
            DSAdvancementTriggers.BE_DRAGON.get().trigger(player);
        }
    }

    public static void handleServer(final Data message, final IPayloadContext context) {
        context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();

                    DragonStateHandler handler = DragonStateProvider.getData(player);
                    Holder<DragonSpecies> previousType = handler.species();
                    handler.deserializeNBT(player.registryAccess(), message.nbt);
                    handleDragonSync(player, false);

                    if (!handler.isDragon()) {
                        PenaltySupply.getData(player).clear();
                        DSModifiers.clearModifiers(player);
                        handler.refreshMagicData(player, false);
                        return;
                    }

                    // When we are processing a complete sync on the server, the client has requested it. This happens in two cases:
                    // 1. When the player changes dragon species in the dragon selection screen
                    // 2. When the player reverts to human in the dragon altar screen
                    // In both of these cases, we want to make sure to refresh the magic data and penalty supply if the server isn't set to save it

                    if (previousType == null || !previousType.is(handler.species())) {
                        handler.refreshMagicData(player, false);
                    }
                })
                .thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), message))
                .thenAccept(v -> context.reply(RequestClientData.INSTANCE));
    }

    public record Data(int playerId, CompoundTag nbt) implements CustomPacketPayload {
        public static final Type<Data> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "complete_data"));

        public static final StreamCodec<FriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::playerId,
                ByteBufCodecs.COMPOUND_TAG, Data::nbt,
                Data::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}