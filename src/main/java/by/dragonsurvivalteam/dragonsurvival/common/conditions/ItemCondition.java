package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class ItemCondition {
    public static ItemPredicate is(final ItemLike... items) {
        return ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, items).build();
    }

    public static ItemPredicate is(final TagKey<Item> tag) {
        return ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, tag).build();
    }

    @SafeVarargs
    public static ItemPredicate hasPotion(final Holder<Potion>... potions) {
        return ItemPredicate.Builder.item().withComponents(
                DataComponentMatchers.Builder.components()
                        .partial(DataComponentPredicates.POTIONS, new PotionsPredicate(HolderSet.direct(potions)))
                        .build()
        ).build();
    }
}
