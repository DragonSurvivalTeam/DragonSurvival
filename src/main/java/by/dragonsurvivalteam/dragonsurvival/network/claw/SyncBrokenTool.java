package by.dragonsurvivalteam.dragonsurvival.network.claw;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncBrokenTool(int playerId, int slot) implements CustomPacketPayload {
    public static final Type<SyncBrokenTool> TYPE = new Type<>(DragonSurvival.res("sync_broken_tool"));

    public static final StreamCodec<FriendlyByteBuf, SyncBrokenTool> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncBrokenTool::playerId,
            ByteBufCodecs.INT, SyncBrokenTool::slot,
            SyncBrokenTool::new
    );

    public static void handleClient(final SyncBrokenTool packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ClawInventoryData data = ClawInventoryData.getData(player);

                if (data.switchedTool || data.switchedWeapon) {
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
