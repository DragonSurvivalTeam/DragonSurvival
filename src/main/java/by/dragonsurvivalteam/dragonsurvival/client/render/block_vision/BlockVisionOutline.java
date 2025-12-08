package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.neoforge.client.GlStateBackup;

public class BlockVisionOutline {
    private static BufferBuilder buffer;
    private static GlStateBackup backup;

    public static void render(final PoseStack pose, final int colorARGB) {
        prepare();
        drawLines(buffer, pose.last(), 0, 0, 0, 1, 1, 1, colorARGB);
    }

    private static void drawLines(final VertexConsumer buffer, final PoseStack.Pose pose, final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ, final int color) {
        drawLine(buffer, pose, minX, minY, minZ, maxX, minY, minZ, 1, 0, 0, color);
        drawLine(buffer, pose, minX, minY, minZ, minX, maxY, minZ, 0, 1, 0, color);
        drawLine(buffer, pose, minX, minY, minZ, minX, minY, maxZ, 0, 0, 1, color);
        drawLine(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, 0, 1, 0, color);
        drawLine(buffer, pose, maxX, maxY, minZ, minX, maxY, minZ, -1, 0, 0, color);
        drawLine(buffer, pose, minX, maxY, minZ, minX, maxY, maxZ, 0, 0, 1, color);
        drawLine(buffer, pose, minX, maxY, maxZ, minX, minY, maxZ, 0, -1, 0, color);
        drawLine(buffer, pose, minX, minY, maxZ, maxX, minY, maxZ, 1, 0, 0, color);
        drawLine(buffer, pose, maxX, minY, maxZ, maxX, minY, minZ, 0, 0, -1, color);
        drawLine(buffer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, 1, 0, 0, color);
        drawLine(buffer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, 0, 1, 0, color);
        drawLine(buffer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, 0, 0, 1, color);
    }

    private static void drawLine(final VertexConsumer buffer, final PoseStack.Pose pose, float fromX, float fromY, float fromZ, float toX, float toY, float toZ, int normalX, int normalY, int normalZ, final int color) {
        buffer.addVertex(pose, fromX, fromY, fromZ).setColor(color).setNormal(pose, normalX, normalY, normalZ);
        buffer.addVertex(pose, toX, toY, toZ).setColor(color).setNormal(pose, normalX, normalY, normalZ);
    }

    public static void beginBatch() {
        backup = new GlStateBackup();
        RenderSystem.backupGlState(backup);
        buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    }

    public static void endBatch() {
        prepare();

        if (buffer != null) {
            MeshData meshData = buffer.build();

            if (meshData != null) {
                BufferUploader.drawWithShader(meshData);
            }
        }

        RenderSystem.restoreGlState(backup);

        backup = null;
        buffer = null;
    }

    private static void prepare() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
    }
}
