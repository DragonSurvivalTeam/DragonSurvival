package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.PillageIconRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.SmeltEffectIconRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Shadow @Final protected EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "render", at = @At("HEAD"))
    private void dragonSurvival$renderTheftIcon(final T entity, final float entityYaw, final float partialTick, final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight, final CallbackInfo callback) {
        // If we try to render in a "normal" way through a layer there will be weird left-over pose stack modifications (rotation, position etc.), messing with our icon
        PillageIconRenderer.renderIcon(entity, poseStack, entityRenderDispatcher.distanceToSqr(entity));
        SmeltEffectIconRenderer.renderIcon(entity, poseStack, entityRenderDispatcher.distanceToSqr(entity));
    }
}
