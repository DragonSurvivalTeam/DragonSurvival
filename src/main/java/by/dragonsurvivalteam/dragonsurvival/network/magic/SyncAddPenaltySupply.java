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

public record SyncAddPenaltySupply(
        ResourceLocation id,
        float maximumSupply,
        float reductionRateMultiplier,
        float regenerationRate,
        float currentSupply
) implements CustomPacketPayload {
    public static final Type<SyncAddPenaltySupply> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_add_penalty_supply"));

    public static final StreamCodec<FriendlyByteBuf, SyncAddPenaltySupply> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SyncAddPenaltySupply::id,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::maximumSupply,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::reductionRateMultiplier,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::regenerationRate,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::currentSupply,
            SyncAddPenaltySupply::new
    );

    public static void handleClient(final SyncAddPenaltySupply packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            PenaltySupply penaltySupply = context.player().getData(DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.initialize(packet.id(), packet.maximumSupply(), packet.reductionRateMultiplier(), packet.regenerationRate(), packet.currentSupply(), /* Not relevant to the client */ 0);
        });
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
