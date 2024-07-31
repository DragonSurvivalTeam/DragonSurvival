package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
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

import java.util.Locale;

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

		if (animatable == ClientDragonRender.dragonArmor) {
			return;
		}

		Player player = animatable.getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		SkinPreset preset = handler.getSkinData().skinPreset;
		SkinAgeGroup ageGroup = preset.skinAges.get(handler.getLevel()).get();

		ResourceLocation glowTexture = DragonSkins.getGlowTexture(player, handler.getType(), handler.getLevel());

		if (glowTexture == null || glowTexture.getPath().contains("/" + handler.getTypeNameLowerCase() + "_")) {
			if (dragonRenderer.glowTexture != null) {
				glowTexture = dragonRenderer.glowTexture;
			}
		}

		if (glowTexture == null && handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get().defaultSkin) {
			ResourceLocation location = new ResourceLocation(DragonSurvivalMod.MODID, "textures/dragon/" + handler.getTypeNameLowerCase() + "_" + handler.getLevel().name.toLowerCase(Locale.ENGLISH) + "_glow.png");

			if (Minecraft.getInstance().getResourceManager().getResource(location).isPresent()) {
				glowTexture = location;
			}
		}

		if (glowTexture != null) {
			RenderType type = RenderType.eyes(glowTexture);
			VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
			dragonRenderer.isRenderLayers = true;
			dragonRenderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			ResourceLocation dynamicGlowKey = new ResourceLocation(DragonSurvivalMod.MODID, "dynamic_glow_" + animatable.getPlayer().getStringUUID() + "_" + handler.getLevel().name);
			dragonRenderer.isRenderLayers = true;

			if (ageGroup.layerSettings.values().stream().anyMatch(layerSettings -> layerSettings.get().glowing)) {
				RenderType type = RenderType.eyes(dynamicGlowKey);
				VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
				dragonRenderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			}
		}

		dragonRenderer.isRenderLayers = false;
	}
}