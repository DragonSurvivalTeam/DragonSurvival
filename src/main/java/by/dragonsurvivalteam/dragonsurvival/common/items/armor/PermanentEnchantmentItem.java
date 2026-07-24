package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

public interface PermanentEnchantmentItem {
    default List<Pair<ResourceKey<Enchantment>, Integer>> enchantments() {
        return List.of();
    }

    default ItemEnchantments getDefaultEnchantments() {
        ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        for (Pair<ResourceKey<Enchantment>, Integer> enchantment : enchantments()) {
            EnchantmentUtils.set(itemEnchantments, enchantment.first(), enchantment.second());
        }

        return itemEnchantments.toImmutable();
    }
}
