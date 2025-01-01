package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import net.minecraft.advancements.critereon.ItemPotionsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class ItemCondition {
    public static ItemPredicate is(final ItemLike... items) {
        return ItemPredicate.Builder.item().of(items).build();
    }

    @SafeVarargs
    public static ItemPredicate.Builder hasPotion(final Holder<Potion>... potions) {
        return ItemPredicate.Builder.item().withSubPredicate(ItemSubPredicates.POTIONS, new ItemPotionsPredicate(HolderSet.direct(potions)));
    }
}
