package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;

public record ItemUsedTrigger(ItemPredicate predicate) implements PenaltyTrigger {
    public static final MapCodec<ItemUsedTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.fieldOf("predicate").forGetter(ItemUsedTrigger::predicate)
    ).apply(instance, ItemUsedTrigger::new));

    @Override
    public boolean matches(final ServerPlayer dragon, final boolean conditionMatched) {
        return conditionMatched;
    }

    @Override
    public boolean hasCustomTrigger() {
        return true;
    }

    @Override
    public MapCodec<? extends PenaltyTrigger> codec() {
        return CODEC;
    }
}
