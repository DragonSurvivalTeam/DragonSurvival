package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.EModelRenderCycle;
import software.bernie.geckolib3.util.RenderUtils;

import javax.annotation.Nullable;

public class DragonRenderer extends GeoEntityRenderer<DragonEntity>{
	public ResourceLocation glowTexture = null;

	public boolean shouldRenderLayers = true;
	public boolean isRenderLayers = false;

	public Color renderColor = Color.ofRGB(255, 255, 255);

	public DragonRenderer(EntityRendererProvider.Context renderManager, AnimatedGeoModel<DragonEntity> modelProvider){
		super(renderManager, modelProvider);
		addLayer(new DragonGlowLayerRenderer(this));
		addLayer(new ClawsAndTeethRenderLayer(this));
		addLayer(new DragonArmorRenderLayer(this));

		if(ModList.get().isLoaded("curios")){
			addLayer(new DragonCuriosRenderLayer(this));
		}
	}


	private int pOverlay;
	protected DragonEntity currentEntityBeingRendered;
	private float currentPartialTicks;

	@Override
	public void renderEarly(DragonEntity animatable, PoseStack stackIn, float ticks, MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks){
		Minecraft.getInstance().getProfiler().push("player_dragon");
		super.renderEarly(animatable, stackIn, ticks, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, partialTicks);
		pOverlay = packedOverlayIn;
	}

	@Override
	public void renderLate(DragonEntity animatable, PoseStack stackIn, float partialTicks, MultiBufferSource renderTypeBuffer, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha){
		super.renderLate(animatable, stackIn, partialTicks, renderTypeBuffer, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		Minecraft.getInstance().getProfiler().pop();
		currentEntityBeingRendered = animatable;
		currentPartialTicks = partialTicks;
	}

	@ConfigOption( side = ConfigSide.CLIENT, key = "renderHeldItem", comment = "Should items be rendered in third person for dragon players?", category = "rendering" )
	public static boolean renderHeldItem = true;

	@Override
	public void render(DragonEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn){
		if(entity.getPlayer().hasEffect(MobEffects.INVISIBILITY)){
			return;
		}

		Player player = entity.getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		boolean hasWings = handler.hasWings() && handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get().wings;

		IBone leftWing = ClientDragonRender.dragonModel.getAnimationProcessor().getBone("WingLeft");
		IBone rightWing = ClientDragonRender.dragonModel.getAnimationProcessor().getBone("WingRight");

		if(leftWing != null)
			leftWing.setHidden(!hasWings);

		if(rightWing != null)
			rightWing.setHidden(!hasWings);

		super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
	}

	@Override
	public void renderRecursively(GeoBone bone, PoseStack stack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		if (getCurrentModelRenderCycle() != EModelRenderCycle.INITIAL) {
			super.renderRecursively(bone, stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			return;
		}

		if(!isRenderLayers){
			Player player = currentEntityBeingRendered.getPlayer();

			ResourceLocation currentTexture = getTextureLocation(currentEntityBeingRendered);
			MultiBufferSource bufferSource = getCurrentRTB();

			RenderType renderType = getRenderType(currentEntityBeingRendered, currentPartialTicks, stack, bufferSource, buffer, packedLight, currentTexture);
			buffer = bufferSource.getBuffer(renderType);

			ItemStack leftItem, rightItem;
			if(player.getMainArm() == HumanoidArm.RIGHT) {
				leftItem = player.getOffhandItem();
				rightItem = player.getMainHandItem();
			} else {
				leftItem = player.getMainHandItem();
				rightItem = player.getOffhandItem();
			}

			if (getCurrentModelRenderCycle() == EModelRenderCycle.INITIAL){
				if(renderHeldItem){
					if(player != Minecraft.getInstance().player || ClientDragonRender.alternateHeldItem || !Minecraft.getInstance().options.getCameraType().isFirstPerson()){
						if(bone.getName().equals(ClientDragonRender.renderItemsInMouth ? "LeftItem_jaw" : "LeftItem") && !leftItem.isEmpty()){
							stack.pushPose();

							RenderUtils.prepMatrixForBone(stack, bone);
							RenderUtils.translateAndRotateMatrixForBone(stack, bone);

							Minecraft.getInstance().getItemRenderer().renderStatic(leftItem, ClientDragonRender.thirdPersonItemRender ? TransformType.FIRST_PERSON_LEFT_HAND : TransformType.GROUND, packedLight, pOverlay, stack, getCurrentRTB(), 0);
							buffer = bufferSource.getBuffer(RenderType.entityTranslucent(currentTexture));
							stack.popPose();
						}

						if(bone.getName().equals(ClientDragonRender.renderItemsInMouth ? "RightItem_jaw" : "RightItem") && !rightItem.isEmpty()){
							stack.pushPose();
							RenderUtils.prepMatrixForBone(stack, bone);
							RenderUtils.translateAndRotateMatrixForBone(stack, bone);

							Minecraft.getInstance().getItemRenderer().renderStatic(rightItem, ClientDragonRender.thirdPersonItemRender ? TransformType.THIRD_PERSON_RIGHT_HAND : TransformType.GROUND, packedLight, pOverlay, stack, getCurrentRTB(), 0);
							buffer = bufferSource.getBuffer(RenderType.entityTranslucent(currentTexture));
							stack.popPose();
						}
					}
				}

//				if(bone.getName().equals("BreathSource")){
//					stack.pushPose();
//					RenderUtils.prepMatrixForBone(stack, bone);
//					RenderUtils.translateAndRotateMatrixForBone(stack, bone);
//
//					if(handler.getMagic().getCurrentlyCasting() instanceof BreathAbility ability){
//						if(ability.getChargeTime() >= ability.getSkillChargeTime()){
//							if(ability.getEffectEntity() != null){
//								stack.mulPose(Vector3f.YN.rotationDegrees(-90));
//								//stack.mulPose(Vector3f.ZN.rotationDegrees(player.xRot));//For head pitch
//								EntityRenderer<? super Entity> effectRender = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(ability.getEffectEntity());
//								effectRender.render(ability.getEffectEntity(), player.getViewYRot(currentPartialTicks), currentPartialTicks, stack, getCurrentRTB(), 200);
//							}
//						}
//					}
//
//					stack.popPose();
//				}
			}

			stack.pushPose();
			RenderUtils.prepMatrixForBone(stack, bone);
			super.renderCubesOfBone(bone, stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			super.renderChildBones(bone, stack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			stack.popPose();
		}
	}

	@Override
	public Color getRenderColor(DragonEntity animatable, float partialTicks, PoseStack stack,
		@Nullable
		MultiBufferSource renderTypeBuffer,
		@Nullable
		VertexConsumer vertexBuilder, int packedLightIn){
		return renderColor;
	}
}