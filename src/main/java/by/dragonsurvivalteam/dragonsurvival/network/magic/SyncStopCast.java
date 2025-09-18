package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.animation.StopAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StopTickingSound;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record SyncStopCast(int playerId, Optional<ResourceKey<DragonAbility>> ability) implements CustomPacketPayload {
    public static final Type<SyncStopCast> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_stop_cast"));

    public static final StreamCodec<FriendlyByteBuf, SyncStopCast> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncStopCast::playerId,
            ByteBufCodecs.optional(ResourceKey.streamCodec(DragonAbility.REGISTRY)), SyncStopCast::ability,
            SyncStopCast::new
    );

    public static void handleClient(final SyncStopCast packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                MagicData magic = MagicData.getData(player);
                packet.ability().ifPresentOrElse(ability -> magic.stopCasting(player, magic.getAbility(ability)), () -> magic.stopCasting(player));
            }
        });
    }

    // Needed so we can reuse this logic in DragonAbilityInstance to properly handle sound effect/animation stopping logic
    public static void handleServer(final Player player, @Nullable final ResourceKey<DragonAbility> ability) {
        MagicData data = MagicData.getData(player);
        DragonAbilityInstance abilityInstance;

        if (ability == null) {
            abilityInstance = data.getCurrentlyCasting();
        } else {
            abilityInstance = data.getAbility(ability);
        }

        if (abilityInstance == null) {
            return;
        }

        PacketDistributor.sendToPlayersTrackingEntity(player, new StopTickingSound(abilityInstance.location().withSuffix(player.getStringUUID())));

        if (!abilityInstance.isApplyingEffects() || (abilityInstance.isApplyingEffects() && !abilityInstance.hasEndAnimation())) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new StopAbilityAnimation(player.getId()));
        }

        if (ability == null) {
            data.stopCasting(player);
        } else {
            data.stopCasting(player, data.getAbility(ability));
        }
    }

    public static void handleServer(final SyncStopCast packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                packet.ability().ifPresentOrElse(ability -> handleServer(player, ability), () -> handleServer(player, null));
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
