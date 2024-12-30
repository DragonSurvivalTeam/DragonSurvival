package by.dragonsurvivalteam.dragonsurvival.network.dragon_editor;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPlayerSkinPreset(int playerId, ResourceKey<DragonType> dragonType, CompoundTag preset) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPlayerSkinPreset> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_player_skin_preset"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerSkinPreset> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPlayerSkinPreset::playerId,
            ResourceKey.streamCodec(DragonType.REGISTRY), SyncPlayerSkinPreset::dragonType,
            ByteBufCodecs.COMPOUND_TAG, SyncPlayerSkinPreset::preset,
            SyncPlayerSkinPreset::new
    );

    public static void handleClient(final SyncPlayerSkinPreset message, final IPayloadContext context) {
        context.enqueueWork(() -> ClientProxy.handleSyncPlayerSkinPreset(message, context.player().registryAccess()));
    }

    public static void handleServer(final SyncPlayerSkinPreset message, final IPayloadContext context) {
        Player sender = context.player();

        context.enqueueWork(() -> {
            DragonStateProvider.getOptional(sender).ifPresent(handler -> {
                SkinPreset newPreset = new SkinPreset();
                newPreset.deserializeNBT(sender.registryAccess(), message.preset());
                handler.setSkinPresetForType(message.dragonType, newPreset);
            });
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(sender, message));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
