package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncWingIcon(ResourceLocation icon) implements CustomPacketPayload {
    public static final Type<SyncWingIcon> TYPE = new Type<>(DragonSurvival.res("sync_wing_icon"));

    public static final StreamCodec<ByteBuf, SyncWingIcon> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SyncWingIcon::icon,
            SyncWingIcon::new
    );

    public static void handleClient(final SyncWingIcon packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            FlightData.getData(context.player()).icon = packet.icon();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
