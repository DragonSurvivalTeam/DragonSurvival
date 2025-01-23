package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyReturnValue(method = "getNightVisionScale", at = @At(value = "RETURN"))
    private static float dragonSurvival$modifyNightVisionScale(float original) {
        return ClientConfig.stableNightVision ? 1f : original;
    }

    /** To prevent clipping issues (with blocks) at small sizes */
    @ModifyArg(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"), index = 2)
    private float dragonSurvival$adjustNearPlane(float original) {
        //noinspection DataFlowIssue -> player is present
        if (Minecraft.getInstance().player.getScale() < 1) {
            return original * Minecraft.getInstance().player.getScale();
        }

        return original;
    }

    /** Adjust intensity of the bobbing animation while walking based on the current scale */
    @ModifyArgs(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void dragonSurvival$modifyBobViewTranslate(final Args args) {
        //noinspection DataFlowIssue -> player is present
        if (Minecraft.getInstance().player.getScale() < 1) {
            args.set(0, (float) args.get(0) * Minecraft.getInstance().player.getScale());
            args.set(1, (float) args.get(1) * Minecraft.getInstance().player.getScale());
        }
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