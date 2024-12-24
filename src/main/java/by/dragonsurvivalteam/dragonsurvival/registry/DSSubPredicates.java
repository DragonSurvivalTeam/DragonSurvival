package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.CustomPredicates;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.EntityCheckPredicate;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DSSubPredicates {
    public static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, DragonSurvival.MODID);

    static {
        REGISTRY.register("dragon_predicate", () -> DragonPredicate.CODEC);
        REGISTRY.register("entity_check_predicate", () -> EntityCheckPredicate.CODEC);
        REGISTRY.register("custom_predicates", () -> CustomPredicates.CODEC);
    }
}
