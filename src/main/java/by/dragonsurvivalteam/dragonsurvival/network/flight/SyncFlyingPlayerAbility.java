package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncFlyingPlayerAbility(int playerId, boolean state) implements CustomPacketPayload {
    public static final Type<SyncFlyingPlayerAbility> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "sync_flying_player_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncFlyingPlayerAbility> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncFlyingPlayerAbility::playerId,
            ByteBufCodecs.BOOL, SyncFlyingPlayerAbility::state,
            SyncFlyingPlayerAbility::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final SyncFlyingPlayerAbility packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                player.getAbilities().flying = packet.state();
            }
        });
    }
}
