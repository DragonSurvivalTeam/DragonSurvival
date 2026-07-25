package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface PermanentEnchantmentItem {
    default List<Pair<ResourceKey<Enchantment>, Integer>> enchantments() {
        return List.of();
    }

    default Map<Enchantment, Integer> getDefaultEnchantments() {
        Map<Enchantment, Integer> itemEnchantments = new LinkedHashMap<>();

        for (Pair<ResourceKey<Enchantment>, Integer> enchantment : enchantments()) {
            EnchantmentUtils.set(itemEnchantments, enchantment.first(), enchantment.second());
        }

        return itemEnchantments;
    }
}
