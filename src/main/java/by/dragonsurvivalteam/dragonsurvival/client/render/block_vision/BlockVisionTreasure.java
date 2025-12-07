package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class BlockVisionTreasure {
    private static final int CPU_PIXEL_GRID = 16;              // cells per axis (matches shader PIXEL_GRID)
    private static final float CPU_COVERAGE_FLOOR = 0.35f;     // minimum coverage
    private static final float CPU_SPARKLE_DENSITY = 0.15f;    // fraction of cells that sparkle
    private static final float CPU_SPARKLE_LIFE = 1.80f;       // seconds per sparkle cycle (slightly longer)
    private static final float CPU_SPARKLE_BRIGHTNESS = 0.65f; // brightness scale (subtler)
    private static final float CPU_BASE_ALPHA = 0.50f;         // overall transparency (like BASE_ALPHA)
    private static final float CPU_EDGE_FEATHER = 0.06f;       // soft edge inside cell (approximation)
    private static final float CPU_ACTIVE_BUCKETS = 4.0f;
    private static final float CPU_BUCKET_SPEED = 0.35f;

    // FIXME :: has pretty bad performance
    public static void render(final BlockVisionHandler.Data data, final PoseStack pose, int colorARGB, final int tick, final float partialTick) {
        setup();

        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float blockSeed = (data.x() * 0.73f + data.y() * 0.41f + data.z() * 0.29f);
        float timeSeconds = (tick + partialTick) * 0.02f;
        Matrix4f matrix = pose.last().pose();

        emitFaceGrid(buffer, matrix, colorARGB, timeSeconds, blockSeed);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        cleanup();
    }

    private static void setup() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0f, -1.0f);
    }

    private static void cleanup() {
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void emitFaceGrid(final VertexConsumer buffer, final Matrix4f matrix, final int baseArgb, final float timeSeconds, final float blockSeed) {
        float step = 1f / CPU_PIXEL_GRID;

        for (int y = 0; y < CPU_PIXEL_GRID; y++) {
            float v0 = y * step;
            float v1 = v0 + step;
            float vCenter = (v0 + v1) * 0.5f;

            for (int x = 0; x < CPU_PIXEL_GRID; x++) {
                float u0 = x * step;
                float u1 = u0 + step;

                float uCenter = (u0 + u1) * 0.5f;
                float cellAlpha = cpuSparkleAlpha(x, y, uCenter, vCenter, timeSeconds, blockSeed);
                int argb = withAlpha(baseArgb, Math.round(255f * cellAlpha));

                buffer.addVertex(matrix, 0, v0, u0).setColor(argb);
                buffer.addVertex(matrix, 0, v0, u1).setColor(argb);
                buffer.addVertex(matrix, 0, v1, u1).setColor(argb);
                buffer.addVertex(matrix, 0, v1, u0).setColor(argb);

                buffer.addVertex(matrix, 1, v0, u0).setColor(argb);
                buffer.addVertex(matrix, 1, v0, u1).setColor(argb);
                buffer.addVertex(matrix, 1, v1, u1).setColor(argb);
                buffer.addVertex(matrix, 1, v1, u0).setColor(argb);

                buffer.addVertex(matrix, u0, 0, v0).setColor(argb);
                buffer.addVertex(matrix, u1, 0, v0).setColor(argb);
                buffer.addVertex(matrix, u1, 0, v1).setColor(argb);
                buffer.addVertex(matrix, u0, 0, v1).setColor(argb);

                buffer.addVertex(matrix, u0, 1, v0).setColor(argb);
                buffer.addVertex(matrix, u1, 1, v0).setColor(argb);
                buffer.addVertex(matrix, u1, 1, v1).setColor(argb);
                buffer.addVertex(matrix, u0, 1, v1).setColor(argb);

                buffer.addVertex(matrix, u0, v0, 0).setColor(argb);
                buffer.addVertex(matrix, u1, v0, 0).setColor(argb);
                buffer.addVertex(matrix, u1, v1, 0).setColor(argb);
                buffer.addVertex(matrix, u0, v1, 0).setColor(argb);

                buffer.addVertex(matrix, u0, v0, 1).setColor(argb);
                buffer.addVertex(matrix, u1, v0, 1).setColor(argb);
                buffer.addVertex(matrix, u1, v1, 1).setColor(argb);
                buffer.addVertex(matrix, u0, v1, 1).setColor(argb);
            }
        }
    }

    private static float cpuSparkleAlpha(final int cellX, final int cellY, final float uCenter, final float vCenter, final float timeSeconds, final float blockSeed) {
        // Participation based on hashed cell index
        float participationSelector = hash01(cellX * 37.0f + cellY * 131.0f + blockSeed * 19.0f + 13.79f);
        if (participationSelector < (1.0f - CPU_SPARKLE_DENSITY)) {
            return CPU_COVERAGE_FLOOR * CPU_BASE_ALPHA; // still apply floor later; return minimal here
        }

        // Smooth activity gate per cell:
        // Use a per-cell phased pulse that rises and falls smoothly over time to avoid abrupt cut-offs.
        float gatePhase = (float)(timeSeconds * CPU_BUCKET_SPEED + blockSeed * 0.13f +
                hash01(cellX * 5.123f + cellY * 9.871f + blockSeed * 2.71f));
        // fract
        gatePhase = gatePhase - (float)Math.floor(gatePhase);
        float gateIn  = smooth01(clamp01(gatePhase / 0.20f));
        float gateOut = 1.0f - smooth01(clamp01((gatePhase - 0.80f) / 0.20f));
        float gate    = gateIn * gateOut; // 0..1 bell

        // Phase jitter per cell
        float phaseJitter = hash01(cellX * 91.17f + cellY * 47.23f + blockSeed * 11.0f);
        float localTime = timeSeconds + phaseJitter * CPU_SPARKLE_LIFE + blockSeed * 0.37f;
        float cyclePhase = (localTime % CPU_SPARKLE_LIFE) / CPU_SPARKLE_LIFE; // 0..1

        // Bell-shaped intensity envelope
        float bell = 1.0f - Math.abs(cyclePhase * 2.0f - 1.0f);
        float envelope = smooth01(bell);
        // Apply smooth per-cell gate to fade sparkles in/out instead of ending at full alpha
        envelope *= gate;

        // Core size over life
        float minHalf = 0.03f;
        float maxHalf = 0.12f;
        float coreHalf = minHalf + (maxHalf - minHalf) * envelope;

        // Soft box membership for center; use edge feather as soft region
        float coreDx = Math.max(0f, Math.abs(uCenter - 0.5f) - coreHalf);
        float coreDy = Math.max(0f, Math.abs(vCenter - 0.5f) - coreHalf);
        float coreSoft = softEdge(coreDx, CPU_EDGE_FEATHER) * softEdge(coreDy, CPU_EDGE_FEATHER);

        // Short rays approximation: add weight if u or v near center
        float reach = 0.25f * envelope; // shorter rays, subtler look
        float rayX = softBox1D(Math.abs(uCenter - 0.5f), reach, CPU_EDGE_FEATHER);
        float rayY = softBox1D(Math.abs(vCenter - 0.5f), reach, CPU_EDGE_FEATHER);

        float sparkleShape = clamp01(coreSoft + 0.9f * (rayX + rayY));
        float sparkle = CPU_SPARKLE_BRIGHTNESS * envelope * sparkleShape;

        float combined = CPU_COVERAGE_FLOOR + sparkle * (1.0f - CPU_COVERAGE_FLOOR);
        return clamp01(CPU_BASE_ALPHA * combined);
    }

    private static float softEdge(float distanceBeyond, float feather) {
        // 1 at inside; smoothly falls to 0 across [0..feather]
        float t = clamp01(1.0f - distanceBeyond / Math.max(1e-6f, feather));
        return t * t * (3.0f - 2.0f * t); // smoothstep
    }

    private static float softBox1D(float halfAbsCoord, float halfSize, float feather) {
        float d = Math.max(0f, halfAbsCoord - halfSize);
        return softEdge(d, feather);
    }

    private static float smooth01(float x) {
        float t = clamp01(x);
        return t * t * (3.0f - 2.0f * t);
    }

    private static float clamp01(float x) {
        return Math.max(0f, Math.min(1f, x));
    }

    private static float hash01(float n) {
        float s = (float) Math.sin(n * 12.9898f + 78.233f);
        return s - (float) Math.floor(s);
    }

    private static int withAlpha(int argb, int alpha) {
        int a = (alpha & 0xFF) << 24;
        return a | (argb & 0x00FFFFFF);
    }
}
