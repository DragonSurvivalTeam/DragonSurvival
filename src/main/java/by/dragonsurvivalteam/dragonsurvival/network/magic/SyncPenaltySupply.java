package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncPenaltySupply(CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<SyncPenaltySupply> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_penalty_supply"));

    public static final StreamCodec<FriendlyByteBuf, SyncPenaltySupply> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SyncPenaltySupply::nbt,
            SyncPenaltySupply::new
    );

    public static void handleClient(final SyncPenaltySupply packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            PenaltySupply penaltySupply = AttachmentManager.getData(context.player(), DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.deserializeNBT(context.player().level().registryAccess(), packet.nbt());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
