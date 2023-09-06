package by.dragonsurvivalteam.dragonsurvival.server.containers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSContainers;
import by.dragonsurvivalteam.dragonsurvival.server.containers.slots.ClawToolSlot;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DragonContainer extends AbstractContainerMenu {
	public final CraftingContainer craftMatrix = new CraftingContainer(this, 3, 3);
	public final ResultContainer craftResult = new ResultContainer();
	public final Player player;

	public List<Slot> craftingSlots = new ArrayList<>();
	public List<Slot> inventorySlots = new ArrayList<>();
	public Inventory playerInventory;
	public int menuStatus;


	private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = {InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};

	private final int craftingResultIndex;

	/** (Unsure)
	Data which needs to be synchronized between Server and Client<br>
	Used by {@link ClawToolSlot} to determine if they are active or not
	*/
	protected final ContainerData dataStatus = new ContainerData() {
		@Override
		public int get(int index) {
			if (index == 0) {
				return menuStatus;
			}

			return 0;
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) {
				menuStatus = value;
			}
		}

		@Override
		public int getCount() {
			return 1;
		}
	};

	public DragonContainer(int id, final Inventory inventory) {
		super(DSContainers.dragonContainer, id);
		this.player = inventory.player;
		this.playerInventory = inventory;

		addDataSlots(dataStatus);

		for (int i = 0; i < 4; i++) {
			EquipmentSlot equipmentSlot = VALID_EQUIPMENT_SLOTS[i];

			addSlot(new Slot(inventory, 39 - i, 8, 8 + i * 18) {
				@Override
				public boolean mayPlace(@NotNull final ItemStack itemStack) {
					return itemStack.canEquip(equipmentSlot, player);
				}

				@Override
				@OnlyIn(Dist.CLIENT) // TODO :: Is this needed?
				public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
					return Pair.of(InventoryMenu.BLOCK_ATLAS, ARMOR_SLOT_TEXTURES[equipmentSlot.getIndex()]);
				}

				@Override
				public boolean mayPickup(@NotNull final Player player) {
					ItemStack itemStack = getItem();
					return (itemStack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(itemStack)) && super.mayPickup(player);
				}
			});

			broadcastChanges();
		}

		// Inventory slots
		for (int column = 0; column < 3; column++) {
			for(int row = 0; row < 9; row++) {
				Slot s = new Slot(inventory, row + column * 9 + 9, 8 + row * 18, 84 + column * 18);
				addSlot(s);
				inventorySlots.add(s);
			}
		}

		// Hotbar slots
		for (int i = 0; i < 9; i++) {
			Slot s = new Slot(inventory, i, 8 + i * 18, 142);
			addSlot(s);
			inventorySlots.add(s);
		}

		// Claw tool slots
		DragonStateProvider.getCap(player).ifPresent(handler -> {
			for (int i = 0; i < 4; i++) {
				ClawToolSlot clawToolSlot = new ClawToolSlot(this, handler.getClawToolData().getClawsInventory(), i, -50, 35 + i * 18, i);
				addSlot(clawToolSlot);
				inventorySlots.add(clawToolSlot);
			}
		});

		// Offhand slot
		addSlot(new Slot(inventory, 40, 26, 62));

		// Crafting slots
		addSlot(new ResultSlot(inventory.player, craftMatrix, craftResult, 0, 178, 33));
		craftingResultIndex = slots.size() - 1;

		for (int row = 0; row < craftMatrix.getWidth(); row++) {
			for (int column = 0; column < craftMatrix.getHeight(); column++) {
				Slot s = new Slot(craftMatrix, column + row * 3, 111 + column * 18, 15 + row * 18);
				addSlot(s);
				craftingSlots.add(s);
			}
		}

		update();
	}

	public void update() {
		DragonStateProvider.getCap(player).ifPresent(handler -> menuStatus = handler.getClawToolData().isMenuOpen() ? 1 : 0);
		broadcastChanges();
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull final Player player, int index) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = slots.get(index);

		if (slot.hasItem()) {
			ItemStack slotItemStack = slot.getItem();
			itemStack = slotItemStack.copy();

			EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);

			if (index == craftingResultIndex) { // Index 45
				// Move item from the crafting slot into the inventory / hotbar
				if (!moveItemStackTo(slotItemStack, 4, 45, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(slotItemStack, itemStack);
			} else if (equipmentSlot.getType() == Type.ARMOR && !slots.get(3 - equipmentSlot.getIndex()).hasItem()) {
				// Move the item into the relevant armor equipment slot (0 to 4) (if the slot is free)
				int i = 3 - equipmentSlot.getIndex();

				if (!moveItemStackTo(slotItemStack, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 4) {
				// Move the item from the armor slot into the inventory / hotbar
				if (!moveItemStackTo(slotItemStack, 4, 40, false)) {
					return ItemStack.EMPTY;
				}
			} else if (menuStatus == 1 && !slots.get(42).hasItem() && ToolUtils.isAxe(slotItemStack)) {
				// Claw tool axe (check first to prefer axe slot over weapon slot)
				if (!moveItemStackTo(slotItemStack, 42, 43, false)) {
					return ItemStack.EMPTY;
				}
			} else if (menuStatus == 1 && !slots.get(40).hasItem() && ToolUtils.isWeapon(slotItemStack)) {
				// Claw tool sword
				if (!moveItemStackTo(slotItemStack, 40, 41, false)) {
					return ItemStack.EMPTY;
				}
			} else if (menuStatus == 1 && !slots.get(41).hasItem() && ToolUtils.isPickaxe(slotItemStack)) {
				// Claw tool pickaxe
				if (!moveItemStackTo(slotItemStack, 41, 42, false)) {
					return ItemStack.EMPTY;
				}
			} else if (menuStatus == 1 && !slots.get(43).hasItem() && ToolUtils.isShovel(slotItemStack)) {
				// Claw tool shovel
				if (!moveItemStackTo(slotItemStack, 43, 44, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentSlot == EquipmentSlot.OFFHAND && !slots.get(44).hasItem()) {
				// Move the item into the offhand (if the slot is free)
				if (!moveItemStackTo(slotItemStack, 44, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 31) {
				// Move item from the inventory into the hotbar
				if (!moveItemStackTo(slotItemStack, 31, 40, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 40) {
				// Move item from the hotbar into the inventory
				if (!moveItemStackTo(slotItemStack, 4, 31, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				/* Move the item from the following slots into the inventory:
				Claw inventory slots: 	index 40 to 43
				Offhand slot: 			index 44
				Crafting slots: 		index 46 to 54
				*/
				if (!moveItemStackTo(slotItemStack, 4, 31, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (slotItemStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (slotItemStack.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, slotItemStack);

			if (index == 0) {
				player.drop(slotItemStack, false);
			}
		}

		return itemStack;
	}

	@Override
	public boolean canTakeItemForPickAll(@Nonnull final ItemStack itemStack, final Slot slot) {
		return slot.container != craftResult && super.canTakeItemForPickAll(itemStack, slot);
	}

	@Override
	public void removed(@NotNull final Player player) {
		super.removed(player);
		clearContainer(player, craftMatrix);
	}

	/** Callback for when the crafting matrix is changed */
	@Override
	public void slotsChanged(@NotNull final Container inventory) {
		if (player instanceof ServerPlayer serverPlayer) {
			ItemStack itemStack = ItemStack.EMPTY;
			Optional<CraftingRecipe> recipeOptional = serverPlayer.level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftMatrix, serverPlayer.level);

			if (recipeOptional.isPresent()) {
				CraftingRecipe recipe = recipeOptional.get();

				if (craftResult.setRecipeUsed(player.level, serverPlayer, recipe)) {
					itemStack = recipe.assemble(craftMatrix);
				}
			}

			craftResult.setItem(45, itemStack);
			setRemoteSlot(45, itemStack);
			serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 45, itemStack));
		}
	}

	@Override
	public boolean stillValid(@NotNull final Player ignored) {
		return true;
	}
}