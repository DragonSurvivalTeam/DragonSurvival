package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.SpearmanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;

public class SpearmanRenderer extends DynamicGeoEntityRenderer<SpearmanEntity> {
    public SpearmanRenderer(EntityRendererProvider.Context renderManager, GeoModel<SpearmanEntity> model) {
        super(renderManager, model);
    }

    @Override
    public void preRender(final PoseStack poseStack, final SpearmanEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        Minecraft.getInstance().getProfiler().push("spearman");
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }

    @Override
    public void postRender(final PoseStack poseStack, final SpearmanEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
        Minecraft.getInstance().getProfiler().pop();
    }
}
