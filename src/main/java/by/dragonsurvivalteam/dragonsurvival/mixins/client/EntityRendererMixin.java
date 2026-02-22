package by.dragonsurvivalteam.dragonsurvival.mixins.client;


import net.minecraft.world.entity.Entity;

// FIXME
public abstract class EntityRendererMixin<T extends Entity> {
    //@Shadow @Final protected EntityRenderDispatcher entityRenderDispatcher;

//    @Inject(method = "render", at = @At("HEAD"))
//    private void dragonSurvival$renderTheftIcon(final T entity, final float entityYaw, final float partialTick, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final CallbackInfo callback) {
////        // If we try to render in a "normal" way through a layer there will be weird left-over pose stack modifications (rotation, position etc.), messing with our icon
////        PillageIconRenderer.renderIcon(entity, poseStack, entityRenderDispatcher.distanceToSqr(entity));
////        SmeltEffectIconRenderer.renderIcon(entity, poseStack, entityRenderDispatcher.distanceToSqr(entity));
//    }
}
