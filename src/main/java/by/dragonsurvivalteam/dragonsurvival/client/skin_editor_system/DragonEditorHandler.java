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
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.OptionalInt;

@EventBusSubscriber(value = Dist.CLIENT)
public class DragonEditorHandler {
    private static final Identifier SKIN_GENERATION_SHADER = DragonSurvival.res("core/skin_generation");
    private static final com.mojang.blaze3d.pipeline.RenderPipeline SKIN_GENERATION_PIPELINE = com.mojang.blaze3d.pipeline.RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
        .withLocation(DragonSurvival.res("pipeline/skin_generation"))
        .withVertexShader(SKIN_GENERATION_SHADER)
        .withFragmentShader(SKIN_GENERATION_SHADER)
        .withSampler("SkinTexture")
        .withUniform("SkinGenerationInfo", com.mojang.blaze3d.shaders.UniformType.UNIFORM_BUFFER)
        .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE)))
        .build();
    private static final int SKIN_GENERATION_UBO_SIZE = new Std140SizeCalculator()
        .putFloat()
        .putFloat()
        .putFloat()
        .putFloat()
        .putFloat()
        .get();

    private static MappableRingBuffer skinGenerationUniformBuffer;

    public static void generateSkinTextures(final Player player, final DragonStateHandler handler) {
        if (player == null || handler.body() == null) {
            return;
        }

        RenderSystem.assertOnRenderThread();

        var textureSize = handler.body().value().textureSize();
        TextureTarget normalTarget = new TextureTarget("Dragon Skin Normal", textureSize.width(), textureSize.height(), false);
        TextureTarget glowTarget = new TextureTarget("Dragon Skin Glow", textureSize.width(), textureSize.height(), false);

        try {
            clearTarget(normalTarget);
            clearTarget(glowTarget);

            DragonStageCustomization customization = handler.getCurrentStageCustomization();

            for (SkinLayer layer : SkinLayer.values()) {
                LayerSettings settings = customization.layerSettings.get(layer).get();
                String partKey = settings.partKey;

                if (partKey == null || DefaultPartLoader.NO_PART.equals(partKey)) {
                    continue;
                }

                DragonPart part = DragonPartLoader.getDragonPart(layer, handler.speciesKey(), handler.body(), partKey);

                if (part == null) {
                    continue;
                }

                AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(part.texture());
                TextureTarget activeTarget = settings.isGlowing ? glowTarget : normalTarget;
                float hueVal = settings.hue - part.averageHue();

                renderPartToTarget(activeTarget, texture, hueVal, settings.saturation, settings.brightness, part.isColorable(), settings.isGlowing);

                if (settings.isGlowing && layer == SkinLayer.BASE) {
                    renderPartToTarget(normalTarget, texture, hueVal, settings.saturation, settings.brightness, part.isColorable(), true);
                }
            }

            Identifier normalTexture = DragonModel.dynamicTexture(player, handler, false);
            Identifier glowTexture = DragonModel.dynamicTexture(player, handler, true);
            RenderingUtils.copyTextureFromRenderTarget(normalTarget, normalTexture);
            RenderingUtils.copyTextureFromRenderTarget(glowTarget, glowTexture);
        } finally {
            glowTarget.destroyBuffers();
            normalTarget.destroyBuffers();
        }
    }

    public static void registerRenderPipelines(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(SKIN_GENERATION_PIPELINE);
    }

    private static void renderPartToTarget(
        final TextureTarget target,
        final AbstractTexture texture,
        final float hueVal,
        final float satVal,
        final float brightVal,
        final boolean colorable,
        final boolean glowing
    ) {
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView view = commandEncoder.mapBuffer(getSkinGenerationUniformBuffer().currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                .putFloat(hueVal)
                .putFloat(satVal)
                .putFloat(brightVal)
                .putFloat(colorable ? 1.0F : 0.0F)
                .putFloat(glowing ? 1.0F : 0.0F);
        }

        try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Dragon skin generation", target.getColorTextureView(), OptionalInt.empty())) {
            renderPass.setPipeline(SKIN_GENERATION_PIPELINE);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("SkinGenerationInfo", getSkinGenerationUniformBuffer().currentBuffer());
            renderPass.bindTexture("SkinTexture", texture.getTextureView(), texture.getSampler());
            renderPass.draw(0, 3);
        }

        getSkinGenerationUniformBuffer().rotate();
    }

    private static void clearTarget(final TextureTarget target) {
        if (target.getColorTexture() != null) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(), 0);
        }
    }

    private static MappableRingBuffer getSkinGenerationUniformBuffer() {
        if (skinGenerationUniformBuffer == null) {
            skinGenerationUniformBuffer = new MappableRingBuffer(
                () -> "Dragon Skin Generation UBO",
                GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_UNIFORM,
                SKIN_GENERATION_UBO_SIZE
            );
        }

        return skinGenerationUniformBuffer;
    }
}
