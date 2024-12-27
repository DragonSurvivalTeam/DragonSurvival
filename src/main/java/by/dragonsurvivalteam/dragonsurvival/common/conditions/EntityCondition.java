package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.CustomPredicates;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.EntityCheckPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class EntityCondition {
    public static EntityPredicate defaultNaturalGrowthBlocker() {
        return EntityPredicate.Builder.entity().subPredicate(
                DragonPredicate.Builder.dragon().starHeart(StarHeartItem.State.ACTIVE).build()
        ).build();
    }

    public static EntityPredicate isLiving() {
        return EntityPredicate.Builder.entity().subPredicate(EntityCheckPredicate.Builder.start().living().build()).build();
    }

    public static EntityPredicate isItem() {
        return EntityPredicate.Builder.entity().subPredicate(EntityCheckPredicate.Builder.start().item().build()).build();
    }

    public static EntityPredicate isOnBlock(final TagKey<Block> tag) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(tag))).build();
    }

    public static EntityPredicate isOnBlock(final Block... blocks) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks))).build();
    }

    public static EntityPredicate isInBlock(final TagKey<Block> tag) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(tag))).build();
    }

    public static EntityPredicate isInFluid(final HolderSet<Fluid> fluids) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setFluid(FluidPredicate.Builder.fluid().of(fluids))).build();
    }

    public static EntityPredicate isInRain() {
        return EntityPredicate.Builder.entity()
                .located(LocationPredicate.Builder.location().setCanSeeSky(true))
                .subPredicate(CustomPredicates.Builder.start().raining(true).build()).build();
    }

    public static EntityPredicate isInSunlight(int sunLightLevel) {
        return EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().sunLightLevel(sunLightLevel).build()).build();
    }

    public static EntityPredicate isInLight(int lightLevel) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.atLeast(lightLevel)))).build();
    }

    @SafeVarargs
    public static EntityPredicate hasEffect(final Holder<MobEffect>... effects) {
        MobEffectsPredicate.Builder builder = MobEffectsPredicate.Builder.effects();

        for (Holder<MobEffect> effect : effects) {
            builder.and(effect);
        }

        return EntityPredicate.Builder.entity().effects(builder).build();
    }
}
