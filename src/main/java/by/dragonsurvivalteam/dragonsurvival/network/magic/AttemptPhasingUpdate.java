package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PhasingData;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AttemptPhasingUpdate(String resourceLocation, int x, int y, int z) implements CustomPacketPayload {
    public static final Type<AttemptPhasingUpdate> TYPE = new Type<>(DragonSurvival.res("attempt_phasing_update"));

    public static final StreamCodec<FriendlyByteBuf, AttemptPhasingUpdate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AttemptPhasingUpdate::resourceLocation,
            ByteBufCodecs.VAR_INT, AttemptPhasingUpdate::x,
            ByteBufCodecs.VAR_INT, AttemptPhasingUpdate::y,
            ByteBufCodecs.VAR_INT, AttemptPhasingUpdate::z,
            AttemptPhasingUpdate::new
    );

    public static void handleClient(final AttemptPhasingUpdate packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            PhasingData data = context.player().getData(DSDataAttachments.PHASING);
            ResourceLocation instance_id = ResourceLocation.parse(packet.resourceLocation());
            Phasing.Instance instance = data.get(instance_id);
            if (instance == null) {
                return;
            }
            BlockPos location = BlockPosHelper.get(packet.x(), packet.y(), packet.z());
            instance.addToCache(location);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
