package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.TextureManagerAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

//@EventBusSubscriber(Dist.CLIENT)
public class RenderingUtils {
    private static final double TWO_PI = Math.PI * 2.0;

    @Translation(key = "min_near_plane", type = Translation.Type.CONFIGURATION, comments = {
            "Lower values prevent x-ray through blocks when using a small entity scale",
            "A value that is too low may cause issues when rendering chunks when certain (unknown) mods are present"
    })
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "min_near_plane")
    public static float MIN_NEAR_PLANE = 0.02f;

    private static int shaderColor = 0xFFFFFFFF;

    public static void drawGradientRect(@NotNull final GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int[] color) {
        if (color == null || color.length < 4) {
            return;
        }

        int minX = Math.min(left, right);
        int minY = Math.min(top, bottom);
        int maxX = Math.max(left, right) + 1;
        int maxY = Math.max(top, bottom) + 1;
        int width = maxX - minX;

        if (width <= 0 || maxY <= minY) {
            return;
        }

        for (int x = 0; x < width; x++) {
            float delta = width == 1 ? 0.0F : (float)x / (width - 1);
            int topColor = lerpColor(color[1], color[0], delta);
            int bottomColor = lerpColor(color[2], color[3], delta);
            graphics.fillGradient(minX + x, minY, minX + x + 1, maxY, topColor, bottomColor);
        }
    }

    public static void renderPureColorSquare(@NotNull final GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        for (int i = 0; i < width; i++) {
            float hue = width == 1 ? 0.0F : (float)i / (width - 1);
            graphics.fill(x + i, y, x + i + 1, y + height, Color.HSBtoRGB(hue, 1.0F, 1.0F));
        }
    }

    public static void renderColorSquare(@NotNull final GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        int midpoint = y + Math.max(1, height / 2);
        int maxY = y + height;

        for (int i = 0; i < width; i++) {
            float hue = width == 1 ? 0.0F : (float)i / (width - 1);
            int dark = Color.HSBtoRGB(hue, 1.0F, 0.0F);
            int full = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            int pale = Color.HSBtoRGB(hue, 0.0F, 1.0F);

            if (midpoint > y) {
                graphics.fillGradient(x + i, y, x + i + 1, midpoint, dark, full);
            }

            if (maxY > midpoint) {
                graphics.fillGradient(x + i, midpoint, x + i + 1, maxY, full, pale);
            }
        }
    }

    public static void fill(@NotNull final GuiGraphicsExtractor graphics, double pMinX, double pMinY, double pMaxX, double pMaxY, int pColor) {
        int minX = (int)Math.floor(Math.min(pMinX, pMaxX));
        int minY = (int)Math.floor(Math.min(pMinY, pMaxY));
        int maxX = (int)Math.ceil(Math.max(pMinX, pMaxX));
        int maxY = (int)Math.ceil(Math.max(pMinY, pMaxY));

        if (maxX <= minX) {
            maxX = minX + 1;
        }

        if (maxY <= minY) {
            maxY = minY + 1;
        }

        graphics.fill(minX, minY, maxX, maxY, pColor);
    }

    public static void uploadTexture(NativeImage image, Identifier key) {
        boolean transferredOwnership = false;

        try {
            var textureManager = Minecraft.getInstance().getTextureManager();
            AbstractTexture texture = textureManager.getTexture(key);

            if (texture instanceof DynamicTexture dynamicTexture) {
                dynamicTexture.setPixels(image);
                dynamicTexture.upload();
                transferredOwnership = true;
            } else {
                textureManager.release(key);
                textureManager.register(key, new DynamicTexture(key::toString, image));
                transferredOwnership = true;
            }
        } catch (Exception exception) {
            DragonSurvival.LOGGER.error("Failed to upload texture {}", key, exception);

            if (!transferredOwnership) {
                image.close();
            }
        }
    }

    public static void copyTextureFromRenderTarget(RenderTarget target, Identifier key) {
        Screenshot.takeScreenshot(target, image -> uploadTexture(image, key));
    }

    public static @Nullable NativeImage getImageFromResource(Identifier location) {
        try (var input = Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow().open()) {
            return NativeImage.read(input);
        } catch (Exception exception) {
            DragonSurvival.LOGGER.warn("Texture resource {} not found!", location.getPath(), exception);
        }

        return null;
    }

    public static boolean hasTexture(final Identifier resource) {
        if (resource == null) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.getResourceManager().getResource(resource).isPresent()) {
            return true;
        }

        AbstractTexture texture = ((TextureManagerAccessor)minecraft.getTextureManager()).dragonSurvival$getTexturesByPath().get(resource);
        return texture instanceof DynamicTexture;
    }

    public static void setShaderColor(int color) {
        shaderColor = color;
    }

    public static void drawGrowthCircle(final GuiGraphicsExtractor graphics, float x, float y, float radius, int sides, float lineWidthPercent, float percent, float targetPercent, Color innerColor, Color outlineColor, Color addColor, Color subtractColor) {
//        Matrix4f matrix4f = GuiGraphicsExtractor.pose().last().pose();
//
//        float z = 100;
//        GlStateBackup state = new GlStateBackup();
//        RenderSystem.backupGlState(state);
//        RenderSystem.backupProjectionMatrix();
//
//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//        RenderSystem.disableDepthTest();
//        RenderSystem.disableCull();
//        RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);
//        Tesselator tesselator = Tesselator.getInstance();
//        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//        float trueX = x + radius;
//        float trueY = y + radius;
//        bufferbuilder.addVertex(matrix4f, trueX - radius, trueY - radius, z).setUv(0, 1);
//        bufferbuilder.addVertex(matrix4f, trueX + radius, trueY - radius, z).setUv(1, 1);
//        bufferbuilder.addVertex(matrix4f, trueX + radius, trueY + radius, z).setUv(1, 0);
//        bufferbuilder.addVertex(matrix4f, trueX - radius, trueY + radius, z).setUv(0, 0);
//
//        growthCircleShader.setSampler("Sampler0", Minecraft.getInstance().getTextureManager().getTexture(DragonSurvival.res("textures/shader/swirl_noise.png")));
//        growthCircleShader.setSampler("Sampler1", Minecraft.getInstance().getTextureManager().getTexture(DragonSurvival.res("textures/shader/growth_bar_gradient.png")));
//        growthCircleShader.getUniform("Sides").set(sides);
//        growthCircleShader.getUniform("LineWidth").set(lineWidthPercent);
//        float[] colorComponents = new float[4];
//        innerColor.getColorComponents(colorComponents);
//        growthCircleShader.getUniform("InnerColor").set(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
//        outlineColor.getColorComponents(colorComponents);
//        growthCircleShader.getUniform("OutlineColor").set(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
//        addColor.getColorComponents(colorComponents);
//        growthCircleShader.getUniform("AddColor").set(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
//        subtractColor.getColorComponents(colorComponents);
//        growthCircleShader.getUniform("SubtractColor").set(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f);
//        growthCircleShader.getUniform("Percent").set(percent);
//        growthCircleShader.getUniform("TargetPercent").set(targetPercent);
//        growthCircleShader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
//        growthCircleShader.getUniform("ModelViewMat").set(RenderSystem.getModelViewMatrix());
//        growthCircleShader.getUniform("Time").set((float) Blaze3D.getTime() % 1000.f);
//        growthCircleShader.apply();
//        BufferUploader.draw(bufferbuilder.buildOrThrow());
//        growthCircleShader.clear();
//
//        RenderSystem.restoreProjectionMatrix();
//        RenderSystem.restoreGlState(state);
    }

    public static float getNearPlane(float original) {
        // There are some cases where mods call this function when the player is still null.
        // We can't provide anything valid in this situation anyways, so just give the original and don't crash.
        if (Minecraft.getInstance().player == null) {
            return original;
        }

        float scale = Minecraft.getInstance().player.getScale();

        if (scale < 1) {
            // Some mods have issues if the near plane is too close (0.016 seems to work)
            return Math.max(MIN_NEAR_PLANE, original * scale);
        }

        return original;
    }

    public static boolean isFirstPerson(final Player player) {
        return player == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    private static int lerpColor(int start, int end, float delta) {
        int alpha = Math.clamp(Math.round(ARGB.alpha(start) + (ARGB.alpha(end) - ARGB.alpha(start)) * delta), 0, 255);
        int red = Math.clamp(Math.round(ARGB.red(start) + (ARGB.red(end) - ARGB.red(start)) * delta), 0, 255);
        int green = Math.clamp(Math.round(ARGB.green(start) + (ARGB.green(end) - ARGB.green(start)) * delta), 0, 255);
        int blue = Math.clamp(Math.round(ARGB.blue(start) + (ARGB.blue(end) - ARGB.blue(start)) * delta), 0, 255);
        return ARGB.color(alpha, red, green, blue);
    }
}
