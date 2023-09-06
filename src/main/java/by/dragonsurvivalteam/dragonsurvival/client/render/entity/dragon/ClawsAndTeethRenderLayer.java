package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
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
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class ClawsAndTeethRenderLayer extends GeoLayerRenderer<DragonEntity>{

	private final IGeoRenderer<DragonEntity> renderer;

	public ClawsAndTeethRenderLayer(IGeoRenderer<DragonEntity> entityRendererIn){
		super(entityRendererIn);
		renderer = entityRendererIn;
	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, DragonEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch){
		if(!((DragonRenderer)renderer).shouldRenderLayers){
			return;
		}

		if(entitylivingbaseIn.hasEffect(MobEffects.INVISIBILITY)){
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(entitylivingbaseIn.getPlayer());

		if(!handler.getClawToolData().shouldRenderClaws){
			return;
		}


		String clawTexture = constructClaws(entitylivingbaseIn.getPlayer());

		if(clawTexture != null){
			ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, clawTexture);
			((DragonRenderer)renderer).isRenderLayers = true;
			renderToolLayer(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, partialTicks, texture, renderer, getEntityModel());
			((DragonRenderer)renderer).isRenderLayers = false;
		}

		String teethTexture = constructTeethTexture(entitylivingbaseIn.getPlayer());

		if(teethTexture != null){
			ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, teethTexture);
			((DragonRenderer)renderer).isRenderLayers = true;
			renderToolLayer(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, partialTicks, texture, renderer, getEntityModel());
			((DragonRenderer)renderer).isRenderLayers = false;
		}
	}


	public String constructClaws(Player playerEntity){

		String texture = "textures/armor/";
		DragonStateHandler handler = DragonUtils.getHandler(playerEntity);
		ItemStack clawItem = handler.getClawToolData().getClawsInventory().getItem(handler.getType().slotForBonus);
		if(!clawItem.isEmpty()){
			texture = ClientEvents.getMaterial(texture, clawItem);
		}else{
			return null;
		}

		return texture + "dragon_claws.png";
	}


	public String constructTeethTexture(Player playerEntity){

		String texture = "textures/armor/";
		ItemStack swordItem = DragonUtils.getHandler(playerEntity).getClawToolData().getClawsInventory().getItem(0);

		if(!swordItem.isEmpty()){
			texture = ClientEvents.getMaterial(texture, swordItem);
		}else{
			return null;
		}

		return texture + "dragon_teeth.png";
	}


	private void renderToolLayer(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, DragonEntity entitylivingbaseIn, float partialTicks, ResourceLocation texture, IGeoRenderer<DragonEntity> renderer, GeoModelProvider<DragonEntity> entityModel){
		RenderType type = renderer.getRenderType(entitylivingbaseIn, partialTicks, matrixStackIn, bufferIn, null, packedLightIn, texture);
		VertexConsumer vertexConsumer = bufferIn.getBuffer(type);

		renderer.render(entityModel.getModel(entityModel.getModelLocation(entitylivingbaseIn)), entitylivingbaseIn, partialTicks, type, matrixStackIn, bufferIn, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}
}