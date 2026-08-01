package by.dragonsurvivalteam.dragonsurvival.mixins.bettercombat;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.BetterCombat;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = AnimationApplier.class, remap = false)
public abstract class AnimationApplierMixin {
    @Inject(method = "updatePart", at = @At("TAIL"))
    private void dragonSurvival$offsetAttackAnimation(final String partName, final ModelPart part, final CallbackInfo callback) {
        if (BetterCombat.isAttacking(BetterCombat.CURRENT_PLAYER) && (partName.equals("rightArm") || partName.equals("leftArm"))) {
            DragonStateHandler handler = DragonStateProvider.getData(BetterCombat.CURRENT_PLAYER);
            if (handler.isDragon()) {
                part.setPos(part.x, (float) (part.y + handler.body().value().betterCombatWeaponOffset()), part.z);
            }
        }
    }
}
