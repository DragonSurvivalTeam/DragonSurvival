package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EnchantmentUtils {
    public static int getLevel(@NotNull final LivingEntity entity, @NotNull final ResourceKey<Enchantment> enchantment) {
        Enchantment value = get(enchantment);
        return value == null ? 0 : EnchantmentHelper.getEnchantmentLevel(value, entity);
    }

    public static int getLevel(@NotNull final LivingEntity entity, @NotNull final Enchantment enchantment) {
        return EnchantmentHelper.getEnchantmentLevel(enchantment, entity);
    }

    public static int getLevel(@NotNull final Level level, @NotNull final ResourceKey<Enchantment> enchantment, @NotNull final ItemStack stack) {
        Enchantment value = get(enchantment);
        return value == null ? 0 : stack.getEnchantmentLevel(value);
    }

    public static int getLevel(@NotNull final Level level, @NotNull final Enchantment enchantment, @NotNull final ItemStack stack) {
        return stack.getEnchantmentLevel(enchantment);
    }

    public static @Nullable Enchantment get(final ResourceKey<Enchantment> enchantment) {
        return BuiltInRegistries.ENCHANTMENT.getOptional(enchantment.location()).orElse(null);
    }

    public static void set(final Map<Enchantment, Integer> enchantments, final ResourceKey<Enchantment> enchantment, int level) {
        Enchantment value = get(enchantment);

        if (value != null) {
            enchantments.put(value, level);
        }
    }
}
