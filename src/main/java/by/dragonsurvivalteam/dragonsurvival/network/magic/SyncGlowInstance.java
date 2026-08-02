package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record SyncGlowInstance(int entityId, Glow.Instance instance, boolean isRemoval) implements CustomPacketPayload {
    public static final Type<SyncGlowInstance> TYPE = new Type<>(DragonSurvival.res("sync_glow"));

    public static final StreamCodec<FriendlyByteBuf, SyncGlowInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncGlowInstance::entityId,
            ByteBufCodecs.fromCodecWithRegistries(Glow.Instance.CODEC), SyncGlowInstance::instance,
            ByteBufCodecs.BOOL, SyncGlowInstance::isRemoval,
            SyncGlowInstance::new
    );

    public static void handleClient(final SyncGlowInstance packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            if (entity != null) {
                GlowData data = AttachmentManager.getData(entity, DSDataAttachments.GLOW);

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
