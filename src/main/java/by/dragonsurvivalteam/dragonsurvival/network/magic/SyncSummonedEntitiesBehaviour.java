package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SyncSummonedEntitiesBehaviour(SummonedEntities.AttackBehaviour attackBehaviour, SummonedEntities.MovementBehaviour movementBehaviour) implements CustomPacketPayload {
    public static final Type<SyncSummonedEntitiesBehaviour> TYPE = new Type<>(DragonSurvival.res("sync_summoned_entities_behaviour"));

    public static final StreamCodec<FriendlyByteBuf, SyncSummonedEntitiesBehaviour> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.enumCodec(SummonedEntities.AttackBehaviour.class), SyncSummonedEntitiesBehaviour::attackBehaviour,
            ByteBufCodecs.enumCodec(SummonedEntities.MovementBehaviour.class), SyncSummonedEntitiesBehaviour::movementBehaviour,
            SyncSummonedEntitiesBehaviour::new
    );

    public static void handleServer(final SyncSummonedEntitiesBehaviour packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            SummonedEntities summonData = context.player().getData(DSDataAttachments.SUMMONED_ENTITIES);
            summonData.attackBehaviour = packet.attackBehaviour();
            summonData.movementBehaviour = packet.movementBehaviour();

            if (context.player().level() instanceof ServerLevel serverLevel) {
                summonData.all().forEach(instance -> {
                    for (UUID uuid : instance.entityUUIDs()) {
                        Entity entity = serverLevel.getEntity(uuid);

                        if (entity == null) {
                            continue;
                        }

                        SummonData data = entity.getData(DSDataAttachments.SUMMON);
                        data.attackBehaviour = packet.attackBehaviour();
                        data.movementBehaviour = packet.movementBehaviour();

                        if (data.attackBehaviour == SummonedEntities.AttackBehaviour.PASSIVE && entity instanceof Mob mob) {
                            mob.setTarget(null);
                        }
                    }
                });
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
