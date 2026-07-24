package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncSummonedEntity(SummonEntityEffect.Instance instance, boolean isRemoval) implements CustomPacketPayload {
    public static final Type<SyncSummonedEntity> TYPE = new Type<>(DragonSurvival.res("sync_summoned_entity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSummonedEntity> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistries(SummonEntityEffect.Instance.CODEC), SyncSummonedEntity::instance,
            ByteBufCodecs.BOOL, SyncSummonedEntity::isRemoval,
            SyncSummonedEntity::new
    );

    public static void handleClient(final SyncSummonedEntity packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            SummonedEntities data = context.player().getData(DSDataAttachments.SUMMONED_ENTITIES);

            if (packet.isRemoval()) {
                data.remove(context.player(), packet.instance());
            } else {
                data.add(context.player(), packet.instance());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
