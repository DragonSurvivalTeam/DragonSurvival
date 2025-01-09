package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MultiblockMiningToggled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMultiblockMiningEnabled(boolean enabled) implements CustomPacketPayload {
    public static final Type<SyncMultiblockMiningEnabled> TYPE = new Type<>(DragonSurvival.res("multiblock_mining_enabled"));

    public static final StreamCodec<FriendlyByteBuf, SyncMultiblockMiningEnabled> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncMultiblockMiningEnabled::enabled,
            SyncMultiblockMiningEnabled::new
    );

    public static void handleServer(final SyncMultiblockMiningEnabled packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MultiblockMiningToggled data = context.player().getData(DSDataAttachments.MULTIBLOCK_MINING_TOGGLED);
            data.enabled = packet.enabled();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
