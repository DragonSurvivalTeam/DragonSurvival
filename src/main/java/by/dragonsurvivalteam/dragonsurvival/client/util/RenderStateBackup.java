package by.dragonsurvivalteam.dragonsurvival.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public final class RenderStateBackup {
    private final boolean blendEnabled;
    private final int blendSrcRgb;
    private final int blendDestRgb;
    private final int blendSrcAlpha;
    private final int blendDestAlpha;
    private final boolean depthEnabled;
    private final boolean depthMask;
    private final int depthFunc;
    private final boolean cullEnabled;
    private final boolean polygonOffsetEnabled;
    private final float polygonOffsetFactor;
    private final float polygonOffsetUnits;
    private final boolean colorMaskRed;
    private final boolean colorMaskGreen;
    private final boolean colorMaskBlue;
    private final boolean colorMaskAlpha;

    private RenderStateBackup() {
        RenderSystem.assertOnRenderThread();

        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        blendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        blendDestRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        blendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        blendDestAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        polygonOffsetEnabled = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);
        polygonOffsetFactor = GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR);
        polygonOffsetUnits = GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer colorMask = stack.malloc(4);
            GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMask);
            colorMaskRed = colorMask.get(0) != 0;
            colorMaskGreen = colorMask.get(1) != 0;
            colorMaskBlue = colorMask.get(2) != 0;
            colorMaskAlpha = colorMask.get(3) != 0;
        }
    }

    public static RenderStateBackup capture() {
        return new RenderStateBackup();
    }

    public void restore() {
        RenderSystem.assertOnRenderThread();

        RenderSystem.blendFuncSeparate(blendSrcRgb, blendDestRgb, blendSrcAlpha, blendDestAlpha);
        if (blendEnabled) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }

        RenderSystem.depthMask(depthMask);
        RenderSystem.depthFunc(depthFunc);
        if (depthEnabled) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }

        if (cullEnabled) {
            RenderSystem.enableCull();
        } else {
            RenderSystem.disableCull();
        }

        RenderSystem.polygonOffset(polygonOffsetFactor, polygonOffsetUnits);
        if (polygonOffsetEnabled) {
            RenderSystem.enablePolygonOffset();
        } else {
            RenderSystem.disablePolygonOffset();
        }

        RenderSystem.colorMask(colorMaskRed, colorMaskGreen, colorMaskBlue, colorMaskAlpha);
    }
}
