package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncWingIcon(ResourceLocation icon) implements CustomPacketPayload {
    public static final Type<SyncWingIcon> TYPE = new Type<>(DragonSurvival.res("sync_wing_icon"));

    public static final StreamCodec<ByteBuf, SyncWingIcon> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.RESOURCE_LOCATION, SyncWingIcon::icon,
            SyncWingIcon::new
    );

    public static void handleClient(final SyncWingIcon packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            FlightData.getData(context.player()).icon = packet.icon();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
