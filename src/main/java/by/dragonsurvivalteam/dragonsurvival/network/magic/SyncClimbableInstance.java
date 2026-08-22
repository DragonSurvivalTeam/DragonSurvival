package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Climbable;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncClimbableInstance(int playerId, Climbable.Instance instance, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncClimbableInstance> TYPE = new Type<>(DragonSurvival.res("sync_climbable_instance"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncClimbableInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncClimbableInstance::playerId,
            ByteBufCodecs.fromCodecWithRegistries(Climbable.Instance.CODEC), SyncClimbableInstance::instance,
            ByteBufCodecs.BOOL, SyncClimbableInstance::remove,
            SyncClimbableInstance::new
    );

    public static void handleClient(final SyncClimbableInstance packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ClimbableData data = player.getData(DSDataAttachments.CLIMBABLE_DATA);

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
