package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonStagePredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.EntityCheckPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class Condition {
    public static ContextAwarePredicate none() {
        return EntityPredicate.wrap(EntityPredicate.Builder.entity().build());
    }

    public static EntityPredicate defaultNaturalGrowthBlocker() {
        return EntityPredicate.Builder.entity().subPredicate(
                DragonPredicate.Builder.dragon().starHeart(StarHeartItem.State.ACTIVE).build()
        ).build();
    }

    public static EntityPredicate build(final DragonPredicate.Builder builder) {
        return EntityPredicate.Builder.entity().subPredicate(builder.build()).build();
    }

    public static EntityPredicate living() {
        return EntityPredicate.Builder.entity().subPredicate(EntityCheckPredicate.Builder.start().living().build()).build();
    }

    public static EntityPredicate onBlock(final TagKey<Block> tag) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(tag))).build();
    }

    public static EntityPredicate inBlock(final TagKey<Block> tag) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(tag))).build();
    }

    public static BlockPredicate blocks(final Block... blocks) {
        return BlockPredicate.Builder.block().of(blocks).build();
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
