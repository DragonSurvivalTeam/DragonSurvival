package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.BlockVisionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncBlockVision(int playerId, BlockVision.Instance blockVisionInstance, boolean isRemoval) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncBlockVision> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_block_vision"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBlockVision> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncBlockVision::playerId,
            ByteBufCodecs.fromCodecWithRegistries(BlockVision.Instance.CODEC), SyncBlockVision::blockVisionInstance,
            ByteBufCodecs.BOOL, SyncBlockVision::isRemoval,
            SyncBlockVision::new
    );

    public static void handleClient(final SyncBlockVision packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                BlockVisionData data = player.getData(DSDataAttachments.BLOCK_VISION);

                if (packet.isRemoval()) {
                    data.remove(player, packet.blockVisionInstance());
                } else {
                    data.add(player, packet.blockVisionInstance());
                }
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
