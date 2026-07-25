package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.client.util.RenderStateBackup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

public class BlockVisionOutline {
    private static BufferBuilder buffer;
    private static RenderStateBackup backup;

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
        buffer.vertex(pose.pose(), fromX, fromY, fromZ).color(color).normal(pose.normal(), normalX, normalY, normalZ).endVertex();
        buffer.vertex(pose.pose(), toX, toY, toZ).color(color).normal(pose.normal(), normalX, normalY, normalZ).endVertex();
    }

    public static void beginBatch() {
        backup = RenderStateBackup.capture();
        buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    }

    public static void endBatch() {
        prepare();

        if (buffer != null) {
            BufferBuilder.RenderedBuffer renderedBuffer = buffer.endOrDiscardIfEmpty();

            if (renderedBuffer != null) {
                BufferUploader.drawWithShader(renderedBuffer);
            }
        }

        backup.restore();

        backup = null;
        buffer = null;
    }

    private static void prepare() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
    }
}
