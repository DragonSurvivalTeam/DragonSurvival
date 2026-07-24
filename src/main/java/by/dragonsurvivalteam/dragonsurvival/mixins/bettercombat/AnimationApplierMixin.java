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
    public void dragonSurvival$offsetAttackAnimation(final String partName, final ModelPart part, final CallbackInfo callback) {
        if (BetterCombat.isAttacking(BetterCombat.CURRENT_PLAYER)) {
            if (partName.equals("rightArm") || partName.equals("leftArm")) {
                DragonStateHandler handler = DragonStateProvider.getData(BetterCombat.CURRENT_PLAYER);

                if (handler.isDragon()) {
                    // 'y' has a default value of 2 - can't find anything to offset it without an extra field
                    // Seems to be dependent on the eye height, but there is no proper reference value to work with it
                    // (Since 'y' will always be 2, we don't have access to the position that scales with the eye height or sth. like that)
                    part.setPos(part.x, (float) (part.y + handler.body().value().betterCombatWeaponOffset()), part.z);
                }
            }
        }
    }
}