package by.dragonsurvivalteam.dragonsurvival.mixins.bettercombat;

import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.AttackAnimationAccess;
import net.bettercombat.client.animation.AttackAnimationSubStack;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractClientPlayer.class, priority = 1500, remap = false)
public abstract class AbstractClientPlayerMixin implements AttackAnimationAccess {
    @Dynamic @Shadow @Final private AttackAnimationSubStack attackAnimation;

    @Override
    public boolean dragonSurvival$hasActiveAnimation() {
        return this.attackAnimation.base.getAnimation() != null && this.attackAnimation.base.getAnimation().isActive();
    }
}