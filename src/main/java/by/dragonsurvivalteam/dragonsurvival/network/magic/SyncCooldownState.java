package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncCooldownState(int playerId, int slot, int cooldown) implements CustomPacketPayload {
    public static final Type<SyncCooldownState> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_cooldown_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncCooldownState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncCooldownState::playerId,
            ByteBufCodecs.VAR_INT, SyncCooldownState::slot,
            ByteBufCodecs.VAR_INT, SyncCooldownState::cooldown,
            SyncCooldownState::new
    );

    public static void handleClient(final SyncCooldownState packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId) instanceof Player player) {
                MagicData.getData(player).setClientCooldown(player, packet.slot(), packet.cooldown());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
