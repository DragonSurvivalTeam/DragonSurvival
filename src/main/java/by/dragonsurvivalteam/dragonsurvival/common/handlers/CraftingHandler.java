package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.items.armor.PermanentEnchantmentItem;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@SuppressWarnings("unused")
@EventBusSubscriber
public class CraftingHandler {
    @SubscribeEvent
    public static void returnBeacon(PlayerEvent.ItemCraftedEvent craftedEvent) {
        Container inventory = craftedEvent.getInventory();
        ItemStack result = craftedEvent.getCrafting();
        int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.is(DSItems.PASSIVE_FIRE_BEACON)
                || item.is(DSItems.PASSIVE_MAGIC_BEACON)
                || item.is(DSItems.PASSIVE_PEACE_BEACON), 1, true);
        if (rem == 0 && result.getItem() == DSBlocks.DRAGON_BEACON.get().asItem()) {
            craftedEvent.getEntity().addItem(new ItemStack(Items.BEACON));
        }
    }

    @SubscribeEvent
    public static void returnNetherStarHeart(PlayerEvent.ItemCraftedEvent craftedEvent) {
        Container inventory = craftedEvent.getInventory();
        ItemStack result = craftedEvent.getCrafting();
        int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.is(DSItems.STAR_HEART), 1, true);
        if (rem == 0 && result.is(DSItems.STAR_HEART)) {
            craftedEvent.getEntity().addItem(new ItemStack(Items.NETHER_STAR));
        }
    }

    @SubscribeEvent
    public static void returnNetherStarBone(PlayerEvent.ItemCraftedEvent craftedEvent) {
        Container inventory = craftedEvent.getInventory();
        ItemStack result = craftedEvent.getCrafting();
        int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.is(DSItems.STAR_BONE), 1, true);
        if (rem == 0 && result.is(DSItems.STAR_BONE)) {
            craftedEvent.getEntity().addItem(new ItemStack(Items.NETHER_STAR));
        }
    }

    @SubscribeEvent
    public static void getAllEnchantmentLevels(GetEnchantmentLevelEvent event) {
        if (event.getStack().getItem() instanceof PermanentEnchantmentItem item) {
            item.getDefaultEnchantments().keySet().forEach(
                    enchantmentHolder -> {
                        int itemLevel = item.getDefaultEnchantments().getLevel(enchantmentHolder);
                        event.getEnchantments().upgrade(enchantmentHolder, itemLevel + (event.getEnchantments().getLevel(enchantmentHolder) == itemLevel ? 1 : 0));
                    }
            );
        }
    }
}
