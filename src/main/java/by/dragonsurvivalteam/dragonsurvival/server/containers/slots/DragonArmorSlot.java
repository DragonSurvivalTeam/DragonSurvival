package by.dragonsurvivalteam.dragonsurvival.server.containers.slots;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nullable;

/**
 * The named 1.21 armor slot behavior, implemented against the 1.20 Slot API.
 */
public class DragonArmorSlot extends Slot {
    private final LivingEntity owner;
    private final EquipmentSlot equipmentSlot;
    @Nullable private final ResourceLocation emptyIcon;

    public DragonArmorSlot(final Container container, final LivingEntity owner, final EquipmentSlot equipmentSlot,
                           final int slotIndex, final int x, final int y, @Nullable final ResourceLocation emptyIcon) {
        super(container, slotIndex, x, y);
        this.owner = owner;
        this.equipmentSlot = equipmentSlot;
        this.emptyIcon = emptyIcon;
    }

    @Override
    public void setByPlayer(final ItemStack stack) {
        owner.onEquipItem(equipmentSlot, getItem(), stack);
        super.setByPlayer(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return stack.canEquip(equipmentSlot, owner);
    }

    @Override
    public boolean mayPickup(final Player player) {
        ItemStack stack = getItem();
        return stack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(stack) && super.mayPickup(player);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return emptyIcon == null ? super.getNoItemIcon() : Pair.of(InventoryMenu.BLOCK_ATLAS, emptyIcon);
    }
}
