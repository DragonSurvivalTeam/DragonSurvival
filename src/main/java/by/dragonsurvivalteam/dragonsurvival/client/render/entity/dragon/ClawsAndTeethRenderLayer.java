package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ClawsAndTeethRenderLayer extends GeoRenderLayer<DragonEntity> {
	private final GeoEntityRenderer<DragonEntity> renderer;

	public ClawsAndTeethRenderLayer(final GeoEntityRenderer<DragonEntity> renderer) {
		super(renderer);
		this.renderer = renderer;
	}

	@Override
	public void render(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
		if(!((DragonRenderer) renderer).shouldRenderLayers) {
			return;
		}

		if (animatable.hasEffect(MobEffects.INVISIBILITY)) {
			return;
		}

		Player player = animatable.getPlayer();

		if (player == null) {
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (!handler.getClawToolData().shouldRenderClaws) {
			return;
		}

		String clawTexture = constructClaws(player);

		if (clawTexture != null) {
			ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, clawTexture);

			((DragonRenderer) renderer).isRenderLayers = true;
			renderToolLayer(poseStack, animatable, bakedModel, bufferSource, texture, partialTick, packedLight);
			((DragonRenderer) renderer).isRenderLayers = false;
		}

		String teethTexture = constructTeethTexture(player);

		if (teethTexture != null) {
			ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, teethTexture);

			((DragonRenderer) renderer).isRenderLayers = true;
			renderToolLayer(poseStack, animatable, bakedModel, bufferSource, texture, partialTick, packedLight);
			((DragonRenderer) renderer).isRenderLayers = false;
		}
	}

	private void renderToolLayer(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final MultiBufferSource bufferSource, final ResourceLocation texture, float partialTick, int packedLight) {
		RenderType type = renderer.getRenderType(animatable, texture, bufferSource, partialTick);
		VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
		renderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

	public String constructClaws(final Player player) {
		String texturePath = "textures/armor/";
		DragonStateHandler handler = DragonUtils.getHandler(player);
		ItemStack clawItem = handler.getClawToolData().getClawsInventory().getItem(handler.getType().slotForBonus);

		if (!clawItem.isEmpty()) {
			texturePath = ClientEvents.getMaterial(texturePath, clawItem);
		} else {
			return null;
		}

		return texturePath + "dragon_claws.png";
	}

	public String constructTeethTexture(final Player player) {
		String texturePath = "textures/armor/";
		ItemStack swordItem = DragonUtils.getHandler(player).getClawToolData().getClawsInventory().getItem(0);

		if (!swordItem.isEmpty()) {
			texturePath = ClientEvents.getMaterial(texturePath, swordItem);
		} else {
			return null;
		}

		return texturePath + "dragon_teeth.png";
	}
}