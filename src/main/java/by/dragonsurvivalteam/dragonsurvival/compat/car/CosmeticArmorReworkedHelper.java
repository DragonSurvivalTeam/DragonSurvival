package by.dragonsurvivalteam.dragonsurvival.compat.car;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CosmeticArmorReworkedHelper {
    public static ItemStack getItemVisibleInSlot(Player player, EquipmentSlot slot) {
        return player.getItemBySlot(slot);
    }
}
