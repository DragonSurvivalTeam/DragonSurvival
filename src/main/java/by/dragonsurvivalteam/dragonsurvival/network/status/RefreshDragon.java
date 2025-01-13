package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RefreshDragon(int playerId) implements CustomPacketPayload {
    public static final Type<RefreshDragon> TYPE = new Type<>(DragonSurvival.res("refresh_dragon"));

    public static void handleClient(final RefreshDragon packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DragonEntity dragon = DSEntities.DRAGON.get().create(player.level());
                //noinspection DataFlowIssue -> dragon is present
                dragon.playerId = player.getId();
                ClientDragonRenderer.PLAYER_DRAGON_MAP.putIfAbsent(player.getId(), dragon);
            }
        });
    }

    public static final StreamCodec<FriendlyByteBuf, RefreshDragon> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RefreshDragon::playerId,
            RefreshDragon::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}