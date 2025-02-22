package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncSlotAssignment(ResourceKey<DragonAbility> abilityToMove, int newSlot) implements CustomPacketPayload {
    public static final Type<SyncSlotAssignment> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_slot_assignment"));

    public static final StreamCodec<FriendlyByteBuf, SyncSlotAssignment> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(DragonAbility.REGISTRY), SyncSlotAssignment::abilityToMove,
            ByteBufCodecs.VAR_INT, SyncSlotAssignment::newSlot,
            SyncSlotAssignment::new
    );

    public static void handleServer(final SyncSlotAssignment packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicData data = MagicData.getData(context.player());
            data.moveAbilityToSlot(packet.abilityToMove(), packet.newSlot());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
