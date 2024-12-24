package by.dragonsurvivalteam.dragonsurvival.client.util;


import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.GlStateBackup;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.*;
import java.io.IOException;
import javax.annotation.Nullable;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderingUtils {
    static final double PI_TWO = Math.PI * 2.0;
    private static ShaderInstance growthCircleShader;


    public static void drawRect(@NotNull final GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.hLine(x, x + width, y, color);
        guiGraphics.hLine(x, x + width, y + height, color);
        guiGraphics.vLine(x, y, y + height, color);
        guiGraphics.vLine(x + width, y, y + height, color);
    }

    public static void drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int[] color) {
        float[] alpha = new float[4];
        float[] red = new float[4];
        float[] green = new float[4];
        float[] blue = new float[4];

        for (int i = 0; i < 4; i++) {
            alpha[i] = (float) (color[i] >> 24 & 255) / 255.0F;
            red[i] = (float) (color[i] >> 16 & 255) / 255.0F;
            green[i] = (float) (color[i] >> 8 & 255) / 255.0F;
            blue[i] = (float) (color[i] & 255) / 255.0F;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.enableBlend();
//		RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.addVertex(mat, right, top, zLevel).setColor(red[0], green[0], blue[0], alpha[0]);
        bufferbuilder.addVertex(mat, left, top, zLevel).setColor(red[1], green[1], blue[1], alpha[1]);
        bufferbuilder.addVertex(mat, left, bottom, zLevel).setColor(red[2], green[2], blue[2], alpha[2]);
        bufferbuilder.addVertex(mat, right, bottom, zLevel).setColor(red[3], green[3], blue[3], alpha[3]);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
//		RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void renderPureColorSquare(PoseStack mStack, int x, int y, int width, int height) {
        Matrix4f mat = mStack.last().pose();
        int zLevel = 0;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.enableBlend();
//		RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        for (int i = 0; i <= width; i++) {
            float val = (float) i / width * 360f / 360f;
            Color top = new Color(Color.HSBtoRGB(val, 1f, 1f));
            bufferbuilder.addVertex(mat, x + i, y, zLevel).setColor(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f);
            bufferbuilder.addVertex(mat, x + i, y + height, zLevel).setColor(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f);
        }

        // Insecure modifications
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void renderColorSquare(@NotNull final GuiGraphics guiGraphics, int x, int y, int width, int height) {
        Matrix4f mat = guiGraphics.pose().last().pose();
        int zLevel = 0;
        RenderSystem.enableBlend();
//		RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < width; i++) {
            float val = (float) i / width * 360f / 360f;
            Color top = new Color(Color.HSBtoRGB(val, 1f, 0f));
            Color bot = new Color(Color.HSBtoRGB(val, 1f, 1f));

            bufferbuilder.addVertex(mat, x + i, y, zLevel).setColor(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f);
            bufferbuilder.addVertex(mat, x + i, y + height / 2f, zLevel).setColor(bot.getRed() / 255f, bot.getGreen() / 255f, bot.getBlue() / 255f, bot.getAlpha() / 255f);
        }

        for (int i = 0; i < width; i++) {
            float val = (float) i / width * 360f / 360f;
            Color top = new Color(Color.HSBtoRGB(val, 1f, 1f));
            Color bot = new Color(Color.HSBtoRGB(val, 0f, 1f));

            bufferbuilder.addVertex(mat, x + i, y + height / 2f, zLevel).setColor(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f);
            bufferbuilder.addVertex(mat, x + i, y + height, zLevel).setColor(bot.getRed() / 255f, bot.getGreen() / 255f, bot.getBlue() / 255f, bot.getAlpha() / 255f);
        }

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
//		RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void fill(@NotNull final GuiGraphics guiGraphics, double pMinX, double pMinY, double pMaxX, double pMaxY, int pColor) {
        Matrix4f pMatrix = guiGraphics.pose().last().pose();

        if (pMinX < pMaxX) {
            double i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        if (pMinY < pMaxY) {
            double j = pMinY;
            pMinY = pMaxY;
            pMaxY = j;
        }

        float f3 = (float) (pColor >> 24 & 255) / 255.0F;
        float f = (float) (pColor >> 16 & 255) / 255.0F;
        float f1 = (float) (pColor >> 8 & 255) / 255.0F;
        float f2 = (float) (pColor & 255) / 255.0F;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.enableBlend();
//		RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.addVertex(pMatrix, (float) pMinX, (float) pMaxY, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex(pMatrix, (float) pMaxX, (float) pMaxY, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex(pMatrix, (float) pMaxX, (float) pMinY, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex(pMatrix, (float) pMinX, (float) pMinY, 0.0F).setColor(f, f1, f2, f3);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
//		RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void uploadTexture(NativeImage image, ResourceLocation key) {
        try (image) {
            // DEBUG :: Export the texture
            //if (key.toString().contains("dynamic_normal")) {
            //	File file = new File(Minecraft.getInstance().gameDirectory, "texture");
            //	file.mkdirs();
            //	file = new File(file.getPath(), key.toString().replace(":", "_") + ".png");
            //	image.writeToFile(file);
            //}

            // the other 'getTexture' call tries to register the texture immediately
            if (Minecraft.getInstance().getTextureManager().getTexture(key, null) instanceof DynamicTexture texture) {
                texture.setPixels(image);
                texture.upload();
            } else {
                DynamicTexture layer = new DynamicTexture(image);
                Minecraft.getInstance().getTextureManager().register(key, layer);
                image.close();
            }
        } catch (Exception e) {
            DragonSurvival.LOGGER.error(e);
        }
    }

    public static void copyTextureFromRenderTarget(RenderTarget target, ResourceLocation key) {
        NativeImage image = new NativeImage(target.width, target.height, true);
        RenderSystem.bindTexture(target.getColorTextureId());
        image.downloadTexture(0, false);
        uploadTexture(image, key);
    }

    @Nullable public static NativeImage getImageFromResource(ResourceLocation location) {
        NativeImage image = null;
        try {
            image = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(location).get().open());
        } catch (Exception e) {
            DragonSurvival.LOGGER.error(String.format("Texture resource %s not found!", location.getPath()), e);
        }

        return image;
    }

    public static void drawGrowthCircle(final GuiGraphics guiGraphics, float x, float y, float radius, int sides, float lineWidthPercent, float percent, Color borderColor, Color innerColor, Color outlineColor) {
        Matrix4f matrix4f = guiGraphics.pose().last().pose();

        float z = 100;
        GlStateBackup state = new GlStateBackup();
        RenderSystem.backupGlState(state);
        RenderSystem.backupProjectionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float trueX = x + radius;
        float trueY = y + radius;
        bufferbuilder.addVertex(matrix4f, trueX - radius, trueY - radius, z).setUv(0, 1);
        bufferbuilder.addVertex(matrix4f, trueX + radius, trueY - radius, z).setUv(1, 1);
        bufferbuilder.addVertex(matrix4f, trueX + radius, trueY + radius, z).setUv(1, 0);
        bufferbuilder.addVertex(matrix4f, trueX - radius, trueY + radius, z).setUv(0, 0);

        growthCircleShader.setSampler("Sampler0", Minecraft.getInstance().getTextureManager().getTexture(DragonSurvival.res("textures/shader/swirl_noise.png")));
        growthCircleShader.getUniform("Sides").set(sides);
        growthCircleShader.getUniform("LineWidth").set(lineWidthPercent);
        float[] colorComponents = new float[4];
        borderColor.getColorComponents(colorComponents);
        growthCircleShader.getUniform("BorderColor").set(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
        innerColor.getColorComponents(colorComponents);
        growthCircleShader.getUniform("InnerColor").set(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
        outlineColor.getColorComponents(colorComponents);
        growthCircleShader.getUniform("OutlineColor").set(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
        growthCircleShader.getUniform("Percent").set(percent);
        growthCircleShader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
        growthCircleShader.getUniform("ModelViewMat").set(RenderSystem.getModelViewMatrix());
        growthCircleShader.getUniform("Time").set((float) Blaze3D.getTime() % 1000.f);
        growthCircleShader.apply();
        BufferUploader.draw(bufferbuilder.buildOrThrow());
        growthCircleShader.clear();

        RenderSystem.restoreProjectionMatrix();
        RenderSystem.restoreGlState(state);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("growth_circle"), DefaultVertexFormat.POSITION_TEX), instance -> growthCircleShader = instance);
    }
}