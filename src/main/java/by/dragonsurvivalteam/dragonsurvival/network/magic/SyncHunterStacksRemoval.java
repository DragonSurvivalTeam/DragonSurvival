package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncHunterStacksRemoval(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncHunterStacksRemoval> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_hunter_stacks_removal"));
    public static final StreamCodec<ByteBuf, SyncHunterStacksRemoval> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncHunterStacksRemoval::entityId,
            SyncHunterStacksRemoval::new
    );

    public static void handleClient(final SyncHunterStacksRemoval packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId());

            if (entity != null) {
                entity.getData(DSDataAttachments.HUNTER).clearHunterStacks();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
