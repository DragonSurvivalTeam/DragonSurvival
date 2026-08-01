package by.dragonsurvivalteam.dragonsurvival.compat.car;

import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CosmeticArmorReworkedHelper {
    public static ItemStack getItemVisibleInSlot(Player player, EquipmentSlot slot) {
        if (ModID.COSMETIC_ARMOR_REWORKED.isLoaded()) {
            InventoryCosArmor cosmeticArmor = (InventoryCosArmor) CosArmorAPI.getCAStacksClient(player.getUUID());
            ItemStack item = cosmeticArmor.getItem(slot.getIndex());
            if (cosmeticArmor.isSkinArmor(slot.getIndex())) {
                return new ItemStack(Items.AIR);
            } else if (!item.is(Items.AIR)) {
                return item;
            }
        }

        return player.getItemBySlot(slot);
    }
}
