package by.dragonsurvivalteam.dragonsurvival.compat.car;

import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CosmeticArmorReworkedHelper {
    public static ItemStack getItemVisibleInSlot(Player player, EquipmentSlot slot) {
        if (ModCheck.isModLoaded(ModCheck.COSMETIC_ARMOR_REWORKED)) {
            InventoryCosArmor CAItems = (InventoryCosArmor) CosArmorAPI.getCAStacksClient(player.getUUID());
            ItemStack item = CAItems.getItem(slot.getIndex());
            if (CAItems.isSkinArmor(slot.getIndex())) {
                return new ItemStack(Items.AIR); // The slot is currently hidden and should show no armor
            } else if (!item.is(Items.AIR)) {
                return item; // You have an item equipped in the cosmetic slot, so it should be visible
            }
        }
        return player.getItemBySlot(slot); // You don't have an item equipped in the cosmetic slot, so use the base
    }
}
