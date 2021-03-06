package by.jackraidenph.dragonsurvival.nest;

import by.jackraidenph.dragonsurvival.handlers.Containers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class NestContainer extends Container {
    NestEntity nestEntity;

    public NestContainer(int windowId, PlayerInventory inv, PacketBuffer data) {
        super(Containers.nestContainer, windowId);
        nestEntity = (NestEntity) inv.player.world.getTileEntity(data.readBlockPos());
        int index = 0;
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, index++, 8 + 18 * i, 141));
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, index++, 8 + 18 * j, 84 + 18 * i));
            }
        }

        addSlot(new Slot(new Inventory(1), 0, 8 + 18 * 7, 85 - 24) {
            @Override
            public void onSlotChange(ItemStack oldStackIn, ItemStack newStackIn) {
                super.onSlotChange(oldStackIn, newStackIn);
            }
        });
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return nestEntity.ownerUUID.equals(playerIn.getUniqueID());
    }
}
