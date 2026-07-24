package by.dragonsurvivalteam.dragonsurvival.network.dragon_editor;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncPlayerSkinPreset(int playerId, ResourceKey<DragonSpecies> dragonSpecies, CompoundTag preset) implements CustomPacketPayload {
    public static final Type<SyncPlayerSkinPreset> TYPE = new Type<>(DragonSurvival.res("sync_player_skin_preset"));

    public static final StreamCodec<FriendlyByteBuf, SyncPlayerSkinPreset> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPlayerSkinPreset::playerId,
            ByteBufCodecs.resourceKey(DragonSpecies.REGISTRY), SyncPlayerSkinPreset::dragonSpecies,
            ByteBufCodecs.COMPOUND_TAG, SyncPlayerSkinPreset::preset,
            SyncPlayerSkinPreset::new
    );

    public static void handleClient(final SyncPlayerSkinPreset packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                SkinPreset newPreset = new SkinPreset();
                newPreset.deserializeNBT(player.registryAccess(), packet.preset());

                DragonStateHandler handler = DragonStateProvider.getData(player);
                handler.setSkinPresetForType(packet.dragonSpecies(), newPreset);
                handler.recompileCurrentSkin();
            }
        });
    }

    public static void handleServer(final SyncPlayerSkinPreset packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                SkinPreset newPreset = new SkinPreset();
                newPreset.deserializeNBT(player.registryAccess(), packet.preset());
                DragonStateProvider.getData(player).setSkinPresetForType(packet.dragonSpecies(), newPreset);
            }
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
