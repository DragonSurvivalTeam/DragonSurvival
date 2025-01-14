package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.util.PotionUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.*;
import java.util.function.Predicate;

/**
 * Copied from Quark <br>
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
        if (player.containerMenu instanceof DragonContainer) {
            InvWrapper wrapper = new InvWrapper(player.getInventory());
            sortInventory(wrapper, 9, 36);
        }
    }

    public static void sortInventory(final IItemHandler handler, final int start, final int end) {
        List<ItemStack> stacks = new ArrayList<>();
        List<ItemStack> restore = new ArrayList<>();

        for (int slot = start; slot < end; slot++) {
            ItemStack stackAt = handler.getStackInSlot(slot);

            restore.add(stackAt.copy());

            if (!stackAt.isEmpty()) {
                stacks.add(stackAt.copy());
            }
        }

        mergeStacks(stacks);
        sortStackList(stacks);

        if (setInventory(handler, stacks, start, end) == InteractionResult.FAIL) {
            setInventory(handler, restore, start, end);
        }
    }

    private static InteractionResult setInventory(final IItemHandler inventory, final List<ItemStack> stacks, final int start, final int end) {
        for (int slot = start; slot < end; slot++) {
            int index = slot - start;

            ItemStack stack = index >= stacks.size() ? ItemStack.EMPTY : stacks.get(index);
            ItemStack stackInSlot = inventory.getStackInSlot(slot);

            if (!stackInSlot.isEmpty()) {
                ItemStack extractTest = inventory.extractItem(slot, inventory.getSlotLimit(slot), true);

                if (extractTest.isEmpty() || extractTest.getCount() != stackInSlot.getCount()) {
                    return InteractionResult.PASS;
                }
            }

            if (!stack.isEmpty() && !inventory.isItemValid(slot, stack)) {
                return InteractionResult.PASS;
            }
        }

        for (int slot = start; slot < end; slot++) {
            // Remove all currently existing items
            inventory.extractItem(slot, inventory.getSlotLimit(slot), false);
        }

        for (int slot = start; slot < end; slot++) {
            int index = slot - start;

            if (index >= stacks.size()) {
                // Since we already extracted all items
                // We don't need to re-fill the slots with empty stacks
                break;
            }

            ItemStack stack = stacks.get(index);

            if (stack.isEmpty()) {
                continue;
            }

            if (!inventory.insertItem(slot, stack, false).isEmpty()) {
                return InteractionResult.FAIL;
            }
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

            if (stackAt.getCount() < stackAt.getMaxStackSize() && ItemStack.isSameItem(stack, stackAt) && ItemStack.isSameItemSameComponents(stack, stackAt)) {
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

    private static Predicate<ItemStack> classPredicate(Class<? extends Item> clazz) {
        return (ItemStack s) -> !s.isEmpty() && clazz.isInstance(s.getItem());
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
                case String string -> {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(string));

                    if (item != Items.AIR) {
                        list.add(item);
                    }
                }
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
        return nutrition(second.getFoodProperties(null)) - nutrition(first.getFoodProperties(null));
    }

    private static float saturation(final FoodProperties properties) {
        if (properties == null) {
            return 0;
        }

        return properties.saturation();
    }

    private static int foodSaturationCompare(final ItemStack first, final ItemStack second) {
        float result = saturation(second.getFoodProperties(null)) - saturation(first.getFoodProperties(null));

        if (result < 0) {
            return 1;
        } else if (result > 0) {
            return -1;
        }

        return 0;
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
        Tier firstTier = ((DiggerItem) first.getItem()).getTier();
        Tier secondTier = ((DiggerItem) second.getItem()).getTier();
        return (int) (secondTier.getSpeed() * 100 - firstTier.getSpeed() * 100);
    }

    private static int swordPowerCompare(final ItemStack first, final ItemStack second) {
        Tier firstTier = ((SwordItem) first.getItem()).getTier();
        Tier secondTier = ((SwordItem) second.getItem()).getTier();
        return (int) (secondTier.getAttackDamageBonus() * 100 - firstTier.getAttackDamageBonus() * 100);
    }

    private static int armorSlotAndToughnessCompare(final ItemStack first, final ItemStack second) {
        ArmorItem firstArmor = (ArmorItem) first.getItem();
        ArmorItem secondArmor = (ArmorItem) second.getItem();

        EquipmentSlot firstSlot = firstArmor.getEquipmentSlot();
        EquipmentSlot secondSlot = secondArmor.getEquipmentSlot();

        if (firstSlot == secondSlot) {
            double firstDefense = firstArmor.getMaterial().value().getDefense(firstArmor.getType()) * (1 + firstArmor.getToughness());
            double secondDefense = secondArmor.getMaterial().value().getDefense(secondArmor.getType()) * (1 + secondArmor.getToughness());

            // To make sure that a difference of 0.3 gets sorted properly
            double result = secondDefense - firstDefense;

            if (result < 0) {
                return 1;
            } else if (result > 0) {
                return -1;
            } else {
                return 0;
            }
        }

        return secondSlot.getIndex() - firstSlot.getIndex();
    }

    public static int damageCompare(ItemStack stack1, ItemStack stack2) {
        return stack1.getDamageValue() - stack2.getDamageValue();
    }

    @SuppressWarnings("deprecation") // tag is not modified
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
            // Both have no custom data
            return 0;
        }

        CompoundTag firstTag = firstData.getUnsafe();
        CompoundTag secondTag = secondData.getUnsafe();

        if (NbtUtils.compareNbt(firstTag, secondTag, true)) {
            return 0;
        }

        return Comparator.comparingInt(CompoundTag::size).compare(firstTag, secondTag);
    }

    public static int potionComplexityCompare(final ItemStack first, final ItemStack second) {
        List<MobEffectInstance> firstEffects = PotionUtils.getPotion(first).map(Potion::getEffects).orElse(Collections.emptyList());
        List<MobEffectInstance> secondEffects = PotionUtils.getPotion(second).map(Potion::getEffects).orElse(Collections.emptyList());

        // TODO :: higher total amplifier should maybe win in general
        //  and if they're the same then duration can be considered?
        int firstTotal = 0;
        int secondTotal = 0;

        for (MobEffectInstance inst : firstEffects) {
            firstTotal += inst.getAmplifier() * inst.getDuration();
        }

        for (MobEffectInstance inst : secondEffects) {
            secondTotal += inst.getAmplifier() * inst.getDuration();
        }

        return secondTotal - firstTotal;
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
        FOOD(stack -> stack.getFoodProperties(null) != null, FOOD_COMPARATOR),
        TORCH(list(Blocks.TORCH)),
        TOOL_PICKAXE(classPredicate(PickaxeItem.class), TOOL_COMPARATOR),
        TOOL_SHOVEL(classPredicate(ShovelItem.class), TOOL_COMPARATOR),
        TOOL_AXE(classPredicate(AxeItem.class), TOOL_COMPARATOR),
        TOOL_SWORD(classPredicate(SwordItem.class), SWORD_COMPARATOR),
        TOOL_GENERIC(classPredicate(DiggerItem.class), TOOL_COMPARATOR),
        ARMOR(classPredicate(ArmorItem.class), ARMOR_COMPARATOR),
        BOW(classPredicate(BowItem.class), BOW_COMPARATOR),
        CROSSBOW(classPredicate(CrossbowItem.class), BOW_COMPARATOR),
        TRIDENT(classPredicate(TridentItem.class), BOW_COMPARATOR),
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