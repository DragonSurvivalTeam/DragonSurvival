package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.GlStateBackup;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT)
public class DragonEditorHandler {
    private static ShaderInstance skinGenerationShader;

    public static void generateSkinTextures(final DragonEntity dragon) {
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
            String partKey = settings.partKey;

            if (partKey != null) {
                DragonPart skinTexture = DragonPartLoader.getDragonPart(layer, handler.speciesKey(), handler.body(), partKey);

                if (skinTexture != null) {
                    float hueVal = settings.hue - skinTexture.averageHue();
                    float satVal = settings.saturation;
                    float brightVal = settings.brightness;

                    DragonPart part = DragonPartLoader.getDragonPart(layer, handler.speciesKey(), handler.body(), partKey);

                    if (part == null) {
                        continue;
                    }

                    AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(part.texture());

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

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("skin_generation"), DefaultVertexFormat.BLIT_SCREEN), instance -> skinGenerationShader = instance);
    }
}