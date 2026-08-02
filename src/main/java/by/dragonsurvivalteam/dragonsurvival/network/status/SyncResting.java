package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SyncResting(int playerId, boolean isResting) implements CustomPacketPayload {
    public static final Type<SyncResting> TYPE = new Type<>(DragonSurvival.res("interrupt_treasure_rest"));

    public static final StreamCodec<FriendlyByteBuf, SyncResting> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncResting::playerId,
            ByteBufCodecs.BOOL, SyncResting::isResting,
            SyncResting::new
    );

    public static void handleClient(final SyncResting packet, final PayloadContext context) {
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