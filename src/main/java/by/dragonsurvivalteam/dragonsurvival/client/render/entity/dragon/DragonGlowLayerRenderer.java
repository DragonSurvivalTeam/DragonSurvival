package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
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

        SkinPreset preset = handler.getSkinData().skinPreset;
        SkinAgeGroup ageGroup = preset.skinAges.get(handler.getLevel()).get();

        ResourceLocation glowTexture = DragonSkins.getGlowTexture(player, handler.getType(), handler.getLevel());

        if (glowTexture == null || glowTexture.getPath().contains("/" + handler.getTypeNameLowerCase() + "_")) {
            if (dragonRenderer.glowTexture != null) {
                glowTexture = dragonRenderer.glowTexture;
            }
        }

        if (glowTexture == null && handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get().defaultSkin) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(DragonSurvivalMod.MODID, "textures/dragon/" + handler.getTypeNameLowerCase() + "_" + handler.getLevel().getRawName() + "_glow.png");

            if (Minecraft.getInstance().getResourceManager().getResource(location).isPresent()) {
                glowTexture = location;
            }
        }

		dragonRenderer.isRenderLayers = true;

		if (glowTexture != null) {
			RenderType type = RenderType.EYES.apply(glowTexture, RenderType.LIGHTNING_TRANSPARENCY);
			VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
			dragonRenderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
		} else {
			ResourceLocation dynamicGlowKey = ResourceLocation.fromNamespaceAndPath(DragonSurvivalMod.MODID, "dynamic_glow_" + animatable.getPlayer().getStringUUID() + "_" + handler.getLevel().name);

			if (ageGroup.layerSettings.values().stream().anyMatch(layerSettings -> layerSettings.get().glowing)) {
				RenderType type = RenderType.EYES.apply(dynamicGlowKey, RenderType.LIGHTNING_TRANSPARENCY);
				VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
				dragonRenderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
			}
		}

        dragonRenderer.isRenderLayers = false;
    }
}