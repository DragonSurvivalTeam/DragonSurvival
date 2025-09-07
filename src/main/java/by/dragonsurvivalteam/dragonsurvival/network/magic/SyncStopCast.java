package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.animation.StopAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StopTickingSound;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncStopCast(int playerId, boolean forceWasApplyingEffects) implements CustomPacketPayload {
    public static final Type<SyncStopCast> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_stop_cast"));

    public static final StreamCodec<FriendlyByteBuf, SyncStopCast> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncStopCast::playerId,
            ByteBufCodecs.BOOL, SyncStopCast::forceWasApplyingEffects,
            SyncStopCast::new
    );

    public static void handleClient(final SyncStopCast packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                MagicData magic = MagicData.getData(player);

                if (packet.forceWasApplyingEffects()) {
                    magic.stopCasting(player, magic.getCurrentlyCasting(), true);
                } else {
                    magic.stopCasting(player);
                }
            }
        });
    }

    // Needed so we can reuse this logic in DragonAbilityInstance to properly handle sound effect/animation stopping logic
    public static void handleServer(final Player player) {
        MagicData data = MagicData.getData(player);
        DragonAbilityInstance currentlyCasting = data.getCurrentlyCasting();

        if (currentlyCasting == null) {
            return;
        }

        PacketDistributor.sendToPlayersTrackingEntity(player, new StopTickingSound(currentlyCasting.location().withSuffix(player.getStringUUID())));

        if (!currentlyCasting.isApplyingEffects() || (currentlyCasting.isApplyingEffects() && !currentlyCasting.hasEndAnimation())) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new StopAbilityAnimation(player.getId()));
        }

        data.stopCasting(player);
    }

    public static void handleServer(final SyncStopCast packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                handleServer(player);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
