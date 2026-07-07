package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.DragonSurvivalClient;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.TextureManagerAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

import java.awt.Color;

//@EventBusSubscriber(Dist.CLIENT)
public class RenderingUtils {
    private static final Identifier GROWTH_CIRCLE_SHADER = DragonSurvival.res("core/growth_circle");
    private static final Identifier SWIRL_NOISE_TEXTURE = DragonSurvival.res("textures/shader/swirl_noise.png");
    private static final Identifier GROWTH_GRADIENT_TEXTURE = DragonSurvival.res("textures/shader/growth_bar_gradient.png");
    private static final int GROWTH_CIRCLE_PARAM_TEXTURE_HEIGHT = 8;
    private static final RenderPipeline GROWTH_CIRCLE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(DragonSurvival.res("pipeline/growth_circle"))
        .withVertexShader(GROWTH_CIRCLE_SHADER)
        .withFragmentShader(GROWTH_CIRCLE_SHADER)
        .withSampler("Sampler0")
        .withSampler("Sampler1")
        .withSampler("Sampler2")
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .build();

    @Translation(key = "min_near_plane", type = Translation.Type.CONFIGURATION, comments = {
            "Lower values prevent x-ray through blocks when using a small entity scale",
            "A value that is too low may cause issues when rendering chunks when certain (unknown) mods are present"
    })
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "min_near_plane")
    public static float MIN_NEAR_PLANE = 0.02f;

    private static int shaderColor = 0xFFFFFFFF;
    private static @Nullable DynamicTexture growthCircleParameterTexture;

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
        RenderSystem.assertOnRenderThread();

        int width = target.width;
        int height = target.height;
        GpuTexture sourceTexture = target.getColorTexture();

        if (sourceTexture == null) {
            throw new IllegalStateException("Tried to copy an incomplete render target");
        }

        var textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture currentTexture = ((TextureManagerAccessor)textureManager).dragonSurvival$getTexturesByPath().get(key);
        DynamicTexture dynamicTexture;

        if (currentTexture instanceof DynamicTexture existingTexture
            && existingTexture.getTexture().getWidth(0) == width
            && existingTexture.getTexture().getHeight(0) == height) {
            dynamicTexture = existingTexture;
        } else {
            textureManager.release(key);
            dynamicTexture = new DynamicTexture(key::toString, width, height, true);
            textureManager.register(key, dynamicTexture);
        }

        RenderSystem.getDevice()
            .createCommandEncoder()
            .copyTextureToTexture(sourceTexture, dynamicTexture.getTexture(), 0, 0, 0, 0, 0, width, height);
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
        Minecraft minecraft = Minecraft.getInstance();
        AbstractTexture swirlNoise = minecraft.getTextureManager().getTexture(SWIRL_NOISE_TEXTURE);
        AbstractTexture growthGradient = minecraft.getTextureManager().getTexture(GROWTH_GRADIENT_TEXTURE);
        int clampedSides = Math.max(3, sides);
        float clampedLineWidth = Math.clamp(lineWidthPercent, 0.0F, 0.43F);
        float clampedPercent = Math.clamp(percent, 0.0F, 1.0F);
        float clampedTargetPercent = Math.clamp(targetPercent, 0.0F, 1.0F);
        DynamicTexture parameterTexture = getGrowthCircleParameterTexture();
        updateGrowthCircleParameterTexture(parameterTexture, clampedSides, clampedLineWidth, clampedPercent, clampedTargetPercent, innerColor, outlineColor, addColor, subtractColor);

        graphics.submitGuiElementRenderState(new GrowthCircleRenderState(
            new TextureSetup(
                swirlNoise.getTextureView(),
                growthGradient.getTextureView(),
                parameterTexture.getTextureView(),
                swirlNoise.getSampler(),
                growthGradient.getSampler(),
                parameterTexture.getSampler()
            ),
            new Matrix3x2f(graphics.pose()),
            x,
            y,
            x + radius * 2.0F,
            y + radius * 2.0F,
            graphics.peekScissorStack()
        ));
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

    public static void registerRenderPipelines(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GROWTH_CIRCLE_PIPELINE);
    }

    private static DynamicTexture getGrowthCircleParameterTexture() {
        if (growthCircleParameterTexture == null) {
            growthCircleParameterTexture = new DynamicTexture(() -> "Growth Circle Params", 1, GROWTH_CIRCLE_PARAM_TEXTURE_HEIGHT, true);
        }

        return growthCircleParameterTexture;
    }

    // Pack all the uniform data into a texture instead so that we can fit Minecraft's guiElementRenderState expectations
    private static void updateGrowthCircleParameterTexture(
        final DynamicTexture parameterTexture,
        final int sides,
        final float lineWidthPercent,
        final float percent,
        final float targetPercent,
        final Color innerColor,
        final Color outlineColor,
        final Color addColor,
        final Color subtractColor
    ) {
        NativeImage pixels = parameterTexture.getPixels();
        pixels.setPixel(0, 0, argb(innerColor));
        pixels.setPixel(0, 1, argb(outlineColor));
        pixels.setPixel(0, 2, argb(addColor));
        pixels.setPixel(0, 3, argb(subtractColor));
        pixels.setPixel(0, 4, ARGB.color(255, ARGB.as8BitChannel(lineWidthPercent), ARGB.as8BitChannel(percent), ARGB.as8BitChannel(targetPercent)));
        pixels.setPixel(0, 5, ARGB.color(255, Math.min(sides, 255), 0, 0));
        pixels.setPixel(0, 6, ARGB.colorFromFloat(1.0F, (DragonSurvivalClient.TIMER % 64.0F) / 64.0F, 0.0F, 0.0F));
        pixels.setPixel(0, 7, shaderColor);
        parameterTexture.upload();
    }

    private static int argb(final Color color) {
        float[] components = color.getRGBComponents(null);
        return ARGB.colorFromFloat(components[3], components[0], components[1], components[2]);
    }

    private record GrowthCircleRenderState(
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x0,
        float y0,
        float x1,
        float y1,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {
        private GrowthCircleRenderState(
            final TextureSetup textureSetup,
            final Matrix3x2f pose,
            final float x0,
            final float y0,
            final float x1,
            final float y1,
            final @Nullable ScreenRectangle scissorArea
        ) {
            this(textureSetup, pose, x0, y0, x1, y1, scissorArea, getBounds(x0, y0, x1, y1, pose, scissorArea));
        }

        @Override
        public void buildVertices(final com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer) {
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0()).setUv(0.0F, 1.0F);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1()).setUv(0.0F, 0.0F);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1()).setUv(1.0F, 0.0F);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0()).setUv(1.0F, 1.0F);
        }

        @Override
        public RenderPipeline pipeline() {
            return GROWTH_CIRCLE_PIPELINE;
        }

        private static @Nullable ScreenRectangle getBounds(
            final float x0,
            final float y0,
            final float x1,
            final float y1,
            final Matrix3x2fc pose,
            final @Nullable ScreenRectangle scissorArea
        ) {
            ScreenRectangle bounds = new ScreenRectangle((int)Math.floor(x0), (int)Math.floor(y0), Math.max(1, (int)Math.ceil(x1 - x0)), Math.max(1, (int)Math.ceil(y1 - y0)))
                .transformMaxBounds(pose);
            return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
        }
    }
}
