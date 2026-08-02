package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EntityScale;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyReturnValue(method = "getNightVisionScale", at = @At(value = "RETURN"))
    private static float dragonSurvival$modifyNightVisionScale(float original) {
        return ClientConfig.stableNightVision ? 1 : original;
    }

    /**
     * Small scale values have camera / x-ray issues (if the near plane is too far away) <br>
     * - First person: You can view through the block in front of you, if you're too close <br>
     * - Third person: You can view through the blocks to your side, if you're too close
     */
    @ModifyConstant(method = "getProjectionMatrix", constant = @Constant(floatValue = 0.05F))
    private float dragonSurvival$adjustNearPlane(float original) {
        return RenderingUtils.getNearPlane(original);
    }

    /** Adjust intensity of the bobbing animation while walking based on the current scale */
    @ModifyArg(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), index = 0)
    private float dragonSurvival$modifyBobViewTranslateX(final float original) {
        return dragonSurvival$scaleBobViewTranslation(original);
    }

    @ModifyArg(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), index = 1)
    private float dragonSurvival$modifyBobViewTranslateY(final float original) {
        return dragonSurvival$scaleBobViewTranslation(original);
    }

    private float dragonSurvival$scaleBobViewTranslation(final float original) {
        //noinspection DataFlowIssue -> player is present
        float scale = EntityScale.get(Minecraft.getInstance().player);
        return scale < 1 ? original * scale : original;
    }

    /** Prevent the hurt animation from playing when setting the health (due to {@link LocalPlayer#hurtTo(float)}) */
    @Inject(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hurtDuration:I"), cancellable = true)
    private void dragonSurvival$skipHurtAnimation(final PoseStack pose, float partialTicks, final CallbackInfo callback, @Local final LivingEntity entity, @Local final DamageSource damageSource) {
        if (damageSource == null && entity instanceof Player player && DragonStateProvider.isDragon(player)) {
            player.hurtTime = 0;
            callback.cancel();
        }
    }
}
