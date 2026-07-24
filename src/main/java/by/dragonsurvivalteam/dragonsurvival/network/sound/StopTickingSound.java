package by.dragonsurvivalteam.dragonsurvival.network.sound;

import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record StopTickingSound(ResourceLocation id) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StopTickingSound> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("stop_ticking_sound"));

    public static final StreamCodec<FriendlyByteBuf, StopTickingSound> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.RESOURCE_LOCATION, StopTickingSound::id,
            StopTickingSound::new
    );

    public static void handleClient(final StopTickingSound packet, final IPayloadContext context) {
        context.enqueueWork(() -> DragonSurvival.PROXY.stopTickingSound(packet.id()));
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
