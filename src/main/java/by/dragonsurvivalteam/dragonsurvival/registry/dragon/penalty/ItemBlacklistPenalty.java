package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.IdentifierWrapper;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DataReloadHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemBlacklistPenalty implements PenaltyEffect {
    public static final MapCodec<ItemBlacklistPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IdentifierWrapper.validatedCodec().listOf().fieldOf("items").forGetter(ItemBlacklistPenalty::items)
    ).apply(instance, ItemBlacklistPenalty::new));

    private final List<String> items;
    private Set<ResourceKey<Item>> blacklisted;
    private long lastUpdate;

    public ItemBlacklistPenalty(final List<String> items) {
        this.items = items;
    }

    public static NonNullList<ItemStack> getArmorItems(final Inventory inventory) {
        return NonNullList.of(
                inventory.getItem(EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE)),
                inventory.getItem(EquipmentSlot.LEGS.getIndex(Inventory.INVENTORY_SIZE)),
                inventory.getItem(EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE)),
                inventory.getItem(EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE))
        );
    }

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        dropAllItemsInList(player, getArmorItems(player.getInventory()));
        dropAllItemsInList(player, NonNullList.of(player.getInventory().getItem(Inventory.SLOT_OFFHAND)));
        dropCurios(player);

        ClawInventoryData clawData = ClawInventoryData.getData(player);
        SimpleContainer clawContainer = clawData.getContainer();

        for (int slot = 0; slot < clawContainer.getContainerSize(); slot++) {
            ItemStack stack = clawContainer.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (isBlacklisted(stack.getItem())) {
                ItemStack removed = clawContainer.removeItem(slot, stack.getCount());
                player.drop(removed, false);
            }
        }

        ItemStack mainHandItem = player.getMainHandItem();

        if (isBlacklisted(mainHandItem.getItem())) {
            player.getInventory().removeItem(mainHandItem);
            player.drop(mainHandItem, false);
        }
    }

    public boolean isBlacklisted(final Item item) {
        if (blacklisted == null || lastUpdate < DataReloadHandler.lastReload) {
            lastUpdate = System.currentTimeMillis();
            blacklisted = map(items);
        }

        //noinspection deprecation -> ignore
        return blacklisted.contains(item.builtInRegistryHolder().getKey());
    }

    private void dropAllItemsInList(final Player player, final NonNullList<ItemStack> items) {
        items.forEach(stack -> {
            if (isBlacklisted(stack.getItem())) {
                player.getInventory().removeItem(stack);
                player.drop(stack, false);
            }
        });
    }

    private void dropCurios(final Player player) {
        if (!ModID.CURIOS.isLoaded()) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> inventory.getCurios().values().forEach(stacksHandler -> dropCurios(player, stacksHandler)));
    }

    private void dropCurios(final Player player, final ICurioStacksHandler stacksHandler) {
        IDynamicStackHandler stacks = stacksHandler.getStacks();

        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            ItemStack stack = stacks.getStackInSlot(slot);

            if (stack.isEmpty() || !isBlacklisted(stack.getItem())) {
                continue;
            }

            ItemStack removed = stack.copy();
            stacks.setStackInSlot(slot, ItemStack.EMPTY);
            player.drop(removed, false);
        }
    }

    private Set<ResourceKey<Item>> map(final List<String> entries) {
        Set<ResourceKey<Item>> blacklisted = new HashSet<>();
        entries.forEach(entry -> blacklisted.addAll(IdentifierWrapper.map(entry, BuiltInRegistries.ITEM)));
        return blacklisted;
    }

    public List<String> items() {
        return items;
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
