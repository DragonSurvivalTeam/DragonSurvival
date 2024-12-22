package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.function.Predicate;

public record LookingAtTarget(Either<BlockTargeting, EntityTargeting> target, LevelBasedValue range) implements AbilityTargeting {
    public static final MapCodec<LookingAtTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("range").forGetter(LookingAtTarget::range)).apply(instance, LookingAtTarget::new)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            BlockHitResult result = getBlockHitResult(dragon, ability);

            if (result.getType() == HitResult.Type.MISS) {
                return;
            }

            if (!blockTarget.matches(dragon.serverLevel(), result.getBlockPos()) || /* This is always checked by the predicate */ !dragon.serverLevel().isLoaded(result.getBlockPos())) {
                return;
            }

            blockTarget.effect().forEach(target -> target.apply(dragon, ability, result.getBlockPos(), result.getDirection()));
        }).ifRight(entityTarget -> {
            Predicate<Entity> filter = entity -> isEntityRelevant(dragon, entityTarget, entity) && entityTarget.matches(dragon.serverLevel(), dragon.position(), entity);
            HitResult result = getEntityHitResult(dragon, filter, ability);

            if (result instanceof EntityHitResult entityHitResult) {
                entityTarget.effect().forEach(target -> target.apply(dragon, ability, entityHitResult.getEntity()));
            }
        });
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        Component targetingComponent = target.map(block -> null, entity -> entity.targetingMode().translation());

        if (targetingComponent == null) {
            return Component.translatable(LangKey.ABILITY_LOOKAT, DSColors.blue(getRange(ability)));
        } else {
            return Component.translatable(LangKey.ABILITY_TO_TARGET_LOOKAT, DSColors.blue(targetingComponent), DSColors.blue(getRange(ability)));
        }
    }

    public BlockHitResult getBlockHitResult(Player dragon, final DragonAbilityInstance ability) {
        Vec3 viewVector = dragon.getViewVector(0);
        return dragon.level().clip(new ClipContext(viewVector, viewVector.scale(range().calculate(ability.level())), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
    }

    public HitResult getEntityHitResult(Player dragon, Predicate<Entity> filter, final DragonAbilityInstance ability) {
        return ProjectileUtil.getHitResultOnViewVector(dragon, filter, range().calculate(ability.level()));
    }

    private float getRange(final DragonAbilityInstance ability) {
        return range().calculate(ability.level());
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
