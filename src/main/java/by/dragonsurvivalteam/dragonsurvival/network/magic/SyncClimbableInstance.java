package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Climbable;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SyncClimbableInstance(int playerId, Climbable.Instance instance, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncClimbableInstance> TYPE = new Type<>(DragonSurvival.res("sync_climbable_instance"));

    public static final StreamCodec<FriendlyByteBuf, SyncClimbableInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncClimbableInstance::playerId,
            ByteBufCodecs.fromCodecWithRegistries(Climbable.Instance.CODEC), SyncClimbableInstance::instance,
            ByteBufCodecs.BOOL, SyncClimbableInstance::remove,
            SyncClimbableInstance::new
    );

    public static void handleClient(final SyncClimbableInstance packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null && context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ClimbableData data = AttachmentManager.getData(player, DSDataAttachments.CLIMBABLE_DATA);

                if (packet.remove()) {
                    // The stored server and client ids (used to remove the effects) may be different,
                    // Therefore, we retrieve the actual client instance and remove that, not the encoded server instance
                    data.remove(player, data.get(packet.instance().id()));
                } else {
                    data.add(player, packet.instance());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
