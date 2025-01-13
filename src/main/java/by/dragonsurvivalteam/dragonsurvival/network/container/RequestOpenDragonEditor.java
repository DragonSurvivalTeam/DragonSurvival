package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RequestOpenDragonEditor() implements CustomPacketPayload {
    public static final Type<RequestOpenDragonEditor> TYPE = new Type<>(DragonSurvival.res("open_dragon_editor"));

    public static final RequestOpenDragonEditor INSTANCE = new RequestOpenDragonEditor();
    public static final StreamCodec<ByteBuf, RequestOpenDragonEditor> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handleClient(final RequestOpenDragonEditor ignored, final IPayloadContext context) {
        context.enqueueWork(ClientProxy::openDragonEditor);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}