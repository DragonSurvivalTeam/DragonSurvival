package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.HitByWaterPotionTrigger;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.ItemUsedTrigger;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(ThrownPotion.class)
public class ThrownPotionMixin {

    @ModifyArg(method = "applyWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private Predicate<LivingEntity> considerDragonsInApplyWater(Predicate<LivingEntity> predicate) {
        return predicate.or(DragonStateProvider::isDragon);
    }

    @ModifyExpressionValue(method = "applyWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSensitiveToWater()Z"))
    private boolean allowEffectApplyToDragonsWithWeaknessToIt(boolean original, @Local LivingEntity instance, @Share("penaltyLocalRef") LocalRef<DragonPenalty> penaltyLocalRef) {
        if(instance instanceof ServerPlayer player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return original;
            }

            for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
                //noinspection DeconstructionCanBeUsed -> spotless is too stupid to handle this
                if (penalty.value().trigger() instanceof HitByWaterPotionTrigger && penalty.value().condition().map(condition -> condition.test(Condition.createContext(player))).orElse(true)) {
                    penaltyLocalRef.set(penalty.value());
                    return true;
                }
            }
        }

        return original;
    }

    @WrapOperation(method = "applyWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean applyEffectToDragonsWithWeaknessToIt(LivingEntity instance, DamageSource damageSource, float amount, Operation<Boolean> original, @Share("penaltyLocalRef") LocalRef<DragonPenalty> penaltyLocalRef) {
        if(penaltyLocalRef.get() != null) {
            penaltyLocalRef.get().apply((ServerPlayer) instance);
            return false;
        } else {
            return original.call(instance, damageSource, amount);
        }
    }
}
