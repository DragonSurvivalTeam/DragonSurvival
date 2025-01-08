package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncRemovePenaltySupply(ResourceLocation id) implements CustomPacketPayload {
    public static final Type<SyncRemovePenaltySupply> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_remove_penalty_supply"));

    public static final StreamCodec<FriendlyByteBuf, SyncRemovePenaltySupply> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, SyncRemovePenaltySupply::id,
        SyncRemovePenaltySupply::new
    );

    public static void handleClient(final SyncRemovePenaltySupply packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            PenaltySupply penaltySupply = context.player().getData(DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.remove(packet.id());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
