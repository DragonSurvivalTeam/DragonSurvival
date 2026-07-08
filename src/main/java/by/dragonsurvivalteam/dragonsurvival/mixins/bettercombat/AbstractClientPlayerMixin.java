package by.dragonsurvivalteam.dragonsurvival.mixins.bettercombat;

//import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.AttackAnimationAccess;
//import net.bettercombat.client.animation.AttackAnimationSubStack;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AbstractClientPlayer.class, priority = 1500, remap = false)
public abstract class AbstractClientPlayerMixin /*implements AttackAnimationAccess*/ {

    // FIXME :: Better Combat
//    @Dynamic @Shadow @Final private AttackAnimationSubStack attackAnimation;
//
//    @Override
//    public boolean dragonSurvival$hasActiveAnimation() {
//        return this.attackAnimation.base.getAnimation() != null && this.attackAnimation.base.getAnimation().isActive();
//    }

    @ModifyExpressionValue(method = "updateBob", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isSwimming()Z"))
    private boolean dragonSurvival$consideredSwimmingEvenWhenGroundedInWater(final boolean isSwimming) {
        if (isSwimming) {
            return true;
        }

        Player self = (Player) (Object) this;
        return DragonStateProvider.isDragon(self) && DragonEntity.isConsideredSwimmingForAnimation(self);
    }
}