package by.dragonsurvivalteam.dragonsurvival.registry.projectile;

import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects.ProjectileWorldEffect;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public interface ProjectileEffect<T> {
    LootContextParamSet POSITION_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .required(LootContextParams.BLOCK_STATE)
            .build();

    LootContextParamSet ENTITY_CONTEXT = new LootContextParamSet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ATTACKING_ENTITY)
            .required(LootContextParams.ORIGIN)
            .build();

    Codec<ProjectileEffect<?>> GENERIC_CODEC = Codec.either(
                    ProjectileBlockEffect.CODEC,
                    Codec.either(ProjectileEntityEffect.CODEC, ProjectileWorldEffect.CODEC))
            .flatXmap(
                    either -> either.map(
                            DataResult::success, other -> other.map(DataResult::success, DataResult::success)),
                    effect -> {
                        if (effect instanceof ProjectileBlockEffect blockEffect) {
                            return DataResult.success(Either.left(blockEffect));
                        }

                        if (effect instanceof ProjectileEntityEffect entityEffect) {
                            return DataResult.success(Either.right(Either.left(entityEffect)));
                        }

                        if (effect instanceof ProjectileWorldEffect worldEffect) {
                            return DataResult.success(Either.right(Either.right(worldEffect)));
                        }

                        return DataResult.error(() -> "Invalid ProjectileEffect type: [" + effect.getClass().getName() + "]");
                    });

    static LootContext positionContext(final ServerLevel level, final Projectile projectile, final Vec3 origin) {
        LootParams parameters = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, projectile)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(BlockPos.containing(origin)))
                .create(POSITION_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    static LootContext entityContext(final ServerLevel level, final Projectile projectile, final Entity target) {
        LootParams parameters = new LootParams.Builder(level)
                .withParameter(LootContextParams.ATTACKING_ENTITY, projectile)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .create(ENTITY_CONTEXT);
        return new LootContext.Builder(parameters).create(Optional.empty());
    }

    default boolean applyGeneric(final Projectile projectile, final Object target, final int level) {
        //noinspection IfCanBeSwitch -> spotless is too stupid to handle this
        if (this instanceof ProjectileBlockEffect blockEffect && target instanceof BlockPos position) {
            blockEffect.apply(projectile, position, level);
            return true;
        }

        if (this instanceof ProjectileEntityEffect entityEffect && target instanceof Entity entity) {
            entityEffect.apply(projectile, entity, level);
            return true;
        }

        if (this instanceof ProjectileWorldEffect worldEffect) {
            worldEffect.apply(projectile, null, level);
            return true;
        }

        return false;
    }

    default List<MutableComponent> getDescription(final Player dragon, final int level) {
        return List.of();
    }

    void apply(final Projectile projectile, final T target, final int level);

    MapCodec<? extends ProjectileEffect<T>> codec();
}
