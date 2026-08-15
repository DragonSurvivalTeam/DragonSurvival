package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/** Render the human player translucent in first person if they have hunter stacks */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Unique
    private final Deque<Boolean> dragonSurvival$mountingBonePoseChanges = new ArrayDeque<>();

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void dragonSurvival$applyMountingBoneRotation(final AbstractClientPlayer rider, float entityYaw, float partialTicks, final PoseStack poseStack, final MultiBufferSource buffer, int packedLight, final CallbackInfo callback) {
        boolean changedPose = false;

        if (rider.getVehicle() instanceof Player mount && DragonStateProvider.isDragon(mount)) {
            DragonStateHandler handler = DragonStateProvider.getData(mount);

            if (handler.body().value().mountingOffsets().isEmpty() && !handler.body().value().noDragonModelRendering()) {
                Quaternionf rotation = DragonRenderer.getBoneRotationOrNull(mount, DragonRidingHandler.MOUNTING_BONE);

                if (rotation != null) {
                    Vec3 pivot = rider.getVehicleAttachmentPoint(mount);
                    poseStack.pushPose();
                    poseStack.translate(pivot.x(), pivot.y(), pivot.z());
                    poseStack.mulPose(rotation);
                    poseStack.translate(-pivot.x(), -pivot.y(), -pivot.z());
                    changedPose = true;
                }
            }
        }

        dragonSurvival$mountingBonePoseChanges.addLast(changedPose);
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void dragonSurvival$restoreMountingBoneRotation(final AbstractClientPlayer rider, float entityYaw, float partialTicks, final PoseStack poseStack, final MultiBufferSource buffer, int packedLight, final CallbackInfo callback) {
        if (!dragonSurvival$mountingBonePoseChanges.isEmpty() && dragonSurvival$mountingBonePoseChanges.removeLast()) {
            poseStack.popPose();
        }
    }

    @WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void dragonSurvival$renderTranslucent(final ModelPart instance, final PoseStack poseStack, final VertexConsumer buffer, int packedLight, int packedOverlay, final Operation<Void> original, @Local(argsOnly = true) final MultiBufferSource bufferSource, @Local(argsOnly = true) final AbstractClientPlayer player) {
        if (HunterData.hasTransparency(player)) {
            VertexConsumer translucentBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation()));
            instance.render(poseStack, translucentBuffer, packedLight, packedOverlay, 1, 1, 1, HunterHandler.calculateAlphaAsFloat(player));
        } else {
            original.call(instance, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    @ModifyReturnValue(method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;", at = @At(value = "RETURN"))
    private Vec3 dragonSurvival$removeRenderOffsetForDragons(Vec3 original, @Local(argsOnly = true) AbstractClientPlayer player) {
        if (DragonStateProvider.isDragon(player) && !DragonSurvival.PROXY.dragonRenderingWasCancelled(player)) {
            return Vec3.ZERO;
        }

        return original;
    }
}
