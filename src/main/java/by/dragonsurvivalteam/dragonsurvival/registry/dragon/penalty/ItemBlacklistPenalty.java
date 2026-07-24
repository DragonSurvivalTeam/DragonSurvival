package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DataReloadHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ItemBlacklistPenalty implements PenaltyEffect {
    public static final MapCodec<ItemBlacklistPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().listOf().optionalFieldOf("items", List.of()).forGetter(ItemBlacklistPenalty::items),
            ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(ItemBlacklistPenalty::predicate)
    ).apply(instance, ItemBlacklistPenalty::new));

    private final List<String> items;
    private final Optional<ItemPredicate> predicate;
    private Set<ResourceKey<Item>> blacklisted;
    private long lastUpdate;

    public ItemBlacklistPenalty(final List<String> items, final Optional<ItemPredicate> predicate) {
        this.items = items;
        this.predicate = predicate;
    }

    public ItemBlacklistPenalty(final List<String> items) {
        this.items = items;
        this.predicate = Optional.empty();
    }

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        dropAllItemsInList(player, player.getInventory().armor);
        dropAllItemsInList(player, player.getInventory().offhand);
        dropCurios(player);

        ClawInventoryData clawData = ClawInventoryData.getData(player);
        SimpleContainer clawContainer = clawData.getContainer();

        for (int slot = 0; slot < clawContainer.getContainerSize(); slot++) {
            ItemStack stack = clawContainer.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (isBlacklisted(stack)) {
                ItemStack removed = clawContainer.removeItem(slot, stack.getCount());
                player.drop(removed, false);
            }
        }

        ItemStack mainHandItem = player.getMainHandItem();

        if (isBlacklisted(mainHandItem)) {
            player.getInventory().removeItem(mainHandItem);
            player.drop(mainHandItem, false);
        }
    }

    public boolean isBlacklisted(final ItemStack stack) {
        if (blacklisted == null || lastUpdate < DataReloadHandler.lastReload) {
            lastUpdate = System.currentTimeMillis();
            blacklisted = map(items);
        }

        return blacklisted.contains(stack.getItemHolder().getKey()) || predicate.isPresent() && predicate.get().test(stack);
    }

    private void dropAllItemsInList(final Player player, final NonNullList<ItemStack> items) {
        items.forEach(stack -> {
            if (isBlacklisted(stack)) {
                player.getInventory().removeItem(stack);
                player.drop(stack, false);
            }
        });
    }

    private void dropCurios(final Player player) {
        if (!ModID.CURIOS.isLoaded()) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            IItemHandlerModifiable equipped = inventory.getEquippedCurios();

            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                if (isBlacklisted(equipped.getStackInSlot(slot))) {
                    ItemStack removed = equipped.extractItem(slot, 64, false);

                    if (!removed.isEmpty()) {
                        player.drop(removed, false);
                    }
                }
            }
        });
    }

    private Set<ResourceKey<Item>> map(final List<String> entries) {
        Set<ResourceKey<Item>> blacklisted = new HashSet<>();
        entries.forEach(entry -> blacklisted.addAll(ResourceLocationWrapper.map(entry, BuiltInRegistries.ITEM)));
        return blacklisted;
    }

    public List<String> items() {
        return items;
    }

    public Optional<ItemPredicate> predicate() {
        return predicate;
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
