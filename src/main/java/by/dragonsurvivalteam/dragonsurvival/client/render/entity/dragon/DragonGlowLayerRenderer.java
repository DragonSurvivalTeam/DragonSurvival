package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
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
        ResourceLocation glowTexture = null;

        // At the moment GitHub only contains textures based on the dragon model
        if (handler.getModel().equals(DragonBody.DEFAULT_MODEL)) {
            glowTexture = DragonSkins.getGlowTexture(player, handler.stageKey());
        }

        // FIXME :: is this safe?
        if (dragonRenderer.glowTexture != null && (glowTexture == null || glowTexture.getPath().contains("/" + handler.speciesId().getPath() + "_"))) {
            glowTexture = dragonRenderer.glowTexture;
        }

        if (glowTexture == null && handler.getCurrentStageCustomization().defaultSkin) {
            ResourceLocation defaultGlowSkin = StageResources.getDefaultSkin(handler.species(), handler.stageKey(), true);

            if (Minecraft.getInstance().getResourceManager().getResource(defaultGlowSkin).isPresent()) {
                glowTexture = defaultGlowSkin;
            }
        }

        dragonRenderer.isRenderLayers = true;

        if (glowTexture == null && customization.layerSettings.values().stream().anyMatch(layerSettings -> layerSettings.get().glowing)) {
            glowTexture = DragonModel.dynamicTexture(player, handler, true);
        }

        if (glowTexture != null && RenderingUtils.hasTexture(glowTexture)) {
            RenderType type = RenderType.EYES.apply(glowTexture, RenderType.LIGHTNING_TRANSPARENCY);
            dragonRenderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, bufferSource.getBuffer(type), true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
        }

        dragonRenderer.isRenderLayers = false;
    }
}