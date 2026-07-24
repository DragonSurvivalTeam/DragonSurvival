package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
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
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Render the human player translucent in first person if they have hunter stacks */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void dragonSurvival$renderTranslucent(final ModelPart instance, final PoseStack poseStack, final VertexConsumer buffer, int packedLight, int packedOverlay, final Operation<Void> original, @Local(argsOnly = true) final MultiBufferSource bufferSource, @Local(argsOnly = true) final AbstractClientPlayer player) {
        if (HunterData.hasTransparency(player)) {
            VertexConsumer translucentBuffer = bufferSource.getBuffer(RenderType.entityTranslucent(player.getSkin().texture()));
            instance.render(poseStack, translucentBuffer, packedLight, packedOverlay, HunterHandler.modifyAlpha(player, -1));
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
