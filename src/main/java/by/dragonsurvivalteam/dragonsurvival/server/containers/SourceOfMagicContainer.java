package by.dragonsurvivalteam.dragonsurvival.server.containers;

import by.dragonsurvivalteam.dragonsurvival.registry.DSContainers;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicTileEntity;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class SourceOfMagicContainer extends AbstractContainerMenu{
	public SourceOfMagicTileEntity nestEntity;

	public SourceOfMagicContainer(int windowId, Inventory inv, FriendlyByteBuf data){
		super(DSContainers.SOURCE_OF_MAGIC_CONTAINER.value(), windowId);
		nestEntity = (SourceOfMagicTileEntity)inv.player.level().getBlockEntity(data.readBlockPos());
		int index = 0;
		for(int i = 0; i < 9; i++){
			addSlot(new Slot(inv, index++, 8 + 18 * i, 142));
		}
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 9; j++){
				addSlot(new Slot(inv, index++, 8 + 18 * j, 84 + 18 * i));
			}
		}

		addSlot(new SlotItemHandler(new InvWrapper(nestEntity), 0, 80, 62){
			@Override
			public boolean mayPlace(
				@Nonnull
					ItemStack stack){
				return SourceOfMagicTileEntity.consumables.containsKey(stack.getItem());
			}
		});
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex){
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = slots.get(pIndex);
		if(slot.hasItem()){
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if(pIndex == 0){
				if(!moveItemStackTo(itemstack1, 1, 37, true)){
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			}else if(moveItemStackTo(itemstack1, 0, 1, false)){
				return ItemStack.EMPTY;
			}else if(pIndex >= 1 && pIndex < 28){
				if(!moveItemStackTo(itemstack1, 28, 37, false)){
					return ItemStack.EMPTY;
				}
			}else if(pIndex >= 28 && pIndex < 37){
				if(!moveItemStackTo(itemstack1, 1, 28, false)){
					return ItemStack.EMPTY;
				}
			}else if(!moveItemStackTo(itemstack1, 1, 37, false)){
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty()){
				slot.set(ItemStack.EMPTY);
			}else{
				slot.setChanged();
			}

			if(itemstack1.getCount() == itemstack.getCount()){
				return ItemStack.EMPTY;
			}

			slot.onTake(pPlayer, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean stillValid(Player p_75145_1_){
		return true;
	}
}