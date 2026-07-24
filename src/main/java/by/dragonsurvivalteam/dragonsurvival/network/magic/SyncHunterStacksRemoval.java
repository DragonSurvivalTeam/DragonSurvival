package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import io.netty.buffer.ByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncHunterStacksRemoval(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncHunterStacksRemoval> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_hunter_stacks_removal"));
    public static final StreamCodec<ByteBuf, SyncHunterStacksRemoval> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncHunterStacksRemoval::entityId,
            SyncHunterStacksRemoval::new
    );

    public static void handleClient(final SyncHunterStacksRemoval packet, final PayloadContext context) {
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
