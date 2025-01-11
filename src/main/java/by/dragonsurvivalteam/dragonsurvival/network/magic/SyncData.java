package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public record SyncData(int targetEntityId, ResourceLocation attachmentType, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<SyncData> TYPE = new Type<>(DragonSurvival.res("sync_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncData::targetEntityId,
            ResourceLocation.STREAM_CODEC, SyncData::attachmentType,
            ByteBufCodecs.COMPOUND_TAG, SyncData::tag,
            SyncData::new
    );

    public static void handleClient(final SyncData packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                AttachmentType<?> type = NeoForgeRegistries.ATTACHMENT_TYPES.get(packet.attachmentType());

                if (type != null && context.player().level().getEntity(packet.targetEntityId()) instanceof Entity entity) {
                    //noinspection unchecked -> it's handled
                    INBTSerializable<CompoundTag> data = (INBTSerializable<CompoundTag>) entity.getData(type);
                    data.deserializeNBT(context.player().registryAccess(), packet.tag());
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
