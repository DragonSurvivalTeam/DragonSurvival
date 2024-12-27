package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonStagePredicate;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public class Condition { // TODO :: make advancements use conditions from here / split into separate classes that return BlockPredicate, ItemPredicate etc.
    // Loot Item Condition

    public static LootItemCondition.Builder thisEntity(final EntityPredicate predicate) {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, predicate);
    }

    // Misc.

    public static ContextAwarePredicate none() {
        return EntityPredicate.wrap(EntityPredicate.Builder.entity().build());
    }

    // --- Builder --- //

    public static EntityPredicate.Builder dragonType(final Holder<DragonType> type) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().type(type).build());
    }

    public static EntityPredicate.Builder dragonStage(final Holder<DragonStage> dragonStage) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().stage(dragonStage).build());
    }

    public static EntityPredicate.Builder dragonBody(final Holder<DragonBody> dragonBody) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().body(dragonBody).build());
    }

    public static EntityPredicate.Builder dragonSizeBetween(double min, double max) {
        return EntityPredicate.Builder.entity().subPredicate(
                DragonPredicate.Builder.dragon().stage(DragonStagePredicate.Builder.start().sizeBetween(min, max).build()).build()
        );
    }

    public static EntityPredicate.Builder dragonSizeAtLeast(double min) {
        return EntityPredicate.Builder.entity().subPredicate(
                DragonPredicate.Builder.dragon().stage(DragonStagePredicate.Builder.start().sizeAtLeast(min).build()).build()
        );
    }

    public static EntityPredicate.Builder dragonSizeAtMost(double max) {
        return EntityPredicate.Builder.entity().subPredicate(
                DragonPredicate.Builder.dragon().stage(DragonStagePredicate.Builder.start().sizeAtMost(max).build()).build()
        );
    }
}
