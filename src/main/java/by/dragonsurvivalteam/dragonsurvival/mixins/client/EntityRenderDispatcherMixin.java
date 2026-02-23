package by.dragonsurvivalteam.dragonsurvival.mixins.client;


// FIXME
//@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
//@Unique private static boolean dragonSurvival$modifiedPoseStack;

//    @Inject(method = "renderShadow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;"))
//    private static void dragonSurvival$modifyShadow(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo callback) {
//        if (entity instanceof Player player && DragonStateProvider.isDragon(player) && !DragonSurvival.PROXY.dragonRenderingWasCancelled(player)) {
//            Vector3f offset = ClientDragonRenderer.getModelShadowOffset(player, partialTicks).negate();
//            poseStack.pushMatrix();
//            poseStack.translate(offset.x(), offset.y(), offset.z());
//            dragonSurvival$modifiedPoseStack = true;
//        }
//    }
//
//    // FIXME :: If an 'Inject' cancels after we pushed to the pose stack this will not be called (depending on where that 'Inject' is)
//    //  Alternative would be to do the pose stack modifications before the render call, unsure about the performance impact of that though
//    @Inject(method = "renderShadow", at = @At(value = "RETURN"))
//    private static void dragonSurvival$clearPoseStack(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo callback) {
//        if (dragonSurvival$modifiedPoseStack) {
//            poseStack.popMatrix();
//            dragonSurvival$modifiedPoseStack = false;
//        }
//    }

    // TODO: Dragon shadows disappear if you are too big (since you are too far from the ground)
    //  To fix this we would need to mixin to the weight calculation in renderShadow.
}