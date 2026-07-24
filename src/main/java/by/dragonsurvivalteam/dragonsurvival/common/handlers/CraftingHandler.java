package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.items.armor.PermanentEnchantmentItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.enchanting.GetEnchantmentLevelEvent;

@EventBusSubscriber
public class CraftingHandler {
    @SubscribeEvent // Upgrades the enchantment by 1 level if the default enchantment level matches the enchantment level
    public static void getAllEnchantmentLevels(final GetEnchantmentLevelEvent event) {
        if (event.getStack().getItem() instanceof PermanentEnchantmentItem item) {
            item.getDefaultEnchantments().keySet().forEach(
                    holder -> {
                        int defaultLevel = item.getDefaultEnchantments().getLevel(holder);
                        event.getEnchantments().upgrade(holder, defaultLevel + (event.getEnchantments().getLevel(holder) == defaultLevel ? 1 : 0));
                    }
            );
        }
    }
}
