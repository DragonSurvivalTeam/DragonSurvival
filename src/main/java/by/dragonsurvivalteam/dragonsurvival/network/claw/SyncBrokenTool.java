package by.dragonsurvivalteam.dragonsurvival.network.claw;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record SyncBrokenTool(int playerId, int slot) implements CustomPacketPayload {
    public static final Type<SyncBrokenTool> TYPE = new Type<>(DragonSurvival.res("sync_broken_tool"));

    public static final StreamCodec<FriendlyByteBuf, SyncBrokenTool> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncBrokenTool::playerId,
            ByteBufCodecs.INT, SyncBrokenTool::slot,
            SyncBrokenTool::new
    );

    public static void handleClient(final SyncBrokenTool packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ClawInventoryData data = ClawInventoryData.getData(player);

                if (data.switchedTool) {
                    player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    data.getContainer().setItem(packet.slot, ItemStack.EMPTY);
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
