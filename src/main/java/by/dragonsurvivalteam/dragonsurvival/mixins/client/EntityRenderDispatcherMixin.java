package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Unique private static boolean dragonSurvival$modifiedPoseStack;

    @Inject(method = "renderShadow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;"))
    private static void dragonSurvival$modifyShadow(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo callback) {
        if (entity instanceof Player player && DragonStateProvider.isDragon(player)) {
            Vector3f offset = ClientDragonRenderer.getDragonCameraOffset(player, partialTicks).negate();
            poseStack.pushPose();
            poseStack.translate(offset.x(), offset.y(), offset.z());
            dragonSurvival$modifiedPoseStack = true;
        }
    }

    // FIXME :: If an 'Inject' cancels after we pushed to the pose stack this will not be called (depending on where that 'Inject' is)
    //  Alternative would be to do the pose stack modifications before the render call, unsure about the performance impact of that though
    @Inject(method = "renderShadow", at = @At(value = "RETURN"))
    private static void dragonSurvival$clearPoseStack(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo callback) {
        if (dragonSurvival$modifiedPoseStack) {
            poseStack.popPose();
            dragonSurvival$modifiedPoseStack = false;
        }
    }

    // TODO: Dragon shadows disappear if you are too big (since you are too far from the ground)
    //  To fix this we would need to mixin to the weight calculation in renderShadow.
}