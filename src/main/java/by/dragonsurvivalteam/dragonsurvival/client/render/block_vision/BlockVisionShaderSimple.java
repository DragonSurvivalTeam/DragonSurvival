package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.ArrayList;
import java.util.List;

public final class BlockVisionShaderSimple {
    private static final Identifier BLOCK_VISION_SHADER = DragonSurvival.res("core/block_vision_simple");

    private static final RenderPipeline BLOCK_VISION_SHADER_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(DragonSurvival.res("pipeline/block_vision_shader"))
            .withVertexShader(BLOCK_VISION_SHADER)
            .withFragmentShader(BLOCK_VISION_SHADER)
            .withSampler("Sampler0") // Block texture atlas
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, /* It's an overlay */ false, /* Prevent z-fighting */ -1, -10))
            .build();

    private static final RenderType BLOCK_VISION_SHADER_TYPE = RenderType.create(
            "block_vision_shader",
            RenderSetup.builder(BLOCK_VISION_SHADER_PIPELINE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .createRenderSetup()
    );

    public static void registerRenderPipelines(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(BLOCK_VISION_SHADER_PIPELINE);
    }

    public static RenderType renderType() {
        // TODO :: check if cutout blocks (e.g. plants) still need their own render type
        return BLOCK_VISION_SHADER_TYPE;
    }

    public static void render(final BlockVisionHandler.Data data, final PoseStack pose, final VertexConsumer buffer, final int colorARGB) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null) {
            return;
        }

        BlockPos position = BlockPos.containing(data.x(), data.y(), data.z());
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(data.state());

        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(level, position, data.state(), RandomSource.create(data.state().getSeed(position)), parts);

        if (parts.isEmpty()) {
            return;
        }

        pose.pushPose();
        pose.translate(data.x(), data.y(), data.z());
        // Apply the randomized offset some blocks can have to their position
        Vec3 offset = data.state().getOffset(position);
        pose.translate(offset.x, offset.y, offset.z);

        QuadInstance quadInstance = new QuadInstance();
        quadInstance.setColor(colorARGB);
        quadInstance.setLightCoords(LightCoordsUtil.FULL_BRIGHT);
        quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

        PoseStack.Pose lastPose = pose.last();

        for (BlockStateModelPart part : parts) {
            // Unculled faces
            putQuads(buffer, lastPose, part.getQuads(null), quadInstance);

            // Culled faces
            for (Direction direction : Direction.values()) {
                putQuads(buffer, lastPose, part.getQuads(direction), quadInstance);
            }
        }

        pose.popPose();
    }

    private static void putQuads(final VertexConsumer buffer, final PoseStack.Pose pose, final List<BakedQuad> quads, final QuadInstance quadInstance) {
        quads.forEach(quad -> buffer.putBakedQuad(pose, quad, quadInstance));
    }
}
