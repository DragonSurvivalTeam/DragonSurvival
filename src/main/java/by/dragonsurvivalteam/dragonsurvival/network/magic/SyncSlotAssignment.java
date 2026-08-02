package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public record SyncSlotAssignment(ResourceKey<DragonAbility> abilityToMove, int newSlot) implements CustomPacketPayload {
    public static final Type<SyncSlotAssignment> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_slot_assignment"));

    public static final StreamCodec<FriendlyByteBuf, SyncSlotAssignment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.resourceKey(DragonAbility.REGISTRY), SyncSlotAssignment::abilityToMove,
            ByteBufCodecs.VAR_INT, SyncSlotAssignment::newSlot,
            SyncSlotAssignment::new
    );

    public static void handleServer(final SyncSlotAssignment packet, final PayloadContext context) {
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
