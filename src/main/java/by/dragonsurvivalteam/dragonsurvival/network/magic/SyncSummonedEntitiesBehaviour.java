package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncSummonedEntitiesBehaviour(SummonedEntities.AttackBehaviour attackBehaviour, SummonedEntities.MovementBehaviour movementBehaviour) implements CustomPacketPayload {
    public static final Type<SyncSummonedEntitiesBehaviour> TYPE = new Type<>(DragonSurvival.res("sync_summoned_entities_behaviour"));

    public static final StreamCodec<FriendlyByteBuf, SyncSummonedEntitiesBehaviour> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(SummonedEntities.AttackBehaviour.class), SyncSummonedEntitiesBehaviour::attackBehaviour,
            NeoForgeStreamCodecs.enumCodec(SummonedEntities.MovementBehaviour.class), SyncSummonedEntitiesBehaviour::movementBehaviour,
            SyncSummonedEntitiesBehaviour::new
    );

    public static void handleServer(final SyncSummonedEntitiesBehaviour packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            SummonedEntities data = context.player().getData(DSDataAttachments.SUMMONED_ENTITIES);
            data.attackBehaviour = packet.attackBehaviour();
            data.movementBehaviour = packet.movementBehaviour();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
