package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.HitByWaterPotionTrigger;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

/** Handles the {@link HitByWaterPotionTrigger} penalty. */
@Mixin(AbstractThrownPotion.class)
public class AbstractThrownPotionMixin {
    @ModifyArg(method = "onHitAsWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private Predicate<LivingEntity> dragonSurvival$considerDragons(final Predicate<LivingEntity> predicate) {
        return predicate.or(DragonStateProvider::isDragon);
    }

    @ModifyExpressionValue(method = "onHitAsWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSensitiveToWater()Z"))
    private boolean dragonSurvival$storePenalty(final boolean isSensitiveToWater, @Local final LivingEntity livingEntity, @Share("penaltyReference") final LocalRef<Holder<DragonPenalty>> penaltyReference) {
        if (!(livingEntity instanceof ServerPlayer player)) {
            return isSensitiveToWater;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        if (!handler.isDragon()) {
            return isSensitiveToWater;
        }

        for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
            if (penalty.value().trigger() instanceof HitByWaterPotionTrigger && penalty.value().condition().map(condition -> condition.test(Condition.penaltyContext(player))).orElse(true)) {
                penaltyReference.set(penalty);
                return true;
            }
        }

        return isSensitiveToWater;
    }

    @WrapOperation(method = "onHitAsWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean dragonSurvival$applyPenalty(LivingEntity livingEntity, ServerLevel level, DamageSource source, float damage, Operation<Boolean> original, @Share("penaltyReference") final LocalRef<Holder<DragonPenalty>> penaltyReference) {
        if (livingEntity instanceof ServerPlayer player) {
            Holder<DragonPenalty> penalty = penaltyReference.get();

            if (penalty != null) {
                penalty.value().apply(player, penalty);
                return false;
            }
        }

        return original.call(livingEntity, level, source, damage);
    }
}
