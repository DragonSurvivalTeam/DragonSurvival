package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncMagicData(CompoundTag magicData) implements CustomPacketPayload {
    public static final Type<SyncMagicData> TYPE = new Type<>(DragonSurvival.res("sync_magic_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncMagicData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SyncMagicData::magicData,
            SyncMagicData::new
    );

    public static void handleClient(final SyncMagicData packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, context.player().registryAccess(), packet.magicData());
            MagicData.getData(context.player()).deserialize(valueInput);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}