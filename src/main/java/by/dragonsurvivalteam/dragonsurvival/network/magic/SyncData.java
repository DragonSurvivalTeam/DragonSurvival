package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.compat.attachments.AttachmentType;
import by.dragonsurvivalteam.dragonsurvival.common.serialization.INBTSerializable;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record SyncData(int targetEntityId, ResourceLocation attachmentType, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<SyncData> TYPE = new Type<>(DragonSurvival.res("sync_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncData::targetEntityId,
            ByteBufCodecs.RESOURCE_LOCATION, SyncData::attachmentType,
            ByteBufCodecs.COMPOUND_TAG, SyncData::tag,
            SyncData::new
    );

    public static void handleCommon(final SyncData packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            try {
                AttachmentType<?> type = DSDataAttachments.ATTACHMENT_TYPES.get().getValue(packet.attachmentType());
                Entity entity = context.player().level().getEntity(packet.targetEntityId());

                if (type != null && entity != null) {
                    //noinspection unchecked -> it's handled
                    INBTSerializable<CompoundTag> data = (INBTSerializable<CompoundTag>) AttachmentManager.getData(entity, type);
                    data.deserializeNBT(context.player().level().registryAccess(), packet.tag());
                    return;
                }
            } catch (ClassCastException ignored) { /* Nothing to do */ }

            Functions.logOrThrow("Unable to deserialize data [" + packet + "]");
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
