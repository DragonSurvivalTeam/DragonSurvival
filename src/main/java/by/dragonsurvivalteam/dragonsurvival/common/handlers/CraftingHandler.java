package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.items.armor.PermanentEnchantmentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.GetEnchantmentLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Map;

@EventBusSubscriber
public class CraftingHandler {
    @SubscribeEvent // Upgrades the enchantment by 1 level if the default enchantment level matches the enchantment level
    public static void getAllEnchantmentLevels(final GetEnchantmentLevelEvent event) {
        if (event.getStack().getItem() instanceof PermanentEnchantmentItem item) {
            Map<Enchantment, Integer> enchantments = event.getEnchantments();

            item.getDefaultEnchantments().forEach((enchantment, defaultLevel) -> {
                if (event.isTargetting(enchantment)) {
                    int currentLevel = enchantments.getOrDefault(enchantment, 0);
                    enchantments.put(enchantment, Math.max(currentLevel, defaultLevel + (currentLevel == defaultLevel ? 1 : 0)));
                }
            });
        }
    }
}
