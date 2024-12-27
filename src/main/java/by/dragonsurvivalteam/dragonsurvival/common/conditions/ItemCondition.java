package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.level.ItemLike;

public class ItemCondition {
    public static ItemPredicate item(final ItemLike... items) {
        return ItemPredicate.Builder.item().of(items).build();
    }
}
