package by.dragonsurvivalteam.dragonsurvival.network.container;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RequestOpenVanillaInventory() implements CustomPacketPayload {
    public static final Type<RequestOpenVanillaInventory> TYPE = new Type<>(DragonSurvival.res("open_vanilla_inventory"));

    public static final RequestOpenVanillaInventory INSTANCE = new RequestOpenVanillaInventory();
    public static final StreamCodec<ByteBuf, RequestOpenVanillaInventory> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handleServer(final RequestOpenVanillaInventory ignored, final IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().containerMenu.removed(context.player());

            if (context.player() instanceof ServerPlayer serverPlayer) {
                InventoryMenu inventory = context.player().inventoryMenu;
                serverPlayer.initMenu(inventory);
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}