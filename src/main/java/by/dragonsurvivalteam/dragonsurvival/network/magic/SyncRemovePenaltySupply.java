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

public record SyncRemovePenaltySupply(ResourceLocation id) implements CustomPacketPayload {
    public static final Type<SyncRemovePenaltySupply> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_remove_penalty_supply"));

    public static final StreamCodec<FriendlyByteBuf, SyncRemovePenaltySupply> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.RESOURCE_LOCATION, SyncRemovePenaltySupply::id,
            SyncRemovePenaltySupply::new
    );

    public static void handleClient(final SyncRemovePenaltySupply packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            PenaltySupply penaltySupply = AttachmentManager.getData(context.player(), DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.remove(packet.id());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
