package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public record OpenDragonAltar() implements CustomPacketPayload {
    public static final Type<OpenDragonAltar> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "open_dragon_altar"));

    public static final OpenDragonAltar INSTANCE = new OpenDragonAltar();
    public static final StreamCodec<ByteBuf, OpenDragonAltar> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handleClient(final OpenDragonAltar ignored, final IPayloadContext context) {
        context.enqueueWork(ClientProxy::openDragonAltar);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}