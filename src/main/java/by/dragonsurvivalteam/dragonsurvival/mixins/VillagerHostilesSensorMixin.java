package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.HunterOmenHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerHostilesSensor.class)
public class VillagerHostilesSensorMixin {
    @ModifyReturnValue(method = "isMatchingEntity", at = @At("RETURN"))
    private boolean dragonSurvival$detectHunterOmen(final boolean isMatchingEntity, final LivingEntity villager, final LivingEntity target) {
        if (isMatchingEntity) {
            return true;
        }

        return HunterOmenHandler.avoidPlayer(villager, target);
    }
}
