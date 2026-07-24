package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.SortingHandler;
import io.netty.buffer.ByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SortInventory() implements CustomPacketPayload {
    public static final Type<SortInventory> TYPE = new Type<>(DragonSurvival.res("sort_inventory"));

    public static final SortInventory INSTANCE = new SortInventory();
    public static final StreamCodec<ByteBuf, SortInventory> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handleServer(final SortInventory ignored, final PayloadContext context) {
        context.enqueueWork(() -> SortingHandler.sortInventory(context.player()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
