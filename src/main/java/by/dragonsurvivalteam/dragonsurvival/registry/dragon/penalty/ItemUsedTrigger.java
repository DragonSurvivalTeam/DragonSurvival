package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ItemUsedTrigger(List<ItemPredicate> itemPredicates) implements PenaltyTrigger {
    public static final MapCodec<ItemUsedTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.listOf().fieldOf("item_predicates").forGetter(ItemUsedTrigger::itemPredicates)
    ).apply(instance, ItemUsedTrigger::new));

    @Override
    public boolean matches(final ServerPlayer dragon, final boolean conditionMatched) {
        return conditionMatched;
    }

    @Override
    public boolean hasCustomTrigger() {
        return true;
    }

    public boolean test(final ItemStack item) {
        for (ItemPredicate predicate : itemPredicates) {
            if (predicate.test(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public MapCodec<? extends PenaltyTrigger> codec() {
        return CODEC;
    }
}
