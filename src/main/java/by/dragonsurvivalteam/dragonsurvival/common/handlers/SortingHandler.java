package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.util.PotionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Copied from Quark <br>
 * Updated to 26.1 by DS team
 * <a href="https://github.com/VazkiiMods/Quark/blob/1.21.0/dev/src/main/java/org/violetmoon/quark/base/handler/SortingHandler.java">Source</a> <br>
 * <a href="https://github.com/VazkiiMods/Quark/blob/master/LICENSE.md">License</a> <br>
 */
public final class SortingHandler {
    private static final Comparator<ItemStack> FALLBACK_COMPARATOR = jointComparator(Arrays.asList(
            Comparator.comparingInt(stack -> Item.getId(stack.getItem())),
            SortingHandler::damageCompare,
            Comparator.comparingInt(ItemStack::getCount),
            Comparator.comparingInt(Object::hashCode),
            SortingHandler::fallbackNBTCompare)
    );

    private static final Comparator<ItemStack> FOOD_COMPARATOR = jointComparator(Arrays.asList(
            SortingHandler::foodNutritionCompare,
            SortingHandler::foodSaturationCompare)
    );

    private static final Comparator<ItemStack> TOOL_COMPARATOR = jointComparator(Arrays.asList(
            SortingHandler::toolPowerCompare,
            SortingHandler::enchantmentCompare,
            SortingHandler::damageCompare)
    );

    private static final Comparator<ItemStack> SWORD_COMPARATOR = jointComparator(Arrays.asList(
            SortingHandler::swordPowerCompare,
            SortingHandler::enchantmentCompare,
            SortingHandler::damageCompare)
    );

    private static final Comparator<ItemStack> ARMOR_COMPARATOR = jointComparator(Arrays.asList(
            SortingHandler::armorSlotAndToughnessCompare,
            SortingHandler::enchantmentCompare,
            SortingHandler::damageCompare)
    );

    private static final Comparator<ItemStack> BOW_COMPARATOR = jointComparator(Arrays.asList(
            SortingHandler::enchantmentCompare,
            SortingHandler::damageCompare)
    );

    private static final Comparator<ItemStack> POTION_COMPARATOR = jointComparator(Arrays.asList(
            SortingHandler::potionComplexityCompare,
            SortingHandler::potionTypeCompare)
    );

    public static void sortInventory(final Player player) {
        if (player.containerMenu instanceof DragonContainer container) {
            sortInventory(player, container.inventorySlots);
            container.broadcastChanges();
        }
    }

    private static void sortInventory(final Player player, final List<Slot> slots) {
        List<ItemStack> stacks = new ArrayList<>();
        List<ItemStack> restore = new ArrayList<>();

        for (Slot slot : slots) {
            ItemStack stackAt = slot.getItem();

            restore.add(stackAt.copy());

            if (!stackAt.isEmpty()) {
                stacks.add(stackAt.copy());
            }
        }

        mergeStacks(stacks);
        sortStackList(stacks);

        if (setInventory(player, slots, stacks) == InteractionResult.FAIL) {
            setInventory(player, slots, restore);
        }
    }

    private static InteractionResult setInventory(final Player player, final List<Slot> slots, final List<ItemStack> stacks) {
        for (int slotIndex = 0; slotIndex < slots.size(); slotIndex++) {
            Slot slot = slots.get(slotIndex);
            ItemStack currentStack = slot.getItem();
            ItemStack newStack = slotIndex >= stacks.size() ? ItemStack.EMPTY : stacks.get(slotIndex);

            if (!currentStack.isEmpty() && !slot.mayPickup(player)) {
                return InteractionResult.PASS;
            }

            if (!newStack.isEmpty() && (!slot.mayPlace(newStack) || newStack.getCount() > slot.getMaxStackSize(newStack))) {
                return InteractionResult.PASS;
            }
        }

        for (int slotIndex = 0; slotIndex < slots.size(); slotIndex++) {
            Slot slot = slots.get(slotIndex);
            ItemStack previous = slot.getItem().copy();
            ItemStack stack = slotIndex >= stacks.size() ? ItemStack.EMPTY : stacks.get(slotIndex).copy();

            slot.setByPlayer(stack, previous);
        }

        return InteractionResult.SUCCESS;
    }

    public static void mergeStacks(final List<ItemStack> list) {
        for (int i = 0; i < list.size(); i++) {
            ItemStack set = mergeStackWithOthers(list, i);
            list.set(i, set);
        }

        list.removeIf((ItemStack stack) -> stack.isEmpty() || stack.getCount() == 0);
    }

    private static ItemStack mergeStackWithOthers(final List<ItemStack> list, final int index) {
        ItemStack stack = list.get(index);

        if (stack.isEmpty()) {
            return stack;
        }

        for (int i = 0; i < list.size(); i++) {
            if (i == index) {
                continue;
            }

            ItemStack stackAt = list.get(i);

            if (stackAt.isEmpty()) {
                continue;
            }

            if (stackAt.getCount() < stackAt.getMaxStackSize() && ItemStack.isSameItemSameComponents(stack, stackAt)) {
                int setSize = stackAt.getCount() + stack.getCount();
                int carryover = Math.max(0, setSize - stackAt.getMaxStackSize());

                stackAt.setCount(carryover);
                stack.setCount(setSize - carryover);

                if (stack.getCount() == stack.getMaxStackSize()) {
                    return stack;
                }
            }
        }

        return stack;
    }

    public static void sortStackList(final List<ItemStack> list) {
        list.sort(SortingHandler::stackCompare);
    }

    private static int stackCompare(final ItemStack first, final ItemStack second) {
        if (first == second) {
            return 0;
        }

        if (first.isEmpty()) {
            return -1;
        }

        if (second.isEmpty()) {
            return 1;
        }

        ItemType firstType = getType(first);
        ItemType secondType = getType(second);

        if (firstType == secondType) {
            return firstType.comparator.compare(first, second);
        }

        return firstType.ordinal() - secondType.ordinal();
    }

    private static ItemType getType(final ItemStack stack) {
        for (ItemType type : ItemType.values()) {
            if (type.fitsInType(stack)) {
                return type;
            }
        }

        throw new RuntimeException("Having an ItemStack that doesn't fit in any type is impossible.");
    }

    private static Predicate<ItemStack> classPredicate(Class<? extends Item> type) {
        return stack -> !stack.isEmpty() && type.isInstance(stack.getItem());
    }

    @SuppressWarnings("SameParameterValue") // ignore
    private static Predicate<ItemStack> inverseClassPredicate(final Class<? extends Item> type) {
        return classPredicate(type).negate();
    }

    private static Predicate<ItemStack> itemPredicate(final List<Item> list) {
        return (ItemStack s) -> !s.isEmpty() && list.contains(s.getItem());
    }

    public static Comparator<ItemStack> jointComparator(final Comparator<ItemStack> finalComparator, final List<Comparator<ItemStack>> otherComparators) {
        if (otherComparators == null) {
            return jointComparator(List.of(finalComparator));
        }

        List<Comparator<ItemStack>> newList = new ArrayList<>(otherComparators);
        newList.add(finalComparator);
        return jointComparator(newList);
    }

    public static Comparator<ItemStack> jointComparator(final List<Comparator<ItemStack>> comparators) {
        return jointComparatorFallback((ItemStack first, ItemStack second) -> {
            for (Comparator<ItemStack> comparator : comparators) {
                if (comparator == null) {
                    continue;
                }

                int compare = comparator.compare(first, second);

                if (compare == 0) {
                    continue;
                }

                return compare;
            }

            return 0;
        }, FALLBACK_COMPARATOR);
    }

    @SuppressWarnings("SameParameterValue") // ignore
    private static Comparator<ItemStack> jointComparatorFallback(final Comparator<ItemStack> comparator, final Comparator<ItemStack> fallback) {
        return (ItemStack first, ItemStack second) -> {
            int compare = comparator.compare(first, second);

            if (compare == 0) {
                return fallback == null ? 0 : fallback.compare(first, second);
            }

            return compare;
        };
    }

    private static Comparator<ItemStack> listOrderComparator(final List<Item> list) {
        return (ItemStack first, ItemStack second) -> {
            Item firstItem = first.getItem();
            Item secondItem = second.getItem();

            if (list.contains(firstItem)) {
                if (list.contains(secondItem)) {
                    return list.indexOf(firstItem) - list.indexOf(secondItem);
                }

                return 1;
            }

            if (list.contains(secondItem)) {
                return -1;
            }

            return 0;
        };
    }

    private static List<Item> list(final Object... items) {
        List<Item> list = new ArrayList<>();

        for (Object object : items) {
            switch (object) {
                case Item item -> list.add(item);
                case Block block -> list.add(block.asItem());
                case ItemStack stack -> list.add(stack.getItem());
                case String string -> BuiltInRegistries.ITEM.getOptional(Identifier.parse(string)).ifPresent(list::add);
                default -> { /* Nothing to do */ }
            }
        }

        return list;
    }

    private static int nutrition(final FoodProperties properties) {
        if (properties == null) {
            return 0;
        }

        return properties.nutrition();
    }

    private static int foodNutritionCompare(final ItemStack first, final ItemStack second) {
        return nutrition(second.get(DataComponents.FOOD)) - nutrition(first.get(DataComponents.FOOD));
    }

    private static float saturation(final FoodProperties properties) {
        if (properties == null) {
            return 0;
        }

        return properties.saturation();
    }

    private static int foodSaturationCompare(final ItemStack first, final ItemStack second) {
        return Float.compare(saturation(second.get(DataComponents.FOOD)), saturation(first.get(DataComponents.FOOD)));
    }

    private static int enchantmentCompare(final ItemStack first, final ItemStack second) {
        return enchantmentPower(second) - enchantmentPower(first);
    }

    private static int enchantmentPower(final ItemStack stack) {
        if (!stack.isEnchanted()) {
            return 0;
        }

        // There is probably no need to resolve these through Stack#getAllEnchantments
        // Since enchantments added through events are probably present on all instances of the item
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

        int total = 0;

        for (var entry : enchantments.entrySet()) {
            total += entry.getIntValue();
        }

        return total;
    }

    private static int toolPowerCompare(final ItemStack first, final ItemStack second) {
        return Float.compare(toolPower(second), toolPower(first));
    }

    private static int swordPowerCompare(final ItemStack first, final ItemStack second) {
        return Double.compare(attributeValue(second, Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND), attributeValue(first, Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND));
    }

    private static int armorSlotAndToughnessCompare(final ItemStack first, final ItemStack second) {
        EquipmentSlot firstSlot = equipmentSlot(first);
        EquipmentSlot secondSlot = equipmentSlot(second);

        if (firstSlot == secondSlot) {
            double firstDefense = attributeValue(first, Attributes.ARMOR, firstSlot) * (1.0D + attributeValue(first, Attributes.ARMOR_TOUGHNESS, firstSlot));
            double secondDefense = attributeValue(second, Attributes.ARMOR, secondSlot) * (1.0D + attributeValue(second, Attributes.ARMOR_TOUGHNESS, secondSlot));

            return Double.compare(secondDefense, firstDefense);
        }

        return secondSlot.getIndex() - firstSlot.getIndex();
    }

    private static float toolPower(final ItemStack stack) {
        Tool tool = stack.get(DataComponents.TOOL);
        return tool == null ? 0.0F : tool.defaultMiningSpeed();
    }

    private static double attributeValue(final ItemStack stack, final Holder<Attribute> attribute, final EquipmentSlot slot) {
        return stack.getAttributeModifiers().compute(attribute, 0.0D, slot);
    }

    private static EquipmentSlot equipmentSlot(final ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable == null ? EquipmentSlot.MAINHAND : equippable.slot();
    }

    private static boolean isSwordLike(final ItemStack stack) {
        Weapon weapon = stack.get(DataComponents.WEAPON);
        return weapon != null && weapon.itemDamagePerAttack() == 1 && stack.has(DataComponents.TOOL) && !(stack.getItem() instanceof TridentItem);
    }

    public static int damageCompare(ItemStack stack1, ItemStack stack2) {
        return stack1.getDamageValue() - stack2.getDamageValue();
    }

    public static int fallbackNBTCompare(final ItemStack first, final ItemStack second) {
        if (ItemStack.isSameItemSameComponents(first, second)) {
            return 0;
        }

        CustomData firstData = first.get(DataComponents.CUSTOM_DATA);
        CustomData secondData = second.get(DataComponents.CUSTOM_DATA);

        if (firstData != null && secondData == null) {
            return 1;
        } else if (firstData == null && secondData != null) {
            return -1;
        } else if (firstData == null) {
            return 0;
        }

        CompoundTag firstTag = firstData.copyTag();
        CompoundTag secondTag = secondData.copyTag();

        if (NbtUtils.compareNbt(firstTag, secondTag, true)) {
            return 0;
        }

        int sizeCompare = Comparator.comparingInt(CompoundTag::size).compare(firstTag, secondTag);
        return sizeCompare != 0 ? sizeCompare : firstTag.toString().compareTo(secondTag.toString());
    }

    public static int potionComplexityCompare(final ItemStack first, final ItemStack second) {
        return Integer.compare(potionPower(second), potionPower(first));
    }

    private static int potionPower(final ItemStack stack) {
        return PotionUtils.getPotion(stack)
                .map(Potion::getEffects)
                .map(effects -> effects.stream().mapToInt(effect -> (effect.getAmplifier() + 1) * Math.max(1, effect.getDuration())).sum())
                .orElse(0);
    }

    public static int potionTypeCompare(final ItemStack first, final ItemStack second) {
        Potion firstPotion = PotionUtils.getPotion(first).orElse(null);
        Potion secondPotion = PotionUtils.getPotion(second).orElse(null);

        if (firstPotion == null) {
            return secondPotion == null ? 0 : 1;
        }

        if (secondPotion == null) {
            return -1;
        }

        return BuiltInRegistries.POTION.getId(secondPotion) - BuiltInRegistries.POTION.getId(firstPotion);
    }

    private enum ItemType {
        FOOD(stack -> stack.has(DataComponents.FOOD), FOOD_COMPARATOR),
        TORCH(list(Blocks.TORCH)),
        TOOL_SHOVEL(classPredicate(ShovelItem.class), TOOL_COMPARATOR),
        TOOL_AXE(classPredicate(AxeItem.class), TOOL_COMPARATOR),
        TOOL_SWORD(SortingHandler::isSwordLike, SWORD_COMPARATOR),
        ARMOR(stack -> equipmentSlot(stack).getType() == EquipmentSlot.Type.HUMANOID_ARMOR, ARMOR_COMPARATOR),
        BOW(classPredicate(BowItem.class), BOW_COMPARATOR),
        CROSSBOW(classPredicate(CrossbowItem.class), BOW_COMPARATOR),
        TRIDENT(classPredicate(TridentItem.class), BOW_COMPARATOR),
        TOOL_GENERIC(stack -> stack.has(DataComponents.TOOL), TOOL_COMPARATOR),
        ARROWS(classPredicate(ArrowItem.class)),
        POTION(classPredicate(PotionItem.class), POTION_COMPARATOR),
        TIPPED_ARROW(classPredicate(TippedArrowItem.class), POTION_COMPARATOR),
        MINECART(classPredicate(MinecartItem.class)),
        RAIL(list(Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.ACTIVATOR_RAIL)),
        DYE(classPredicate(DyeItem.class)),
        ANY(inverseClassPredicate(BlockItem.class)),
        BLOCK(classPredicate(BlockItem.class));

        private final Predicate<ItemStack> predicate;
        private final Comparator<ItemStack> comparator;

        ItemType(final List<Item> list) {
            this(itemPredicate(list), jointComparator(listOrderComparator(list), new ArrayList<>()));
        }

        ItemType(final Predicate<ItemStack> predicate) {
            this(predicate, FALLBACK_COMPARATOR);
        }

        ItemType(final Predicate<ItemStack> predicate, final Comparator<ItemStack> comparator) {
            this.predicate = predicate;
            this.comparator = comparator;
        }

        public boolean fitsInType(final ItemStack stack) {
            return predicate.test(stack);
        }
    }
}
