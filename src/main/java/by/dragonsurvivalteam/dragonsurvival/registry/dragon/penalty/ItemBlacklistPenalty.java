package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DataReloadHandler;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemBlacklistPenalty implements PenaltyEffect {
    public static final MapCodec<ItemBlacklistPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().listOf().fieldOf("items").forGetter(ItemBlacklistPenalty::items)
    ).apply(instance, ItemBlacklistPenalty::new));

    private final List<String> items;

    private Set<ResourceLocation> blacklisted;
    private long lastUpdate;

    public ItemBlacklistPenalty(final List<String> items) {
        this.items = items;
    }

    @Override
    public void apply(final Player player) {
        dropAllItemsInList(player, player.getInventory().armor);
        dropAllItemsInList(player, player.getInventory().offhand);

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

        //noinspection deprecation,DataFlowIssue -> ignore deprecated / key is present
        return blacklisted.contains(item.builtInRegistryHolder().getKey().location());
    }

    private void dropAllItemsInList(final Player player, final NonNullList<ItemStack> items) {
        items.forEach(stack -> {
            if (isBlacklisted(stack.getItem())) {
                player.getInventory().removeItem(stack);
                player.drop(stack, false);
            }
        });
    }

    private Set<ResourceLocation> map(final List<String> entries) {
        Set<ResourceLocation> blacklisted = new HashSet<>();
        entries.forEach(entry -> blacklisted.addAll(ResourceLocationWrapper.getEntries(entry, BuiltInRegistries.ITEM)));
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
