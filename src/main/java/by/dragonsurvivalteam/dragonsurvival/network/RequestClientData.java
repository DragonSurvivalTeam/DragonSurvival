package by.dragonsurvivalteam.dragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public record RequestClientData() implements CustomPacketPayload {
    public static final Type<RequestClientData> TYPE = new Type<>(DragonSurvival.res("request_client_data"));

    public static final RequestClientData INSTANCE = new RequestClientData();
    public static final StreamCodec<ByteBuf, RequestClientData> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handleClient(final RequestClientData ignored, final PayloadContext context) {
        context.reply(new SyncDragonClawRender(context.player().getId(), ClientDragonRenderer.renderDragonClaws));
        context.reply(new SyncDragonSkinSettings(context.player().getId(), ClientDragonRenderer.renderCustomSkin));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}