package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncAltarState(CompoundTag altarData) implements CustomPacketPayload {
    public static final Type<SyncAltarState> TYPE = new Type<>(DragonSurvival.res("sync_altar_state"));

    public static final StreamCodec<FriendlyByteBuf, SyncAltarState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SyncAltarState::altarData,
            SyncAltarState::new
    );

    public static void handleClient(final SyncAltarState message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            AltarData data = AltarData.getData(context.player());
            DSDataAttachments.deserializeFromCompoundTag(data, message.altarData(), context.player().registryAccess());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
