package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DSItemPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class ItemCondition {
    public static DSItemPredicate is(final ItemLike... items) {
        return DSItemPredicate.of(items);
    }

    public static DSItemPredicate is(final TagKey<Item> tag) {
        return DSItemPredicate.of(tag);
    }

    @SafeVarargs
    public static DSItemPredicate hasPotion(final Potion... potions) {
        return DSItemPredicate.potions(potions);
    }
}
