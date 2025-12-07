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
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;

import java.io.IOException;

@EventBusSubscriber(Dist.CLIENT)
public class BlockVisionTreasureShader {
    private static ShaderInstance treasureShader;

    public static void render(final BlockVisionHandler.Data data, final PoseStack pose, final int colorARGB, final int tick, final float partialTick) {
        float animationTime = (tick + partialTick) * 0.02f;

        float red = FastColor.ARGB32.red(colorARGB) / 255f;
        float green = FastColor.ARGB32.green(colorARGB) / 255f;
        float blue = FastColor.ARGB32.blue(colorARGB) / 255f;

        float blockSeed = (data.x() * 0.73f + data.y() * 0.41f + data.z() * 0.29f);
        renderTreasureShader(pose.last().pose(), animationTime, blockSeed, red, green, blue);
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("block_vision_treasure"), DefaultVertexFormat.POSITION_TEX), instance -> treasureShader = instance);
    }

    @SuppressWarnings("DataFlowIssue") // Referenced shader variables should be present
    private static void renderTreasureShader(
            final Matrix4f modelViewMatrix,
            final float timeSeconds,
            final float blockSeed,
            final float red, final float green, final float blue
    ) {
        // Configure uniforms on the shader instance. We rely on ShaderInstance.apply()
        // to bind the program, mirroring how growth_circle is drawn.
        treasureShader.getUniform("Time").set(timeSeconds);
        treasureShader.getUniform("BlockSeed").set(blockSeed);
        // Pass the ore color as a uniform to allow using POSITION_TEX format (pack-friendly like growth_circle)
        treasureShader.getUniform("OreColor").set(red, green, blue);
        // Set standard matrices expected by the shader
        // Use the current pose stack's model-view matrix and the active projection
        treasureShader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
        treasureShader.getUniform("ModelViewMat").set(modelViewMatrix);
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

        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // Z faces
        faceZTexPT(buffer, 0f, 0f, 0f, 1f, 1f);
        faceZTexPT(buffer, 1f, 1f, 0f, 0f, 1f);
        // X faces
        faceXTexPT(buffer, 0f, 1f, 0f, 0f, 1f);
        faceXTexPT(buffer, 1f, 0f, 0f, 1f, 1f);
        // Y faces
        faceYTexPT(buffer, 0f, 0f, 1f, 1f, 0f);
        faceYTexPT(buffer, 1f, 0f, 0f, 1f, 1f);

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

    private static void faceZTexPT(final VertexConsumer buffer, final float z, final float uMin, final float vMin, final float uMax, final float vMax) {
        buffer.addVertex(0f, 0f, z).setUv(uMin, vMin);
        buffer.addVertex(1f, 0f, z).setUv(uMax, vMin);
        buffer.addVertex(1f, 1f, z).setUv(uMax, vMax);
        buffer.addVertex(0f, 1f, z).setUv(uMin, vMax);
    }

    private static void faceXTexPT(final VertexConsumer buffer, final float x, final float uMin, final float vMin, final float uMax, final float vMax) {
        buffer.addVertex(x, 0f, 0f).setUv(uMin, vMin);
        buffer.addVertex(x, 0f, 1f).setUv(uMax, vMin);
        buffer.addVertex(x, 1f, 1f).setUv(uMax, vMax);
        buffer.addVertex(x, 1f, 0f).setUv(uMin, vMax);
    }

    private static void faceYTexPT(final VertexConsumer buffer, final float y, final float uMin, final float vMin, final float uMax, final float vMax) {
        buffer.addVertex(0f, y, 0f).setUv(uMin, vMin);
        buffer.addVertex(1f, y, 0f).setUv(uMax, vMin);
        buffer.addVertex(1f, y, 1f).setUv(uMax, vMax);
        buffer.addVertex(0f, y, 1f).setUv(uMin, vMax);
    }
}
