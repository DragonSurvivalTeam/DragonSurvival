package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

// TODO :: geckolib has an 'AutoGlowingGeoLayer' class, could that help here?
// FIXME :: glow layer doesn't like translucency much (it goes dark once the alpha changes)
public class DragonGlowLayerRenderer extends GeoRenderLayer<DragonEntity> {
    private final GeoEntityRenderer<DragonEntity> renderer;

    public DragonGlowLayerRenderer(final GeoEntityRenderer<DragonEntity> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!(renderer instanceof DragonRenderer dragonRenderer)) {
            return;
        }

        if (!dragonRenderer.shouldRenderLayers) {
            return;
        }

        Player player = animatable.getPlayer();

        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        SkinPreset preset = handler.getCurrentSkinPreset();

        DragonStageCustomization customization = preset.get(handler.stageKey()).get();
        ResourceLocation customGlowTexture;
        if(handler.body().value().customModel().equals(DragonBody.DEFAULT_MODEL)) {
            customGlowTexture = DragonSkins.getGlowTexture(player, handler.stageKey());
        } else {
            customGlowTexture = null;
        }

        if (customGlowTexture == null || customGlowTexture.getPath().contains("/" + handler.speciesId().getPath() + "_")) {
            if (dragonRenderer.glowTexture != null) {
                customGlowTexture = dragonRenderer.glowTexture;
            }
        }

        if (customGlowTexture == null && handler.getCurrentStageCustomization().defaultSkin) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/dragon/" + handler.speciesId().getPath() + "_" + handler.stageId().getPath() + "_glow.png");

            if (Minecraft.getInstance().getResourceManager().getResource(location).isPresent()) {
                customGlowTexture = location;
            }
        }

        dragonRenderer.isRenderLayers = true;

        if (customGlowTexture != null) {
            RenderType type = RenderType.EYES.apply(customGlowTexture, RenderType.LIGHTNING_TRANSPARENCY);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
            dragonRenderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
        } else {
            ResourceLocation dynamicGlowKey = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "dynamic_glow_" + animatable.getPlayer().getStringUUID() + "_" + handler.stageId().getPath());

            if (customization.layerSettings.values().stream().anyMatch(layerSettings -> layerSettings.get().glowing)) {
                RenderType type = RenderType.EYES.apply(dynamicGlowKey, RenderType.LIGHTNING_TRANSPARENCY);
                VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
                dragonRenderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
            }
        }

        dragonRenderer.isRenderLayers = false;
    }
}