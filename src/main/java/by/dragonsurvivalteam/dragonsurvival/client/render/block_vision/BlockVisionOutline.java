package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class BlockVisionOutline {
    private static final float OUTLINE_WIDTH = 2.0f;
    private static final float SHADOW_WIDTH = 4.0f;
    private static final RenderPipeline BLOCK_VISION_OUTLINE_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(DragonSurvival.res("pipeline/block_vision_outline"))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();
    private static final RenderType BLOCK_VISION_OUTLINE_TYPE = RenderType.create(
            "block_vision_outline",
            RenderSetup.builder(BLOCK_VISION_OUTLINE_PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    private BlockVisionOutline() {}

    public static void registerRenderPipelines(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(BLOCK_VISION_OUTLINE_PIPELINE);
    }

    public static RenderType renderType() {
        return BLOCK_VISION_OUTLINE_TYPE;
    }

    public static void render(final BlockVisionHandler.Data data, final PoseStack pose, final VertexConsumer buffer, final int colorARGB) {
        pose.pushPose();
        pose.translate(data.x(), data.y(), data.z());

        int alpha = Math.max(ARGB.alpha(colorARGB), 192);
        int visibleColor = ARGB.color(alpha, ARGB.red(colorARGB), ARGB.green(colorARGB), ARGB.blue(colorARGB));
        int shadowColor = ARGB.color(Math.max(alpha / 2, 96), 0, 0, 0);

        ShapeRenderer.renderShape(pose, buffer, Shapes.create(new AABB(0, 0, 0, 1, 1, 1)), 0, 0, 0, shadowColor, SHADOW_WIDTH);
        ShapeRenderer.renderShape(pose, buffer, Shapes.create(new AABB(0, 0, 0, 1, 1, 1)), 0, 0, 0, visibleColor, OUTLINE_WIDTH);
        pose.popPose();
    }
}
