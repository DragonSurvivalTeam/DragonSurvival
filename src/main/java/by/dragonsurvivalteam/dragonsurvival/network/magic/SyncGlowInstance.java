package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncGlowInstance(int entityId, Glow.Instance instance, boolean isRemoval) implements CustomPacketPayload {
    public static final Type<SyncGlowInstance> TYPE = new Type<>(DragonSurvival.res("sync_glow"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGlowInstance> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncGlowInstance::entityId,
            ByteBufCodecs.fromCodecWithRegistries(Glow.Instance.CODEC), SyncGlowInstance::instance,
            ByteBufCodecs.BOOL, SyncGlowInstance::isRemoval,
            SyncGlowInstance::new
    );

    public static void handleClient(final SyncGlowInstance packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.entityId()) instanceof Entity entity) {
                GlowData data = entity.getData(DSDataAttachments.GLOW);

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
