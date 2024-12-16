package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncAddPenaltySupply(String id, float maximumSupply, float reductionRateMultiplier, float regenerationRate) implements CustomPacketPayload {
    public static final Type<SyncAddPenaltySupply> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_add_penalty_supply"));

    public static final StreamCodec<FriendlyByteBuf, SyncAddPenaltySupply> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, SyncAddPenaltySupply::id,
        ByteBufCodecs.FLOAT, SyncAddPenaltySupply::maximumSupply,
        ByteBufCodecs.FLOAT, SyncAddPenaltySupply::reductionRateMultiplier,
        ByteBufCodecs.FLOAT, SyncAddPenaltySupply::regenerationRate,
        SyncAddPenaltySupply::new
    );

    public static void handleClient(final SyncAddPenaltySupply packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            PenaltySupply penaltySupply = context.player().getData(DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.initialize(packet.id(), packet.maximumSupply(), packet.reductionRateMultiplier(), packet.regenerationRate());
        });
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
