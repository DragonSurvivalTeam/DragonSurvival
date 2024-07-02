package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.KeyInputHandler;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonArmorModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonArmorRenderLayer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorEntityRendererManager;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorLivingRenderer;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.player.PacketSyncCapabilityMovement;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.core.object.Color;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber( Dist.CLIENT )
public class ClientDragonRender{
	public static DragonModel dragonModel = new DragonModel();
	public static DragonArmorModel dragonArmorModel = new DragonArmorModel(dragonModel);
	/**
	 * First-person armor instance
	 */
	public static DragonEntity dragonArmor;
	public static DragonEntity dummyDragon;

	/**
	 * Instances used for rendering third-person dragon models
	 */
	public static ConcurrentHashMap<Integer, AtomicReference<DragonEntity>> playerDragonHashMap = new ConcurrentHashMap<>(20);

	@ConfigOption( side = ConfigSide.CLIENT, category = "firstperson", key = "renderFirstPerson", comment = "Render dragon model in first person. If your own tail scares you, write false" )
	public static Boolean renderInFirstPerson = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "firstperson", key = "renderFirstPersonFlight", comment = "Render dragon model in first person while gliding. We don't advise you to turn it on." )
	public static Boolean renderFirstPersonFlight = false;

	@ConfigOption( side = ConfigSide.CLIENT, category = "firstperson", key = "firstPersonRotation", comment = "Use rotation of your tail in first person, otherwise the tail is always opposite of your camera. If the tail is constantly climbing in your face, put false." )
	public static Boolean firstPersonRotation = false;

	@ConfigOption( side = ConfigSide.CLIENT, category = "flight", key = "renderOtherPlayerRotation", comment = "Should the rotation effect during gliding of other players be shown?" )
	public static Boolean renderOtherPlayerRotation = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "inventory", key = "alternateHeldItem", comment = "Should held items be rendered as if you are in third-person even in first person as a dragon?" )
	public static Boolean alternateHeldItem = false;

	@ConfigOption( side = ConfigSide.CLIENT, category = "inventory", key = "thirdPersonItemRender", comment = "Should the third person item render for dragons use the default rotations? Use this if modded items are rendering weird when held." )
	public static Boolean thirdPersonItemRender = false;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "renderItemsInMouth", comment = "Should items be rendered near the mouth of dragons rather then hovering by their side?" )
	public static Boolean renderItemsInMouth = false;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "renderDragonClaws", comment = "Should the tools on the claws and teeth be rendered for your dragon?" )
	public static Boolean renderDragonClaws = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "renderNewbornSkin", comment = "Do you want your dragon skin to be rendered as a newborn dragon?" )
	public static Boolean renderNewbornSkin = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "renderYoungSkin", comment = "Do you want your dragon skin to be rendered as a young dragon?" )
	public static Boolean renderYoungSkin = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "renderAdultSkin", comment = "Do you want your dragon skin to be rendered as a adult dragon?" )
	public static Boolean renderAdultSkin = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "renderOtherPlayerSkins", comment = "Should other player skins be rendered?" )
	public static Boolean renderOtherPlayerSkins = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "armorRenderLayer", comment = "Should the armor be rendered as a layer on the dragon? Some shaders requires this to be off. Can cause some weird effects with armor when turned off." )
	public static Boolean armorRenderLayer = true;

	@ConfigOption( side = ConfigSide.CLIENT, category = "nametag", key = "dragonNameTags", comment = "Show name tags for dragons." )
	public static Boolean dragonNameTags = false;

	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "rotateBodyWithCamera", comment = "Should the body rotate with the camera when turning around." )
	public static Boolean rotateBodyWithCamera = true;
	
	@ConfigOption( side = ConfigSide.CLIENT, category = "rendering", key = "rotateCameraWithDragon", comment = "Should the player rotate their view when the dragon they are riding rotates their body?")
	public static Boolean rotateCameraWithDragon = true;

	private static boolean wasFreeLook = false;

	@SubscribeEvent
	public static void renderFirstPerson(RenderHandEvent renderHandEvent){
		if(renderInFirstPerson){
			LocalPlayer player = Minecraft.getInstance().player;
			DragonStateProvider.getCap(player).ifPresent(playerStateHandler -> {
				if(playerStateHandler.isDragon()){
					if(alternateHeldItem){
						renderHandEvent.setCanceled(true);
					}
				}
			});
		}
	}

	/** Show breath hit range when hitboxes are being rendered */
	@SubscribeEvent
	public static void renderBreathHitBox(final RenderLevelStageEvent event) {
		if (ClientConfig.renderBreathRange && event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS && Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
			LocalPlayer localPlayer = Minecraft.getInstance().player;
			DragonStateHandler handler = DragonUtils.getHandler(localPlayer);

			if (localPlayer == null || !handler.isDragon()) {
				return;
			}

			VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
			Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

			PoseStack poseStack = event.getPoseStack();
			poseStack.pushPose();
			poseStack.translate(-camera.x(), -camera.y(), -camera.z());

			int range = BreathAbility.calculateCurrentBreathRange(handler.getSize());
			AbstractDragonType dragonType = handler.getType();

			int red = DragonUtils.isDragonType(dragonType, DragonTypes.CAVE) ? 1 : 0;
			int green = DragonUtils.isDragonType(dragonType, DragonTypes.FOREST) ? 1 : 0;
			int blue = DragonUtils.isDragonType(dragonType, DragonTypes.SEA) ? 1 : 0;

			LevelRenderer.renderLineBox(poseStack, buffer, DragonAbilities.calculateBreathArea(localPlayer, handler, range), red, green, blue, 1);

			/* Draw the area which will affect blocks
			Pair<BlockPos, Direction> data = DragonAbilities.breathStartPosition(localPlayer, red == 1 ? new NetherBreathAbility() : green == 1 ? new ForestBreathAbility() : new StormBreathAbility(), range);
			BlockPos startPosition = data.getFirst();

			if (startPosition != null) {
				AABB blockRange = new AABB(
						startPosition.getX() - (double) range / 2,
						startPosition.getY() - (double) range / 2,
						startPosition.getZ() - (double) range / 2,
						startPosition.getX() + (double) range / 2,
						startPosition.getY() + (double) range / 2,
						startPosition.getZ() + (double) range / 2
				);

				LevelRenderer.renderLineBox(poseStack, buffer, blockRange, 1, 1, 1, 1);
			}
			*/

			poseStack.popPose();
		}
	}

	/** Amount of client ticks the player model will not be rendered if the player was recently a dragon (to avoid player model pop-up after respawning) */
	private static final int MAX_DELAY = 10;
	private static int renderDelay;

	/** Called for every player */
	@SubscribeEvent
	public static void thirdPersonPreRender(final RenderPlayerEvent.Pre renderPlayerEvent) {
		if (!(renderPlayerEvent.getEntity() instanceof AbstractClientPlayer player)) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (!playerDragonHashMap.containsKey(player.getId())) {
			DragonEntity dummyDragon = DSEntities.DRAGON.get().create(player.level());
			dummyDragon.playerId = player.getId();
			playerDragonHashMap.put(player.getId(), new AtomicReference<>(dummyDragon));
		}

		if (dragonArmor == null) {
			dragonArmor = DSEntities.DRAGON_ARMOR.get().create(player.level());
			assert dragonArmor != null;
			dragonArmor.playerId = player.getId();
		}

		if (dummyDragon == null) {
			dummyDragon = DSEntities.DRAGON.get().create(player.level());
			assert dummyDragon != null;
			dummyDragon.playerId = player.getId();
		}

		if(handler.isDragon()){
			if (player == ClientProxy.getLocalPlayer()) {
				renderDelay = MAX_DELAY;
			}

			renderPlayerEvent.setCanceled(true);
			float partialRenderTick = renderPlayerEvent.getPartialTick();
			float yaw = player.getViewYRot(partialRenderTick);

			DragonLevel dragonStage = handler.getLevel();
			ResourceLocation texture = DragonSkins.getPlayerSkin(player, handler.getType(), dragonStage);
			PoseStack poseStack = renderPlayerEvent.getPoseStack();

			try {
				poseStack.pushPose();

				Vector3f lookVector = Functions.getDragonCameraOffset(player);
				poseStack.translate(-lookVector.x(), lookVector.y(), -lookVector.z());

				double size = handler.getSize();
				String playerModelType = player.getModelName();
				EntityRenderer<? extends Player> playerRenderer = /* TODO :: Why not use renderPlayerEvent.getRenderer() */ ((AccessorEntityRendererManager) minecraft.getEntityRenderDispatcher()).getPlayerRenderers().get(playerModelType);
				int eventLight = renderPlayerEvent.getPackedLight();
				final MultiBufferSource renderTypeBuffer = renderPlayerEvent.getMultiBufferSource();

				if (dragonNameTags) {
					RenderNameTagEvent renderNameplateEvent = new RenderNameTagEvent(player, player.getDisplayName(), playerRenderer, poseStack, renderTypeBuffer, eventLight, partialRenderTick);
					MinecraftForge.EVENT_BUS.post(renderNameplateEvent);

					if (renderNameplateEvent.getResult() != Event.Result.DENY && (renderNameplateEvent.getResult() == Event.Result.ALLOW || ((AccessorLivingRenderer) playerRenderer).callShouldShowName(player))) {
						((AccessorEntityRenderer) playerRenderer).callRenderNameTag(player, renderNameplateEvent.getContent(), poseStack, renderTypeBuffer, eventLight);
					}
				}

				poseStack.mulPose(Axis.YN.rotationDegrees((float) handler.getMovementData().bodyYaw));

				// FIXME :: This is some arbitrary scaling that was created back when the maximum size was hard capped at 40. Touching it will cause the render to desync from the hitbox.
				float scale = (float) Math.max(size / 40.0D, 0.4D);
				poseStack.scale(scale, scale, scale);

				((AccessorEntityRenderer) renderPlayerEvent.getRenderer()).setShadowRadius((float) ((3.0F * size + 62.0F) / 260.0F));
				DragonEntity dummyDragon = playerDragonHashMap.get(player.getId()).get();
				EntityRenderer<? super DragonEntity> dragonRenderer = minecraft.getEntityRenderDispatcher().getRenderer(dummyDragon);
				dragonModel.setCurrentTexture(texture);

				if(player.isCrouching() && handler.isWingsSpread() && !player.onGround()){
					poseStack.translate(0, -0.15, 0);
				} else if (player.isCrouching()) {
					if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
						poseStack.translate(0, 0.045, 0);
					}
					else {
						poseStack.translate(0, 0.325 - size / DragonLevel.ADULT.size * 0.140, 0);
					}
				} else if (player.isSwimming() || player.isAutoSpinAttack() || handler.isWingsSpread() && !player.onGround() && !player.isInWater() && !player.isInLava()) {
					if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
						poseStack.translate(0, -0.55, 0);
					}
					else {
						poseStack.translate(0, -0.15 - size / DragonLevel.ADULT.size * 0.2, 0);
					}
				}
				if(!player.isInvisible()){
					if(ServerFlightHandler.isGliding(player) || (player.isPassenger() && DragonUtils.isDragon(player.getVehicle()) && ServerFlightHandler.isGliding((Player) player.getVehicle()))){
						if(renderOtherPlayerRotation || minecraft.player == player){
							float upRot = 0;
							if (ServerFlightHandler.isGliding(player)) {
								upRot = Mth.clamp((float)(player.getDeltaMovement().y * 20), -80, 80);
							} else {
								upRot = Mth.clamp((float)(player.getVehicle().getDeltaMovement().y * 20), -80, 80);
							}

     						dummyDragon.prevXRot = Mth.lerp(0.1F, dummyDragon.prevXRot, upRot);
							dummyDragon.prevXRot = Mth.clamp(dummyDragon.prevXRot, -80, 80);

							handler.getMovementData().prevXRot = dummyDragon.prevXRot;

							if(Float.isNaN(dummyDragon.prevXRot)){
								dummyDragon.prevXRot = upRot;
							}

							if (Float.isNaN(dummyDragon.prevXRot)) {
								dummyDragon.prevXRot = 0;
							}

							poseStack.mulPose(Axis.XN.rotationDegrees(dummyDragon.prevXRot));
							
							Vec3 vector3d1 = new Vec3(0, 0, 0);
							Vec3 vector3d = new Vec3(0, 0, 0);
							if (ServerFlightHandler.isGliding(player)) {
								vector3d1 = player.getDeltaMovement();
								vector3d = player.getViewVector(1f);
							} else {
								vector3d1 = player.getVehicle().getDeltaMovement();
								vector3d = player.getVehicle().getViewVector(1f);
							}
							double d0 = vector3d1.horizontalDistanceSqr();
							double d1 = vector3d.horizontalDistanceSqr();
							double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
							double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;

							float rot = Mth.clamp((float)(Math.signum(d3) * Math.acos(d2)) * 2, -1, 1);
							
							dummyDragon.prevZRot = Mth.lerp(0.1F, dummyDragon.prevZRot, rot);

							handler.getMovementData().prevZRot = dummyDragon.prevZRot;
							dummyDragon.prevZRot = Mth.clamp(dummyDragon.prevZRot, -1, 1);

							if (Float.isNaN(dummyDragon.prevZRot)) {
								dummyDragon.prevZRot = rot;
							}

							if (Float.isNaN(dummyDragon.prevZRot)) {
								dummyDragon.prevZRot = 0;
							}

							handler.getMovementData().prevXRot = dummyDragon.prevXRot;
							handler.getMovementData().prevZRot = rot;

							poseStack.mulPose(Axis.ZP.rotation(dummyDragon.prevZRot));
						}
					} else {
						handler.getMovementData().prevZRot = 0;
						handler.getMovementData().prevXRot = 0;
					}
					if (player != minecraft.player || !Minecraft.getInstance().options.getCameraType().isFirstPerson() || !ServerFlightHandler.isGliding(player) || renderFirstPersonFlight) {
						dragonRenderer.render(dummyDragon, yaw, partialRenderTick, poseStack, renderTypeBuffer, eventLight);

						if (!armorRenderLayer) {
							ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
							ItemStack chestPlate = player.getItemBySlot(EquipmentSlot.CHEST);
							ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
							ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
							
							ResourceLocation helmetTexture;
							ResourceLocation chestPlateTexture;
							ResourceLocation legsTexture;
							ResourceLocation bootsTexture;

							helmetTexture = new ResourceLocation(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(player, EquipmentSlot.HEAD));
							chestPlateTexture = new ResourceLocation(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(player, EquipmentSlot.CHEST));
							legsTexture = new ResourceLocation(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(player, EquipmentSlot.LEGS));
							bootsTexture = new ResourceLocation(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(player, EquipmentSlot.FEET));

							renderArmorPiece(helmet, poseStack, renderTypeBuffer, yaw, eventLight, dummyDragon, partialRenderTick, helmetTexture);
							renderArmorPiece(chestPlate, poseStack, renderTypeBuffer, yaw, eventLight, dummyDragon, partialRenderTick, chestPlateTexture);
							renderArmorPiece(legs, poseStack, renderTypeBuffer, yaw, eventLight, dummyDragon, partialRenderTick, legsTexture);
							renderArmorPiece(boots, poseStack, renderTypeBuffer, yaw, eventLight, dummyDragon, partialRenderTick, bootsTexture);
						}
					}
				}

				if (!player.isSpectator()) {
					// Render the parrot on the players shoulder
					((AccessorLivingRenderer) playerRenderer).getRenderLayers().stream().filter(ParrotOnShoulderLayer.class::isInstance).findAny().ifPresent(renderLayer -> {
						poseStack.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);
						poseStack.mulPose(Axis.XN.rotationDegrees(180.0F));
						double height = 1.3 * scale;
						double forward = 0.3 * scale;
						float parrotHeadYaw = Mth.clamp(-1.0F * ((float) handler.getMovementData().bodyYaw - (float) handler.getMovementData().headYaw), -75.0F, 75.0F);
						poseStack.translate(0, -height, -forward);
						renderLayer.render(poseStack, renderTypeBuffer, eventLight, player, 0.0F, 0.0F, partialRenderTick, (float) player.tickCount + partialRenderTick, parrotHeadYaw, (float) handler.getMovementData().headPitch);
						poseStack.translate(0, height, forward);
						poseStack.mulPose(Axis.XN.rotationDegrees(-180.0F));
						poseStack.scale(scale, scale, scale);
					});

					int combinedOverlayIn = LivingEntityRenderer.getOverlayCoords(player, 0);
					if(player.hasEffect(DragonEffects.TRAPPED)){
						float bolasScale = player.getEyeHeight();
						if(handler.isDragon()) {
							bolasScale = (float) DragonSizeHandler.calculateDragonEyeHeight(handler.getSize(), ServerConfig.hitboxGrowsPastHuman);
						}
						ClientEvents.renderBolas(eventLight, combinedOverlayIn, renderTypeBuffer, poseStack, bolasScale);
					}
				}
			} catch (Throwable throwable) {
				DragonSurvivalMod.LOGGER.error("A problem occurred while rendering a dragon in third person", throwable);
			} finally {
				poseStack.popPose();
			}
		}else{
			if (renderDelay > 0 && player == ClientProxy.getLocalPlayer()) {
				renderDelay--;
				renderPlayerEvent.setCanceled(true);
			} else {
				((AccessorEntityRenderer) renderPlayerEvent.getRenderer()).setShadowRadius(0.5F);
			}
		}
		dragonModel.setCurrentTexture(null);
	}

	private static void renderArmorPiece(ItemStack stack, PoseStack matrixStackIn, MultiBufferSource bufferIn, float yaw, int packedLightIn, DragonEntity entitylivingbaseIn, float partialTicks, ResourceLocation helmetTexture){
		Color armorColor = Color.ofRGB(1f, 1f, 1f);

		if(stack == null || stack.isEmpty()) return;

		if (stack.getItem() instanceof DyeableArmorItem dyeableArmorItem) {
			int colorCode = dyeableArmorItem.getColor(stack);
			armorColor = Color.ofOpaque(colorCode);
		}

		if(!stack.isEmpty()){
			EntityRenderer<? super DragonEntity> dragonArmorRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(ClientDragonRender.dragonArmor);
			ClientDragonRender.dragonArmor.copyPosition(entitylivingbaseIn);
			ClientDragonRender.dragonArmorModel.setArmorTexture(helmetTexture);
			Color preColor = ((DragonRenderer)dragonArmorRenderer).renderColor;
			((DragonRenderer)dragonArmorRenderer).shouldRenderLayers = false;
			((DragonRenderer)dragonArmorRenderer).renderColor = armorColor;
			dragonArmorRenderer.render(entitylivingbaseIn, yaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
			((DragonRenderer)dragonArmorRenderer).renderColor = preColor;
			((DragonRenderer)dragonArmorRenderer).shouldRenderLayers = true;
		}
	}

	@SubscribeEvent
	public static void spin(InputEvent.InteractionKeyMappingTriggered keyInputEvent){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null){
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);
		if(!handler.isDragon()){
			return;
		}

		if(keyInputEvent.isAttack() && keyInputEvent.shouldSwingHand() && !handler.getMovementData().dig){
			handler.getMovementData().bite = true;
		}
	}

	@SubscribeEvent
	public static void onClientTick(RenderTickEvent renderTickEvent){
		if(renderTickEvent.phase == Phase.START){
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if(player != null){
				DragonStateProvider.getCap(player).ifPresent(playerStateHandler -> {
					if(playerStateHandler.isDragon()){
						DragonMovementData md = playerStateHandler.getMovementData();
						md.headYawLastTick = Mth.lerp(0.05, md.headYawLastTick, md.headYaw);
						md.headPitchLastTick = Mth.lerp(0.05, md.headPitchLastTick, md.headPitch);
						md.bodyYawLastTick = Mth.lerp(0.05, md.bodyYawLastTick, md.bodyYaw);

						double bodyYaw = playerStateHandler.getMovementData().bodyYaw;
						float headRot = Functions.angleDifference((float)bodyYaw, Mth.wrapDegrees(player.yRot != 0.0 ? player.yRot : player.yHeadRot));

						if(rotateBodyWithCamera && !KeyInputHandler.FREE_LOOK.isDown() && !wasFreeLook){
							if(headRot > 150){
								bodyYaw += 150 - headRot;
							}else if(headRot < -150){
								bodyYaw -= 150 + headRot;
							}
						}
						headRot = (float)Mth.lerp(0.05, md.headYaw, headRot);


						double headPitch = Mth.lerp(0.1, md.headPitch, player.xRot);
						Vec3 moveVector = getInputVector(new Vec3(player.input.leftImpulse, 0, player.input.forwardImpulse), 1F, player.yRot);

						if(ServerFlightHandler.isFlying(player)){
							moveVector = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
						}

						float f = (float)Mth.atan2(moveVector.z, moveVector.x) * (180F / (float)Math.PI) - 90F;
						float f1 = (float)(Math.pow(moveVector.x, 2) + Math.pow(moveVector.z, 2));

						if(KeyInputHandler.FREE_LOOK.isDown()){
							wasFreeLook = true;
						}

						if(wasFreeLook && !Minecraft.getInstance().options.getCameraType().isFirstPerson()){
							wasFreeLook = false;
						}

						if(!firstPersonRotation && !KeyInputHandler.FREE_LOOK.isDown()){
							if((!wasFreeLook || moveVector.length() > 0) && Minecraft.getInstance().options.getCameraType().isFirstPerson()){
								bodyYaw = player.yRot;
								wasFreeLook = false;
								if(moveVector.length() > 0){
									float f5 = Mth.abs(Mth.wrapDegrees(player.yRot) - f);
									if(95.0F < f5 && f5 < 265.0F){
										f -= 180.0F;
									}

									float _f = Mth.wrapDegrees(f - (float)bodyYaw);
									bodyYaw += _f * 0.3F;
									float _f1 = Mth.wrapDegrees(player.yRot - (float)bodyYaw);

									if(_f1 < -75.0F){
										_f1 = -75.0F;
									}

									if(_f1 >= 75.0F){
										_f1 = 75.0F;

										bodyYaw = player.yRot - _f1;
										if(_f1 * _f1 > 2500.0F){
											bodyYaw += _f1 * 0.2F;
										}
									}
								}
								
								if(md.bodyYaw != bodyYaw || headRot != md.headYaw || headPitch != md.headPitch){
									bodyYaw = Mth.rotLerp(0.1f, (float)playerStateHandler.getMovementData().bodyYaw, (float)bodyYaw);
									bodyYaw = Mth.wrapDegrees(bodyYaw);

									playerStateHandler.setMovementData(bodyYaw, headRot, headPitch, playerStateHandler.getMovementData().bite);
									NetworkHandler.CHANNEL.sendToServer(new PacketSyncCapabilityMovement(player.getId(), md.bodyYaw, md.headYaw, md.headPitch, md.bite));
									return;
								}
							}
						}


						if(f1 > 0.000028){
							float f2 = Mth.wrapDegrees(f - (float)bodyYaw);
							bodyYaw += 0.5F * f2;

							if(minecraft.options.getCameraType() == CameraType.FIRST_PERSON){
								float f5 = Mth.abs(Mth.wrapDegrees(player.yRot) - f);
								if(95.0F < f5 && f5 < 265.0F){
									f -= 180.0F;
								}

								float _f = Mth.wrapDegrees(f - (float)bodyYaw);
								bodyYaw += _f * 0.3F;
								float _f1 = Mth.wrapDegrees(player.yRot - (float)bodyYaw);

								if(_f1 < -75.0F){
									_f1 = -75.0F;
								}

								if(_f1 >= 75.0F){
									_f1 = 75.0F;

									bodyYaw = player.yRot - _f1;
									if(_f1 * _f1 > 2500.0F){
										bodyYaw += _f1 * 0.2F;
									}
								}
							}
						}

						if(md.bodyYaw != bodyYaw || md.headYaw != headRot || md.headPitch != headPitch){
							bodyYaw = Mth.rotLerp(0.1f, (float)playerStateHandler.getMovementData().bodyYaw, (float)bodyYaw);
							bodyYaw = Mth.wrapDegrees(bodyYaw);

							playerStateHandler.setMovementData(bodyYaw, headRot, headPitch, playerStateHandler.getMovementData().bite);
							NetworkHandler.CHANNEL.sendToServer(new PacketSyncCapabilityMovement(player.getId(), md.bodyYaw, md.headYaw, md.headPitch, md.bite));
						}
					}
				});
			}
		}
	}

	public static Vec3 getInputVector(Vec3 movement, float fricSpeed, float yRot){
		double d0 = movement.lengthSqr();
		if(d0 < 1.0E-7D){
			return Vec3.ZERO;
		}else{
			Vec3 vector3d = (d0 > 1.0D ? movement.normalize() : movement).scale(fricSpeed);
			float f = Mth.sin(yRot * ((float)Math.PI / 180F));
			float f1 = Mth.cos(yRot * ((float)Math.PI / 180F));
			return new Vec3(vector3d.x * (double)f1 - vector3d.z * (double)f, vector3d.y, vector3d.z * (double)f1 + vector3d.x * (double)f);
		}
	}
}

//TODO Fix the problem that causes the dragon to take a T pose after disappearing from view. It doesn't matter if it's its own body or another player's. Occurs with forest dragon effect and in flight for any dragon. Also on the server when you turn away from the flying player and look at him again.