package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.CustomPredicates;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonStagePredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.EntityCheckPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

public class Condition { // TODO :: make advancements use conditions from here
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

    public static EntityPredicate onBlock(final Block... blocks) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks))).build();
    }

    public static EntityPredicate inBlock(final TagKey<Block> tag) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(tag))).build();
    }

    public static EntityPredicate inFluid(final HolderSet<Fluid> fluids) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setFluid(FluidPredicate.Builder.fluid().of(fluids))).build();
    }

    public static EntityPredicate eyeInFluid(final Holder<FluidType> fluid) {
        return EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().eyeInFluid(fluid).build()).build();
    }

    public static EntityPredicate inRain() {
        return EntityPredicate.Builder.entity()
                .located(LocationPredicate.Builder.location().setCanSeeSky(true))
                .subPredicate(CustomPredicates.Builder.start().raining(true).build()).build();
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
