package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clear target of the exploding creeper if the player gained max. hunter stacks */
@Mixin(SwellGoal.class)
public abstract class SwellGoalMixin {
    @Shadow @Nullable private LivingEntity target;

    @Inject(method = "tick", at = @At("HEAD"))
    private void dragonSurvival$clearCurrentTarget(final CallbackInfo callback) {
        if (target == null) {
            return;
        }

        if (target.getData(DSDataAttachments.HUNTER).hasMaxHunterStacks()) {
            target = null;
        }
    }
}
