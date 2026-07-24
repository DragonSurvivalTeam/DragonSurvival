package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncPenaltySupplyAmount(ResourceLocation supplyType, float amount) implements CustomPacketPayload {
    public static final Type<SyncPenaltySupplyAmount> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_penalty_supply_amount"));

    public static final StreamCodec<FriendlyByteBuf, SyncPenaltySupplyAmount> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SyncPenaltySupplyAmount::supplyType,
            ByteBufCodecs.FLOAT, SyncPenaltySupplyAmount::amount,
            SyncPenaltySupplyAmount::new
    );

    public static void handleClient(final SyncPenaltySupplyAmount packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            PenaltySupply penaltySupply = context.player().getData(DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.setSupply(packet.supplyType(), packet.amount());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
