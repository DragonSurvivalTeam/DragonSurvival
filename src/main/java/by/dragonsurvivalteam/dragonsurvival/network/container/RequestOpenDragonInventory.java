package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.SimpleMenuProvider;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record RequestOpenDragonInventory() implements CustomPacketPayload {
    public static final Type<RequestOpenDragonInventory> TYPE = new Type<>(DragonSurvival.res("open_dragon_inventory"));

    public static final RequestOpenDragonInventory INSTANCE = new RequestOpenDragonInventory();
    public static final StreamCodec<ByteBuf, RequestOpenDragonInventory> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handleServer(final RequestOpenDragonInventory ignored, final PayloadContext context) {
        context.enqueueWork(() -> {
            DragonStateHandler handler = DragonStateProvider.getData(context.player());

            if (handler.isDragon()) {
                context.player().containerMenu.removed(context.player());
                context.player().openMenu(new SimpleMenuProvider((containerId, inventory, player) -> new DragonContainer(containerId, inventory), Component.empty()));
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}