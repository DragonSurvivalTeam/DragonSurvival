package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PhasingData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncPhasingInstance(int entityId, Phasing.Instance instance, boolean isRemoval) implements CustomPacketPayload {
    public static final Type<SyncPhasingInstance> TYPE = new Type<>(DragonSurvival.res("sync_phasing"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPhasingInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPhasingInstance::entityId,
            ByteBufCodecs.fromCodecWithRegistries(Phasing.Instance.CODEC), SyncPhasingInstance::instance,
            ByteBufCodecs.BOOL, SyncPhasingInstance::isRemoval,
            SyncPhasingInstance::new
    );

    public static void handleClient(final SyncPhasingInstance packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.entityId()) instanceof Entity entity) {
                PhasingData data = entity.getData(DSDataAttachments.PHASING);

                if (packet.isRemoval()) {
                    data.remove(entity, packet.instance());
                } else {
                    data.add(entity, packet.instance());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
