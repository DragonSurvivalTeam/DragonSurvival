package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
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
import org.joml.Matrix4f;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT)
public class BlockVisionTreasureShader {
    private static ShaderInstance shader;
    private static BufferBuilder buffer;

    public static void render(final BlockVisionHandler.Data data, final PoseStack pose, final int colorARGB, final int tick, final float partialTick) {
        addBlock(data, pose, colorARGB);
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("block_vision_treasure"), DefaultVertexFormat.POSITION_TEX_COLOR), instance -> shader = instance);
    }

    // Begin a batched rendering pass for all treasure blocks in this frame.
    @SuppressWarnings("DataFlowIssue")
    public static void beginBatch() {
        if (buffer != null) {
            return;
        }

        RenderSystem.setShader(() -> shader);
        shader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
        shader.getUniform("ModelViewMat").set(new Matrix4f().identity());
        shader.getUniform("ZBias").set(0.0f);
        shader.apply();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0f, -1.0f);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

        buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    // Add one block's geometry to the current batch.
    public static void addBlock(final BlockVisionHandler.Data data, final PoseStack pose, final int colorARGB) {
        if (buffer == null) {
            return;
        }

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) return;

        var pos = net.minecraft.core.BlockPos.containing(data.x(), data.y(), data.z());
        // TODO :: get this from data
        var state = level.getBlockState(pos);
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);

        // Prepare per-vertex color (semi-transparent)
        int red = FastColor.ARGB32.red(colorARGB);
        int green = FastColor.ARGB32.green(colorARGB);
        int blue = FastColor.ARGB32.blue(colorARGB);
        // TODO :: make this configurable
        int alpha = 64;

        var modelData = model.getModelData(level, pos, state, ModelData.EMPTY);

        long seed = state.getSeed(pos);
        RandomSource rand = RandomSource.create();

        // Apply model offset (e.g., for some cutout blocks)
        var offset = state.getOffset(level, pos);
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);
        PoseStack.Pose local = pose.last();
        pose.popPose();

        rand.setSeed(seed);
        // Unculled faces
        for (BakedQuad quad : model.getQuads(state, null, rand, modelData, null)) {
            buffer.putBulkData(local, quad, red / 255f, green / 255f, blue / 255f, alpha / 255f, -1, -1);
        }

        // Culled faces
        for (Direction dir : Direction.values()) {
            rand.setSeed(seed);
            for (BakedQuad quad : model.getQuads(state, dir, rand, modelData, null)) {
                buffer.putBulkData(local, quad,red / 255f, green / 255f, blue / 255f, alpha / 255f, -1, -1);
            }
        }
    }

    // End the current batched pass and draw once.
    public static void endBatch() {
        if (buffer == null) {
            return;
        }

        MeshData meshData = buffer.build();

        if (meshData != null) {
            BufferUploader.draw(meshData);
        }

        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        shader.clear();
        buffer = null;
    }
}
