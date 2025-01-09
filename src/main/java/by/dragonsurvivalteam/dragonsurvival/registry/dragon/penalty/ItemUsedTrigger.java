package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ItemUsedTrigger(HolderSet<Item> items, HolderSet<Potion> potions) implements PenaltyTrigger {
    public static final MapCodec<ItemUsedTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(ItemUsedTrigger::items),
            RegistryCodecs.homogeneousList(Registries.POTION).fieldOf("potions").forGetter(ItemUsedTrigger::potions)
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
