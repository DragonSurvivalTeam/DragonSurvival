package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;


@EventBusSubscriber(value = Dist.CLIENT)
public class DragonEditorHandler {
    //private static ShaderInstance skinGenerationShader;

    public static void generateSkinTextures(final DragonEntity dragon) {
//        Player player = dragon.getPlayer();
//
//        if (player == null) {
//            return;
//        }
//
//        DragonStateHandler handler = DragonStateProvider.getData(player);
//        DragonBody.TextureSize textureSize = handler.body().value().textureSize();
//
//        GlStateBackup state = new GlStateBackup();
//        RenderSystem.backupGlState(state);
//        RenderSystem.backupProjectionMatrix();
//
//        int currentFrameBuffer = GlStateManager.getBoundFramebuffer();
//        int currentViewportX = GlStateManager.Viewport.x();
//        int currentViewportY = GlStateManager.Viewport.y();
//        int currentViewportWidth = GlStateManager.Viewport.width();
//        int currentViewportHeight = GlStateManager.Viewport.height();
//
//        RenderTarget normalTarget = new TextureTarget(textureSize.width(), textureSize.height(), false, Minecraft.ON_OSX);
//        RenderTarget glowTarget = new TextureTarget(textureSize.width(), textureSize.height(), false, Minecraft.ON_OSX);
//        normalTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
//        glowTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
//        normalTarget.clear(true);
//        glowTarget.clear(true);
//
//        DragonStageCustomization customization = handler.getCurrentStageCustomization();
//
//        for (SkinLayer layer : SkinLayer.values()) {
//            LayerSettings settings = customization.layerSettings.get(layer).get();
//            String partKey = settings.partKey;
//
//            if (partKey != null) {
//                DragonPart skinTexture = DragonPartLoader.getDragonPart(layer, handler.speciesKey(), handler.body(), partKey);
//
//                if (skinTexture != null) {
//                    float hueVal = settings.hue - skinTexture.averageHue();
//                    float satVal = settings.saturation;
//                    float brightVal = settings.brightness;
//
//                    DragonPart part = DragonPartLoader.getDragonPart(layer, handler.speciesKey(), handler.body(), partKey);
//
//                    if (part == null) {
//                        continue;
//                    }
//
//                    AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(part.texture());
//
//                    if (settings.isGlowing) {
//                        glowTarget.bindWrite(true);
//                    } else {
//                        normalTarget.bindWrite(true);
//                    }
//
//                    RenderSystem.enableBlend();
//                    RenderSystem.colorMask(true, true, true, true);
//                    RenderSystem.blendEquation(GlConst.GL_FUNC_ADD);
//                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
//                    RenderSystem.disableDepthTest();
//                    RenderSystem.depthMask(false);
//                    skinGenerationShader.setSampler("SkinTexture", texture);
//                    skinGenerationShader.getUniform("HueVal").set(hueVal);
//                    skinGenerationShader.getUniform("SatVal").set(satVal);
//                    skinGenerationShader.getUniform("BrightVal").set(brightVal);
//                    skinGenerationShader.getUniform("Colorable").set(skinTexture.isColorable() ? 1.0f : 0.0f);
//                    skinGenerationShader.getUniform("Glowing").set(settings.isGlowing ? 1.0f : 0.0f);
//                    skinGenerationShader.apply();
//
//                    BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
//                    bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
//                    bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
//                    bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
//                    bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
//                    BufferUploader.draw(bufferbuilder.buildOrThrow());
//
//                    if (settings.isGlowing && layer == SkinLayer.BASE) {
//                        normalTarget.bindWrite(true);
//                        bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
//                        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
//                        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
//                        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
//                        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
//                        BufferUploader.draw(bufferbuilder.buildOrThrow());
//                        normalTarget.unbindWrite();
//                    }
//
//                    skinGenerationShader.clear();
//                    if (settings.isGlowing) {
//                        glowTarget.unbindWrite();
//                    } else {
//                        normalTarget.unbindWrite();
//                    }
//                }
//            }
//        }
//
//        RenderingUtils.copyTextureFromRenderTarget(normalTarget, DragonModel.dynamicTexture(player, handler, false));
//        RenderingUtils.copyTextureFromRenderTarget(glowTarget, DragonModel.dynamicTexture(player, handler, true));
//        glowTarget.destroyBuffers();
//        normalTarget.destroyBuffers();
//        RenderSystem.restoreGlState(state);
//        RenderSystem.restoreProjectionMatrix();
//        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, currentFrameBuffer);
//        GlStateManager._viewport(currentViewportX, currentViewportY, currentViewportWidth, currentViewportHeight);
    }

//    @SubscribeEvent
//    public static void registerShaders(RegisterShadersEvent event) throws IOException {
//        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("skin_generation"), DefaultVertexFormat.BLIT_SCREEN), instance -> skinGenerationShader = instance);
//    }
}