package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
            ByteBufCodecs.RESOURCE_LOCATION, SyncAddPenaltySupply::id,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::maximumSupply,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::reductionRateMultiplier,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::regenerationRate,
            ByteBufCodecs.FLOAT, SyncAddPenaltySupply::currentSupply,
            SyncAddPenaltySupply::new
    );

    public static void handleClient(final SyncAddPenaltySupply packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            PenaltySupply penaltySupply = AttachmentManager.getData(context.player(), DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.initialize(packet.id(), packet.maximumSupply(), packet.reductionRateMultiplier(), packet.regenerationRate(), packet.currentSupply(), /* Not relevant to the client */ 0);
        });
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
