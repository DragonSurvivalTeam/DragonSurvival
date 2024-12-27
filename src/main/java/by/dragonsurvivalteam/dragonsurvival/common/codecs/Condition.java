package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonStagePredicate;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Condition {
    private static final LootContextParamSet PLAYER_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.TOOL)
            .build();

    private static final LootContextParamSet BLOCK_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.BLOCK_STATE)
            .required(LootContextParams.ORIGIN)
            .optional(LootContextParams.BLOCK_ENTITY)
            .build();

    public static LootContext createContext(final ServerPlayer dragon) {
        return createContext(dragon.serverLevel(), dragon, dragon.position());
    }

    // TODO :: add parameter for attacking_entity (dragon)
    public static LootContext createContext(final ServerLevel level, final Entity entity, final Vec3 origin) {
        LootParams parameters = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.TOOL, entity instanceof LivingEntity livingEntity ? livingEntity.getMainHandItem() : ItemStack.EMPTY)
                .create(PLAYER_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    public static LootContext createContext(final ServerLevel serverLevel, final BlockPos position) {
        LootParams parameters = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.BLOCK_STATE, serverLevel.getBlockState(position))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(position))
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, serverLevel.getBlockEntity(position))
                .create(BLOCK_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

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
