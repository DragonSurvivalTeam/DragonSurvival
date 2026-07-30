package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMagicData(CompoundTag magicData) implements CustomPacketPayload {
    public static final Type<SyncMagicData> TYPE = new Type<>(DragonSurvival.res("sync_magic_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncMagicData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SyncMagicData::magicData,
            SyncMagicData::new
    );

    public static void handleClient(final SyncMagicData packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            MagicData magic = MagicData.getData(context.player());
            magic.deserializeNBT(context.player().level().registryAccess(), packet.magicData());
            magic.validateHotbar();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
