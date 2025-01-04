package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncResting(int playerId, boolean isResting) implements CustomPacketPayload {
    public static final Type<SyncResting> TYPE = new Type<>(DragonSurvival.res("interrupt_treasure_rest"));

    public static final StreamCodec<FriendlyByteBuf, SyncResting> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncResting::playerId,
            ByteBufCodecs.BOOL, SyncResting::isResting,
            SyncResting::new
    );

    public static void handleClient(final SyncResting packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                TreasureRestData.getData(player).setResting(packet.isResting());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}