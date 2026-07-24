package by.dragonsurvivalteam.dragonsurvival.server.containers;

import by.dragonsurvivalteam.dragonsurvival.registry.DSContainers;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SourceOfMagicContainer extends AbstractContainerMenu {
    public final SourceOfMagicBlockEntity blockEntity;

    public SourceOfMagicContainer(int windowId, final Inventory inventory, final FriendlyByteBuf buffer) {
        super(DSContainers.SOURCE_OF_MAGIC_CONTAINER.value(), windowId);
        blockEntity = (SourceOfMagicBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos());
        int index = 0;

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inventory, index++, 8 + 18 * i, 142));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inventory, index++, 8 + 18 * j, 84 + 18 * i));
            }
        }

        addSlot(new SlotItemHandler(new InvWrapper(blockEntity), 0, 80, 62) {
            @Override
            public boolean mayPlace(@Nonnull final ItemStack stack) {
                return blockEntity.getDuration(stack.getItem()) > 0;
            }
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull final Player player, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex == 0) {
                if (!moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            } else if (pIndex >= 1 && pIndex < 28) {
                if (!moveItemStackTo(itemstack1, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (pIndex >= 28 && pIndex < 37) {
                if (!moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack1, 1, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}