package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonStagePredicate;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Condition {
    private static final LootContextParamSet ABILITY_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.TOOL)
            .required(LootContextParams.ATTACKING_ENTITY)
            .build();

    private static final LootContextParamSet BLOCK_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.TOOL)
            .required(LootContextParams.BLOCK_STATE)
            .optional(LootContextParams.BLOCK_ENTITY)
            .build();

    private static final LootContextParamSet PENALTY_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.TOOL)
            .build();

    private static final LootContextParamSet PROJECTILE_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.ATTACKING_ENTITY)
            .build();

    private static final LootContextParamSet ITEM_CONTEXT = new LootContextParamSet.Builder().required(LootContextParams.TOOL).build();

    public static LootContext itemContext(final ServerLevel level, final ItemStack stack) {
        LootParams parameters = new LootParams.Builder(level).withParameter(LootContextParams.TOOL, stack).create(ITEM_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    public static LootContext penaltyContext(final ServerPlayer dragon) {
        LootParams parameters = new LootParams.Builder(dragon.serverLevel())
                .withParameter(LootContextParams.THIS_ENTITY, dragon)
                .withParameter(LootContextParams.ORIGIN, dragon.position())
                .withParameter(LootContextParams.TOOL, dragon.getMainHandItem())
                .create(PENALTY_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    public static LootContext abilityContext(final ServerPlayer dragon) {
        return abilityContext(dragon, dragon, dragon.position());
    }

    public static LootContext abilityContext(final ServerPlayer attacker, final Entity entity, final Vec3 origin) {
        LootParams parameters = new LootParams.Builder(attacker.serverLevel())
                .withParameter(LootContextParams.ATTACKING_ENTITY, attacker)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.TOOL, entity instanceof LivingEntity livingEntity ? livingEntity.getMainHandItem() : ItemStack.EMPTY)
                .create(ABILITY_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    public static LootContext blockContext(final ServerPlayer dragon, final BlockPos position) {
        return blockContext(dragon, position, dragon.serverLevel().getBlockState(position));
    }

    public static LootContext blockContext(final ServerPlayer dragon, final BlockPos position, final BlockState state) {
        LootParams parameters = new LootParams.Builder(dragon.serverLevel())
                .withParameter(LootContextParams.THIS_ENTITY, dragon)
                .withParameter(LootContextParams.TOOL, dragon.getMainHandItem())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(position))
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, dragon.serverLevel().getBlockEntity(position))
                .create(BLOCK_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    public static LootContext projectileContext(final ServerLevel level, final Projectile projectile, final Entity target) {
        LootParams parameters = new LootParams.Builder(level)
                .withParameter(LootContextParams.ATTACKING_ENTITY, projectile)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .create(PROJECTILE_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    public static LootItemCondition.Builder thisEntity(final EntityPredicate predicate) {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, predicate);
    }

    public static LootItemCondition.Builder tool(final ItemPredicate predicate) {
        return () -> new MatchTool(Optional.of(predicate));
    }

    // Misc.

    public static ContextAwarePredicate none() {
        return EntityPredicate.wrap(EntityPredicate.Builder.entity().build());
    }

    // --- Builder --- //

    public static EntityPredicate.Builder dragonSpecies(final Holder<DragonSpecies> species) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().species(species).build());
    }

    public static EntityPredicate.Builder dragonStage(final Holder<DragonStage> dragonStage) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().stage(dragonStage).build());
    }

    public static EntityPredicate.Builder dragonSizeAtLeast(double min) {
        return EntityPredicate.Builder.entity().subPredicate(
                DragonPredicate.Builder.dragon().stage(DragonStagePredicate.Builder.start().sizeAtLeast(min).build()).build()
        );
    }
}
