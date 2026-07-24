package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SyncBeginCast(int playerId, int abilitySlot) implements CustomPacketPayload {
    public static final Type<SyncBeginCast> TYPE = new Type<>(DragonSurvival.res("sync_begin_cast"));

    public static final StreamCodec<FriendlyByteBuf, SyncBeginCast> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncBeginCast::playerId,
            ByteBufCodecs.VAR_INT, SyncBeginCast::abilitySlot,
            SyncBeginCast::new
    );

    public static void handleServer(final SyncBeginCast packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicData magic = MagicData.getData(context.player());

            // The server can deny the cast if the player doesn't meet the entity predicate for the casting
            if (!magic.attemptCast(context.player(), packet.abilitySlot())) {
                // Send this deny packet to all players involved, not just the caster, as we may need to stop ticking sounds
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), new SyncStopCast(context.player().getId(), Optional.empty()));
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}