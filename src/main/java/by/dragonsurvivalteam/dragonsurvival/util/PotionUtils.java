package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Optional;

public class PotionUtils {
    // We need to be careful here, as some mods might have items that are instances of
    // PotionItem that do not actually have POTION_CONTENTS, which would crash in previous iterations of this code.
    public static Optional<Potion> getPotion(ItemStack item) {
        Potion potion = net.minecraft.world.item.alchemy.PotionUtils.getPotion(item);
        return potion == Potions.EMPTY ? Optional.empty() : Optional.of(potion);
    }
}
