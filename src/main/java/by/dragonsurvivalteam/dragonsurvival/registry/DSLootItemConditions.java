package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.MatchItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DSLootItemConditions {
    public static final DeferredRegister<LootItemConditionType> REGISTRY = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE, DragonSurvival.MODID);
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> MATCH_ITEM = REGISTRY.register("match_item", () -> new LootItemConditionType(MatchItem.CODEC));
}
