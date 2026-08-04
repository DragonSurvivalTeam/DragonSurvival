package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface PermanentEnchantmentItem extends IForgeItem {
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

    @Override
    default int getEnchantmentLevel(final ItemStack stack, final Enchantment enchantment) {
        int currentLevel = IForgeItem.super.getEnchantmentLevel(stack, enchantment);
        Integer defaultLevel = getDefaultEnchantments().get(enchantment);
        return defaultLevel == null ? currentLevel : mergeEnchantmentLevel(currentLevel, defaultLevel);
    }

    @Override
    default Map<Enchantment, Integer> getAllEnchantments(final ItemStack stack) {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(IForgeItem.super.getAllEnchantments(stack));
        getDefaultEnchantments().forEach((enchantment, defaultLevel) -> enchantments.compute(
                enchantment,
                (ignored, currentLevel) -> mergeEnchantmentLevel(currentLevel == null ? 0 : currentLevel, defaultLevel)
        ));
        return enchantments;
    }

    private static int mergeEnchantmentLevel(int currentLevel, int defaultLevel) {
        return Math.max(currentLevel, defaultLevel + (currentLevel == defaultLevel ? 1 : 0));
    }
}
