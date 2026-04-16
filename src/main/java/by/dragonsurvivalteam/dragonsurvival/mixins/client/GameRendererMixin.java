package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;

    @ModifyReturnValue(method = "getNightVisionScale", at = @At(value = "RETURN"))
    private static float dragonSurvival$modifyNightVisionScale(float original) {
        return ClientConfig.stableNightVision ? 1 : original;
    }

    /** Adjust intensity of the bobbing animation while walking based on the current scale */
    @ModifyArgs(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void dragonSurvival$modifyBobViewTranslate(final Args args) {
        //noinspection DataFlowIssue -> player is present
        float scale = Minecraft.getInstance().player.getScale();

        if (scale < 1) {
            args.set(0, (float) args.get(0) * scale);
            args.set(1, (float) args.get(1) * scale);
        }
    }

    /** Prevent the hurt animation from playing when setting the health (due to {@link LocalPlayer#hurtTo(float)}) */
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$skipHurtAnimation(final CameraRenderState cameraState, final PoseStack pose, final CallbackInfo callback) {
        if (minecraft.getCameraEntity() instanceof Player player && DragonStateProvider.isDragon(player) && player.getLastDamageSource() == null) {
            player.hurtTime = 0;
            callback.cancel();
        }
    }
}
