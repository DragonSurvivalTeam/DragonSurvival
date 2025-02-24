package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerHostilesSensor.class)
public class VillagerHostilesSensorMixin {
    /** Checking the distance value of the map in case an entity type has a higher distance value than the effect distance */
    @WrapOperation(method = "isClose", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    public <V> V dragonSurvival$setHunterOmenDistance(final ImmutableMap<EntityType<?>, Float> instance, final Object entityType, final Operation<V> original, @Local(argsOnly = true, ordinal = 1) final LivingEntity target) {
        // avoid NullPointerException by not storing the result in the primitive type
        Float distance = (Float) original.call(instance, entityType);

        if (target.hasEffect(DSEffects.HUNTER_OMEN) && distance == null || distance < 8) {
            distance = 8f;
        }

        //noinspection unchecked -> type is valid
        return (V) distance;
    }

    @ModifyReturnValue(method = "isHostile", at = @At(value = "RETURN"))
    public boolean dragonSurvival$setHunterOmenAsHostile(final boolean isHostile, @Local(argsOnly = true) final LivingEntity entity) {
        if (isHostile) {
            return true;
        }

        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        return entity.hasEffect(DSEffects.HUNTER_OMEN);
    }
}
