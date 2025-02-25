package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.GlStateBackup;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DragonEditorHandler {
    private static ShaderInstance skinGenerationShader;

    private static @Nullable ResourceLocation getDragonPartLocation(final SkinLayer layer, final String partKey, final ResourceKey<DragonSpecies> type) {
        if (Objects.equals(layer.name, "Extra") && layer != SkinLayer.EXTRA) {
            return getDragonPartLocation(SkinLayer.EXTRA, partKey, type);
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type) == null) {
            DragonSurvival.LOGGER.error("Part type map missing for dragon type {}", type);
            return null;
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type).get(layer) == null) {
            DragonSurvival.LOGGER.error("Dragon part layer {} not found", layer.name);
            return null;
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type).get(layer).isEmpty()) {
            DragonSurvival.LOGGER.error("Dragon part layer {} is empty", layer.name);
            return null;
        }

        if (layer == SkinLayer.BASE && partKey.equalsIgnoreCase(DefaultPartLoader.NO_PART)) {
            // Without a base the dragon will be invisible
            // TODO :: return a sort of full-textured missingno texture?
            return DragonPartLoader.DRAGON_PARTS.get(type).get(layer).getFirst().texture();
        }

        List<DragonPart> parts = DragonPartLoader.DRAGON_PARTS.get(type).get(layer);

        for (DragonPart part : parts) {
            if (Objects.equals(part.key(), partKey)) {
                return part.texture();
            }
        }

        // TODO :: return no part?
        return null;
    }

    public static @Nullable DragonPart getDragonPart(final SkinLayer layer, final String partKey, final ResourceKey<DragonSpecies> type) {
        if (Objects.equals(layer.name, "Extra") && layer != SkinLayer.EXTRA) {
            return getDragonPart(SkinLayer.EXTRA, partKey, type);
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type) == null) {
            DragonSurvival.LOGGER.error("Part type map missing for dragon type {}", type);
            return null;
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type).get(layer) == null) {
            DragonSurvival.LOGGER.error("Dragon part layer {} not found", layer.name);
            return null;
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type).get(layer).isEmpty()) {
            DragonSurvival.LOGGER.error("Dragon part layer {} is empty", layer.name);
            return null;
        }

        if (layer == SkinLayer.BASE && partKey.equalsIgnoreCase(DefaultPartLoader.NO_PART)) {
            // Without a base the dragon will be invisible
            // TODO :: return a sort of full-textured missingno texture?
            return DragonPartLoader.DRAGON_PARTS.get(type).get(layer).getFirst();
        }

        List<DragonPart> parts = DragonPartLoader.DRAGON_PARTS.get(type).get(layer);

        for (DragonPart part : parts) {
            if (Objects.equals(part.key(), partKey)) {
                return part;
            }
        }

        // TODO :: return no part?
        return null;
    }

    public static ArrayList<String> getDragonPartKeys(final Holder<DragonSpecies> type, final Holder<DragonBody> body, final SkinLayer layer) {
        if (Objects.equals(layer.name, "Extra") && layer != SkinLayer.EXTRA) {
            return getDragonPartKeys(type, body, SkinLayer.EXTRA);
        }

        ArrayList<String> keys = new ArrayList<>();
        if (DragonPartLoader.DRAGON_PARTS.get(type.getKey()) == null) {
            DragonSurvival.LOGGER.error("Part type map missing for dragon type {}", type);
            keys.add(DefaultPartLoader.NO_PART);
            return keys;
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type.getKey()).get(layer) == null) {
            DragonSurvival.LOGGER.error("Dragon part layer {} not found", layer.name);
            keys.add(DefaultPartLoader.NO_PART);
            return keys;
        }

        if (DragonPartLoader.DRAGON_PARTS.get(type.getKey()).get(layer).isEmpty()) {
            DragonSurvival.LOGGER.error("Dragon part layer {} is empty", layer.name);
            keys.add(DefaultPartLoader.NO_PART);
            return keys;
        }

        List<DragonPart> parts = DragonPartLoader.DRAGON_PARTS.get(type.getKey()).get(layer);

        for (DragonPart part : parts) {
            if (part.applicableBodies().isEmpty() && body.value().model() == DragonBody.DEFAULT_MODEL || part.applicableBodies().contains(body.getKey())) {
                keys.add(part.key());
            }
        }

        return keys;
    }

    public static ArrayList<String> getDragonPartKeys(final Player player, final SkinLayer layer) {
        return getDragonPartKeys(DragonStateProvider.getData(player).species(), DragonUtils.getBody(player), layer);
    }

    public static CompletableFuture<List<Pair<NativeImage, ResourceLocation>>> generateSkinTextures(final DragonEntity dragon) {
        return CompletableFuture.supplyAsync(() -> generateTextures(dragon), Util.backgroundExecutor());
    }

    public static void generateSkinTexturesGPU(final DragonEntity dragon) {
        genTexturesGPU(dragon);
    }

    private static void genTexturesGPU(final DragonEntity dragon) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonBody.TextureSize textureSize = handler.body().value().textureSize();

        GlStateBackup state = new GlStateBackup();
        RenderSystem.backupGlState(state);
        RenderSystem.backupProjectionMatrix();

        int currentFrameBuffer = GlStateManager.getBoundFramebuffer();
        int currentViewportX = GlStateManager.Viewport.x();
        int currentViewportY = GlStateManager.Viewport.y();
        int currentViewportWidth = GlStateManager.Viewport.width();
        int currentViewportHeight = GlStateManager.Viewport.height();

        RenderTarget normalTarget = new TextureTarget(textureSize.width(), textureSize.height(), false, Minecraft.ON_OSX);
        RenderTarget glowTarget = new TextureTarget(textureSize.width(), textureSize.height(), false, Minecraft.ON_OSX);
        normalTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glowTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        normalTarget.clear(true);
        glowTarget.clear(true);

        DragonStageCustomization customization = handler.getCurrentStageCustomization();

        for (SkinLayer layer : SkinLayer.values()) {
            LayerSettings settings = customization.layerSettings.get(layer).get();
            String selectedSkin = settings.partKey;

            if (selectedSkin != null) {
                DragonPart skinTexture = getDragonPart(layer, selectedSkin, handler.speciesKey());

                if (skinTexture != null) {
                    float hueVal = settings.hue - skinTexture.averageHue();
                    float satVal = settings.saturation;
                    float brightVal = settings.brightness;

                    ResourceLocation location = getDragonPartLocation(layer, selectedSkin, handler.speciesKey());
                    AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);

                    if (settings.glowing) {
                        glowTarget.bindWrite(true);
                    } else {
                        normalTarget.bindWrite(true);
                    }

                    RenderSystem.enableBlend();
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.blendEquation(GlConst.GL_FUNC_ADD);
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);
                    skinGenerationShader.setSampler("SkinTexture", texture);
                    skinGenerationShader.getUniform("HueVal").set(hueVal);
                    skinGenerationShader.getUniform("SatVal").set(satVal);
                    skinGenerationShader.getUniform("BrightVal").set(brightVal);
                    skinGenerationShader.getUniform("Colorable").set(skinTexture.isColorable() ? 1.0f : 0.0f);
                    skinGenerationShader.getUniform("Glowing").set(settings.glowing ? 1.0f : 0.0f);
                    skinGenerationShader.apply();

                    BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
                    bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
                    bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
                    bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
                    bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
                    BufferUploader.draw(bufferbuilder.buildOrThrow());

                    if (settings.glowing && layer == SkinLayer.BASE) {
                        normalTarget.bindWrite(true);
                        bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
                        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
                        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
                        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
                        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
                        BufferUploader.draw(bufferbuilder.buildOrThrow());
                        normalTarget.unbindWrite();
                    }

                    skinGenerationShader.clear();
                    if (settings.glowing) {
                        glowTarget.unbindWrite();
                    } else {
                        normalTarget.unbindWrite();
                    }
                }
            }
        }

        RenderingUtils.copyTextureFromRenderTarget(normalTarget, DragonModel.dynamicTexture(player, handler, false));
        RenderingUtils.copyTextureFromRenderTarget(glowTarget, DragonModel.dynamicTexture(player, handler, true));
        glowTarget.destroyBuffers();
        normalTarget.destroyBuffers();
        RenderSystem.restoreGlState(state);
        RenderSystem.restoreProjectionMatrix();
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, currentFrameBuffer);
        GlStateManager._viewport(currentViewportX, currentViewportY, currentViewportWidth, currentViewportHeight);
    }

    private static List<Pair<NativeImage, ResourceLocation>> generateTextures(final DragonEntity dragon) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return List.of();
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        List<Pair<NativeImage, ResourceLocation>> texturesToRegister = new ArrayList<>();
        DragonStageCustomization customization = handler.getCurrentStageCustomization();
        DragonBody.TextureSize textureSize = handler.body().value().textureSize();
        NativeImage normal = new NativeImage(textureSize.width(), textureSize.height(), true);
        NativeImage glow = new NativeImage(textureSize.width(), textureSize.height(), true);

        for (SkinLayer layer : SkinLayer.values()) {
            LayerSettings settings = customization.layerSettings.get(layer).get();
            String selectedSkin = settings.partKey;

            if (selectedSkin != null) {
                DragonPart skinTexture = getDragonPart(layer, selectedSkin, handler.speciesKey());

                if (skinTexture != null) {
                    float hue = settings.hue - skinTexture.averageHue();
                    float saturation = settings.saturation;
                    float brightness = settings.brightness;

                    ResourceLocation textureLocation = getDragonPartLocation(layer, selectedSkin, handler.speciesKey());
                    NativeImage skinImage = RenderingUtils.getImageFromResource(textureLocation);

                    for (int x = 0; x < skinImage.getWidth(); x++) {
                        for (int y = 0; y < skinImage.getHeight(); y++) {
                            Color baseColor = new Color(skinImage.getPixelRGBA(x, y), true);
                            Color hueAdjustedColor = getHueAdjustedColor(settings.glowing, skinTexture.isColorable(), hue, saturation, brightness, baseColor);

                            if (hueAdjustedColor == null) {
                                continue;
                            }

                            if (hueAdjustedColor.getAlpha() != 0) {
                                Supplier<NativeImage> target = settings.glowing ? () -> glow : () -> normal;
                                target.get().setPixelRGBA(x, y, hueAdjustedColor.getRGB());

                                if (settings.glowing && layer == SkinLayer.BASE) {
                                    normal.setPixelRGBA(x, y, hueAdjustedColor.getRGB());
                                }
                            }
                        }
                    }

                    skinImage.close();
                }
            }
        }

        texturesToRegister.add(new Pair<>(normal, DragonModel.dynamicTexture(player, handler, false)));
        texturesToRegister.add(new Pair<>(glow, DragonModel.dynamicTexture(player, handler, true)));

        return texturesToRegister;
    }

    private static @Nullable Color getHueAdjustedColor(boolean glowing, boolean colorable, float hue, float saturation, float brightness, Color baseColor) {
        if (!colorable) {
            return baseColor;
        }

        float[] hsb = new float[3];
        Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), hsb);

        if (glowing && hsb[0] == 0.5f && hsb[1] == 0.5f) {
            return null;
        }

        hsb[0] = hsb[0] - hue;
        hsb[1] = Mth.lerp(Math.abs(saturation - 0.5f) * 2.0f, hsb[1], saturation > 0.5f ? 1.0f : 0.0f);
        hsb[2] = Mth.lerp(Math.abs(brightness - 0.5f) * 2.0f, hsb[2], brightness > 0.5f ? 1.0f : 0.0f);

        Color adjustedColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        return new Color(adjustedColor.getRed(), adjustedColor.getGreen(), adjustedColor.getBlue(), baseColor.getAlpha());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("skin_generation"), DefaultVertexFormat.BLIT_SCREEN), instance -> skinGenerationShader = instance);
    }
}