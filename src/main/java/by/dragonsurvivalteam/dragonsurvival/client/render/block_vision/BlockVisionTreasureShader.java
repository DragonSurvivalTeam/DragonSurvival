package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT)
public class BlockVisionTreasureShader {
    private static ShaderInstance treasureShader;

    public static void render(final BlockVisionHandler.Data data, final PoseStack pose, final int colorARGB, final int tick, final float partialTick) {
        float animationTime = (tick + partialTick) * 0.02f;

        float red = FastColor.ARGB32.red(colorARGB) / 255f;
        float green = FastColor.ARGB32.green(colorARGB) / 255f;
        float blue = FastColor.ARGB32.blue(colorARGB) / 255f;

        float blockSeed = (data.x() * 0.73f + data.y() * 0.41f + data.z() * 0.29f);
        renderTreasureShader(data, pose, animationTime, blockSeed, red, green, blue);
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("block_vision_treasure"), DefaultVertexFormat.POSITION_TEX), instance -> treasureShader = instance);
    }

    @SuppressWarnings("DataFlowIssue") // Referenced shader variables should be present
    private static void renderTreasureShader(
            final BlockVisionHandler.Data data,
            final PoseStack pose,
            final float timeSeconds,
            final float blockSeed,
            final float red, final float green, final float blue
    ) {
        // Bind our custom shader through RenderSystem, then upload uniforms (matches vanilla pattern)
        RenderSystem.setShader(() -> treasureShader);
        // Configure uniforms on the shader instance
        treasureShader.getUniform("Time").set(timeSeconds);
        treasureShader.getUniform("BlockSeed").set(blockSeed);
        // Pass the ore color as a uniform to allow using POSITION_TEX format (pack-friendly like growth_circle)
        treasureShader.getUniform("OreColor").set(red, green, blue);
        // Set standard matrices expected by the shader
        // Use the current pose stack's model-view matrix and the active projection
        treasureShader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
        treasureShader.getUniform("ModelViewMat").set(pose.last().pose());
        // Disable vertex Z bias (set to 0) — we'll rely on polygon offset to avoid distance-scaling artifacts
        treasureShader.getUniform("ZBias").set(0.0f);
        treasureShader.apply();

        // Local render state for translucent coplanar quads
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Draw translucent without writing to depth, so we don't block other translucency
        RenderSystem.depthMask(false);
        // Render all faces (front/back) so exposed blocks show a full shell
        RenderSystem.disableCull();
        // Use polygon offset to bias depth slightly toward the camera so coplanar quads
        // do not z-fight with the block faces, without introducing a visible spatial gap
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0f, -1.0f);

        // Bind the block atlas for alpha masking in the shader
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

        // Draw the actual baked model quads with atlas UVs so the shader can mask by texture alpha
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;

        var pos = net.minecraft.core.BlockPos.containing(data.x(), data.y(), data.z());
        // TODO :: store it in data instead of Block
        var state = level.getBlockState(pos);
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);

        // Apply per-position model offset used by some cutout blocks (e.g., grass, flowers)
        var offset = state.getOffset(level, pos);
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);

        // TODO :: fix animated sprites not matching up

        // Make sure the adjusted position is passed to the shader
        treasureShader.getUniform("ModelViewMat").set(pose.last().pose());
        treasureShader.apply();

        // Obtain model data for dynamic/baked models
        var modelData = model.getModelData(level, pos, state, ModelData.EMPTY);

        // Use a deterministic random seeded from the state/pos like vanilla, reset per call
        long seed = state.getSeed(pos);
        RandomSource rand = RandomSource.create(seed);

        // General (unculled) quads
        rand.setSeed(seed);
        emitQuads(model, state, null, modelData, rand, buffer);

        // Directional quads
        for (Direction dir : Direction.values()) {
            rand.setSeed(seed);
            emitQuads(model, state, dir, modelData, rand, buffer);
        }

        pose.popPose();

        BufferUploader.draw(buffer.buildOrThrow());

        // Restore polygon offset state
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();

        // Restore local state
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        // Clear to avoid leaking state
        treasureShader.clear();
    }

    private static void emitQuads(final BakedModel model, final net.minecraft.world.level.block.state.BlockState state,
                                  final Direction face, final ModelData modelData, final RandomSource rand,
                                  final VertexConsumer buffer) {
        for (var quad : model.getQuads(state, face, rand, modelData, null)) {
            int[] v = quad.getVertices();
            // Each vertex uses 8 ints in the vanilla BLOCK format: xyz, color, uv, light, normal
            // Decode 4 vertices
            for (int i = 0; i < 4; i++) {
                int base = i * 8;
                float x = Float.intBitsToFloat(v[base]);
                float y = Float.intBitsToFloat(v[base + 1]);
                float z = Float.intBitsToFloat(v[base + 2]);
                float u = Float.intBitsToFloat(v[base + 4]);
                float w = Float.intBitsToFloat(v[base + 5]);
                buffer.addVertex(x, y, z).setUv(u, w);
            }
        }
    }
}
