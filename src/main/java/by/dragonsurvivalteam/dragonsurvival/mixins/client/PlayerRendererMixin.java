package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(
        method = "renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;ZLnet/minecraft/client/player/AbstractClientPlayer;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void dragonSurvival$renderTranslucentRightHand(
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector,
        final int lightCoords,
        final Identifier skinTexture,
        final boolean hasSleeve,
        final AbstractClientPlayer player,
        final CallbackInfo callback
    ) {
        if (!dragonSurvival$hasHunterTransparency(player)) {
            return;
        }

        PlayerModel model = dragonSurvival$getModel();
        if (!ClientHooks.renderSpecificFirstPersonArm(poseStack, submitNodeCollector, lightCoords, player, HumanoidArm.RIGHT)) {
            dragonSurvival$renderHand(poseStack, submitNodeCollector, lightCoords, skinTexture, model.rightArm, hasSleeve, player);
        }

        callback.cancel();
    }

    @Inject(
        method = "renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;ZLnet/minecraft/client/player/AbstractClientPlayer;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void dragonSurvival$renderTranslucentLeftHand(
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector,
        final int lightCoords,
        final Identifier skinTexture,
        final boolean hasSleeve,
        final AbstractClientPlayer player,
        final CallbackInfo callback
    ) {
        if (!dragonSurvival$hasHunterTransparency(player)) {
            return;
        }

        PlayerModel model = dragonSurvival$getModel();
        if (!ClientHooks.renderSpecificFirstPersonArm(poseStack, submitNodeCollector, lightCoords, player, HumanoidArm.LEFT)) {
            dragonSurvival$renderHand(poseStack, submitNodeCollector, lightCoords, skinTexture, model.leftArm, hasSleeve, player);
        }

        callback.cancel();
    }

    private void dragonSurvival$renderHand(
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector,
        final int lightCoords,
        final Identifier skinTexture,
        final ModelPart arm,
        final boolean hasSleeve,
        final AbstractClientPlayer player
    ) {
        PlayerModel model = dragonSurvival$getModel();
        arm.resetPose();
        arm.visible = true;
        model.leftSleeve.visible = hasSleeve;
        model.rightSleeve.visible = hasSleeve;
        model.leftArm.zRot = -0.1F;
        model.rightArm.zRot = 0.1F;
        submitNodeCollector.submitModelPart(
            arm,
            poseStack,
            RenderTypes.entityTranslucent(skinTexture),
            lightCoords,
            OverlayTexture.NO_OVERLAY,
            null,
            HunterHandler.modifyAlpha(player, -1),
            null
        );
    }

    private static boolean dragonSurvival$hasHunterTransparency(final AbstractClientPlayer player) {
        float alpha = HunterHandler.calculateAlphaAsFloat(player);
        return alpha != HunterHandler.UNMODIFIED && alpha < 1.0F;
    }

    private PlayerModel dragonSurvival$getModel() {
        return (PlayerModel)((LivingRendererAccessor)this).dragonSurvival$getModel();
    }
}
