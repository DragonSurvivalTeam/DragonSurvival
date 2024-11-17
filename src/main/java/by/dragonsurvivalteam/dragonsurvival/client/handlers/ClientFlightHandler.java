package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.FastGlideSound;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorGameRenderer;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.flight.RequestSpinResync;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlightSpeed;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.TimeUnit;

/** Used in pair with {@link ServerFlightHandler} */
@Mod.EventBusSubscriber(Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientFlightHandler {
	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "notifyWingStatus", comment = "Notifies of wing status in chat message")
	public static Boolean notifyWingStatus = false;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "jumpToFly", comment = "Should flight be activated when jumping in the air")
	public static Boolean jumpToFly = false;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "lookAtSkyForFlight", comment = "Is it required to look up to start flying while jumping, requires that jumpToFly is on")
	public static Boolean lookAtSkyForFlight = false;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "flightZoomEffect", comment = "Should the zoom effect while gliding as a dragon be enabled")
	public static Boolean flightZoomEffect = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "flightCameraMovement", comment = "Should the camera movement while gliding as a dragon be enabled")
	public static Boolean flightCameraMovement = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "ownSpinParticles", comment = "Should particles from your own spin attack be displayed for you?")
	public static Boolean ownSpinParticles = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "othersSpinParticles", comment = "Should other players particles from spin attack be shown for you?")
	public static Boolean othersSpinParticles = true;

	@ConfigRange(min = -1000, max = 1000)
	@ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spinCooldownXOffset", comment = "Offset the x position of the spin cooldown indicator in relation to its normal position")
	public static Integer spinCooldownXOffset = 0;

	@ConfigRange(min = -1000, max = 1000)
	@ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spinCooldownYOffset", comment = "Offset the y position of the spin cooldown indicator in relation to its normal position")
	public static Integer spinCooldownYOffset = 0;

	public static final ResourceLocation SPIN_COOLDOWN = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/spin_cooldown.png");
	public static int lastSync;
	public static boolean wasGliding;
	public static boolean wasFlying;

	/** Acceleration */
	static double ax, ay, az; // TODO :: Turn into vector?
	static double lastIncrease;
	static float lastZoom = 1f;

	private static long lastHungerMessage;
	private static int levitationLeft;

	@SubscribeEvent
	public static void flightCamera(ViewportEvent.ComputeCameraAngles setup){
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer currentPlayer = minecraft.player;
		Camera info = setup.getCamera();

		if(currentPlayer != null && currentPlayer.isAddedToWorld()){
			DragonStateHandler dragonStateHandler = DragonUtils.getHandler(currentPlayer);
			AccessorGameRenderer gameRenderer = (AccessorGameRenderer)minecraft.gameRenderer;

			if(ServerFlightHandler.isGliding(currentPlayer)){
				if(setup.getCamera().isDetached()){

					if(flightCameraMovement){
						Vec3 lookVec = currentPlayer.getLookAngle();
						double increase = Mth.clamp(lookVec.y * 10, 0, lookVec.y * 5);
						double gradualIncrease = Mth.lerp(0.25, lastIncrease, increase);
						info.move(0, gradualIncrease, 0);
						lastIncrease = gradualIncrease;
					}
				}

				if(minecraft.player != null){
					if(flightZoomEffect){
						if(!minecraft.options.getCameraType().isFirstPerson()){
							Vec3 lookVec = currentPlayer.getLookAngle();
							float f = Math.min(Math.max(0.5F, 1F - (float)(lookVec.y * 5 / 2.5 * 0.5)), 3F);
							float newZoom = Mth.lerp(0.25f, lastZoom, f);
							gameRenderer.setZoom(newZoom);
							lastZoom = newZoom;
						}
					}
				}
			}else{
				if(lastIncrease > 0){
					if(flightCameraMovement){
						lastIncrease = Mth.lerp(0.25, lastIncrease, 0);
						info.move(0, lastIncrease, 0);
					}
				}

				if(lastZoom != 1){
					if(flightZoomEffect){
						lastZoom = Mth.lerp(0.25f, lastZoom, 1f);
						gameRenderer.setZoom(lastZoom);
					}
				}

				// Move the third person camera into a more suitable position if the player is too large (otherwise it ends up clipping inside the player)
				if(setup.getCamera().isDetached()) {
					if(dragonStateHandler.isDragon() && dragonStateHandler.getSize() > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
						// I'm not entirely sure why 20 works here, but it seems to be the magic number that
						// keeps the dragon's size from the camera's perspective constant.
						double offset = (dragonStateHandler.getSize() - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / 20;
						info.move(-offset, 0, 0);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void renderFlightCooldown(RenderGuiOverlayEvent.Post event){
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if (player == null || player.isSpectator()) {
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (!handler.isDragon()) {
			return;
		}

		if (!ServerFlightHandler.isFlying(player) && !ServerFlightHandler.canSwimSpin(player)) {
			return;
		}

		if (handler.getMovementData().spinLearned && handler.getMovementData().spinCooldown > 0) {
			if (event.getOverlay() == VanillaGuiOverlay.AIR_LEVEL.type()) {
				Window window = Minecraft.getInstance().getWindow();

				int cooldown = ServerFlightHandler.flightSpinCooldown * 20;
				float f = ((float) cooldown - (float) handler.getMovementData().spinCooldown) / (float) cooldown;

				int k = window.getGuiScaledWidth() / 2 - 66 / 2;
				int j = window.getGuiScaledHeight() - 96;

				k += spinCooldownXOffset;
				j += spinCooldownYOffset;

				int l = (int) (f * 62);
				event.getGuiGraphics().blit(SPIN_COOLDOWN, k, j, 0, 0, 66, 21, 256, 256);
				event.getGuiGraphics().blit(SPIN_COOLDOWN, k + 4, j + 1, 4, 21, l, 21, 256, 256);
			}
		}
	}

	@SubscribeEvent
	public static void flightParticles(TickEvent.PlayerTickEvent playerTickEvent){
		if(playerTickEvent.phase == Phase.START || playerTickEvent.side == LogicalSide.SERVER){
			return;
		}
		Player player = playerTickEvent.player;
		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if(handler.isDragon()){
				if(handler.getMovementData().spinAttack > 0){
					if(!ownSpinParticles && player == Minecraft.getInstance().player){
						return;
					}
					if(!othersSpinParticles && player != Minecraft.getInstance().player){
						return;
					}


					if(player.tickCount - lastSync >= 20){ // TODO :: Is this necessary?
						//Request the server to resync the status of a spin if it is has been too long since the last update
						NetworkHandler.CHANNEL.sendToServer(new RequestSpinResync());
					}

					if(ServerFlightHandler.canSwimSpin(player) && ServerFlightHandler.isSpin(player)){
						spawnSpinParticle(player, player.isInWater() ? ParticleTypes.BUBBLE_COLUMN_UP : ParticleTypes.LAVA);
					}

					if(EnchantmentHelper.getFireAspect(player) > 0){
						spawnSpinParticle(player, ParticleTypes.LAVA);
					}else if(EnchantmentHelper.getKnockbackBonus(player) > 0){
						spawnSpinParticle(player, ParticleTypes.EXPLOSION);
					}else if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING_EDGE, player) > 0){
						spawnSpinParticle(player, ParticleTypes.SWEEP_ATTACK);
					}else if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, player) > 0){
						spawnSpinParticle(player, new DustParticleOptions(new Vector3f(1f, 1f, 1f), 1f));
					}else if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SMITE, player) > 0){
						spawnSpinParticle(player, ParticleTypes.ENCHANT);
					}else if(EnchantmentHelper.getEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, player) > 0){
						spawnSpinParticle(player, ParticleTypes.DRIPPING_OBSIDIAN_TEAR);
					}
				}
			}
		});
	}

	private static void spawnSpinParticle(Player player, ParticleOptions particleData){
		for(int i = 0; i < 20; i++){
			double d0 = (player.getRandom().nextFloat() - 0.5) * 2;
			double d1 = (player.getRandom().nextFloat() - 0.5) * 2;
			double d2 = (player.getRandom().nextFloat() - 0.5) * 2;

			double posX = player.position().x + player.getDeltaMovement().x + d0;
			double posY = player.position().y - 1.5 + player.getDeltaMovement().y + d1;
			double posZ = player.position().z + player.getDeltaMovement().z + d2;
			player.level().addParticle(particleData, posX, posY, posZ, player.getDeltaMovement().x * -1, player.getDeltaMovement().y * -1, player.getDeltaMovement().z * -1);
		}
	}

	/** Controls acceleration */
	@SubscribeEvent
	public static void flightControl(final ClientTickEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		
		if (event.phase.equals(ClientTickEvent.Phase.START)) {
			return;
		}

		if (player != null && !player.isPassenger() && !Minecraft.getInstance().isPaused()) {
			if (player.hasEffect(MobEffects.LEVITATION)) {
				/* TODO
				To make fall damage work you'd have to:
					- call `player.resetFallDistance()` when the levitation effect is applied (MobEffectEvent.Added)
					- add a check in ServerFlightHandler#changeFallDistance
				*/
				levitationLeft = Functions.secondsToTicks(ServerFlightHandler.levitationAfterEffect);
			} else if (levitationLeft > 0) {
				// TODO :: Set to 0 once ground is reached?
				if (event.phase == Phase.END) {
					levitationLeft--;
				}
			} else {
				DragonStateProvider.getCap(player).ifPresent(handler -> {
					if (handler.isDragon()) {
						Double flightMult = 1.0;
						if (DragonUtils.getDragonBody(handler) != null) { flightMult = DragonUtils.getDragonBody(handler).getFlightMult(); }

						Vec3 viewVector = player.getLookAngle();
						double yaw = Math.toRadians(player.getYHeadRot() + 90);

						// Only apply while in water (not while flying)
						if (ServerFlightHandler.canSwimSpin(player) && ServerFlightHandler.isSpin(player)) {
							Input movement = player.input;

							Vec3 deltaMovement = player.getDeltaMovement();

							double maxFlightSpeed = ServerFlightHandler.maxFlightSpeed;
							// FIXME :: Magic numbers at various places
							ax = Mth.clamp(ax, -0.4 * maxFlightSpeed, 0.4 * maxFlightSpeed);
							az = Mth.clamp(az, -0.4 * maxFlightSpeed, 0.4 * maxFlightSpeed);

							// Increase acceleration depending on how sharply the player turns their character
							ax += Math.cos(yaw) / 500 * 50 * 2;
							az += Math.sin(yaw) / 500 * 50 * 2;
							ay = viewVector.y / 8;

							if (viewVector.y < 0) {
								deltaMovement = deltaMovement.add(ax, 0, az);
							} else {
								// Only increase height if the player is looking up
								deltaMovement = deltaMovement.add(ax, ay, az);
							}

							// TODO :: Why 0.99 etc.?
							deltaMovement = deltaMovement.multiply(0.99F, 0.98F, 0.99F);

							player.setDeltaMovement(deltaMovement);
							ay = player.getDeltaMovement().y;
						}

						if (handler.isWingsSpread()) {
							Input movement = player.input;
							boolean hasFood = player.getFoodData().getFoodLevel() > ServerFlightHandler.flightHungerThreshold || player.isCreative() || ServerFlightHandler.allowFlyingWithoutHunger;

							if (!hasFood) {
								// TODO :: If you use Math.abs you always get a positive number, shouldn't this be max() instead of clamp()?
								ay = Mth.clamp(Math.abs(ay * 4), -0.4 * ServerFlightHandler.maxFlightSpeed, 0.4 * ServerFlightHandler.maxFlightSpeed);
							}

							if (ServerFlightHandler.isFlying(player)) {
								if (!wasFlying) {
									wasFlying = true;
								}

								Vec3 deltaMovement = player.getDeltaMovement();

								double horizontalView = viewVector.horizontalDistance();
								double horizontalMovement = deltaMovement.horizontalDistance();
								double lookMagnitude = viewVector.length();

								float pitch = (float) Math.toRadians(player.getXRot());
								float verticalDelta = Mth.cos(pitch);

								verticalDelta = (float) ((double) verticalDelta * (double) verticalDelta * Math.min(1.0D, lookMagnitude / 0.4D));
								double gravity = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).getValue();

								if (ServerFlightHandler.isGliding(player)) {
									if (!wasGliding) {
										Minecraft.getInstance().getSoundManager().play(new FastGlideSound(player));
										wasGliding = true;
									}
								}

								if (ServerFlightHandler.isGliding(player) || ax != 0 || az != 0) {
									deltaMovement = player.getDeltaMovement().add(0.0D, gravity * (-1.0D + (double) verticalDelta * 0.75D), 0.0D);

									if (deltaMovement.y < 0 && horizontalView > 0) {
										double downwardMomentum = deltaMovement.y * -0.1D * (double) verticalDelta * flightMult;
										deltaMovement = deltaMovement.add(viewVector.x * downwardMomentum / horizontalView, downwardMomentum, viewVector.z * downwardMomentum / horizontalView);
									}

									if (pitch < 0 && horizontalView > 0) {
										// Handle movement when the player makes turns
										double delta = horizontalMovement * -Mth.sin(pitch) * 0.04D * flightMult;
										deltaMovement = deltaMovement.add(-viewVector.x * delta / horizontalView, delta * 3.2D, -viewVector.z * delta / horizontalView);
									}

									if (horizontalView > 0) {
										deltaMovement = deltaMovement.add((viewVector.x * flightMult / horizontalView * horizontalMovement - deltaMovement.x) * 0.1D, 0.0D, (viewVector.z * flightMult / horizontalView * horizontalMovement - deltaMovement.z) * 0.1D);
									}

									// Increase speed while flying down or height when flying up
									if (viewVector.y < 0) {
										ax += (Math.cos(yaw) * flightMult * 2) / 500;
										az += (Math.sin(yaw) * flightMult * 2) / 500;
									} else {
										ay = viewVector.y / 4;
										ax *= 0.98;
										az *= 0.98;
									}

									double speedLimit = ServerFlightHandler.maxFlightSpeed * flightMult;
									ax = Mth.clamp(ax, -0.4 * speedLimit, 0.4 * speedLimit);
									az = Mth.clamp(az, -0.4 * speedLimit, 0.4 * speedLimit);

									if (ServerFlightHandler.isSpin(player)) { // TODO :: If the spin move is used in water won't the acceleration be applied twice?
										ax += (Math.cos(yaw) * flightMult * 100 * 2) / 500;
										az += (Math.sin(yaw) * flightMult * 100 * 2) / 500;
										ay = viewVector.y / 4;
									}

									if (ServerFlightHandler.isGliding(player)) {
										if (viewVector.y < 0) {
											deltaMovement = deltaMovement.add(ax, 0, az);
										} else if (Math.abs(horizontalMovement) > 0.4 || ServerFlightHandler.unlimitedFlightAcceleration) {
											deltaMovement = deltaMovement.add(ax, ay, az);
										} else {
											deltaMovement = deltaMovement.add(ax, ay * horizontalMovement, az);
										}

										deltaMovement = deltaMovement.multiply(0.99F, 0.98F, 0.99F);

										player.setDeltaMovement(deltaMovement);
										ay = player.getDeltaMovement().y;
									}
								}

								if (!ServerFlightHandler.isGliding(player)) {
									wasGliding = false;
									double maxForward = 0.5 * flightMult * 2;

									Vec3 moveVector = ClientDragonRender.getInputVector(new Vec3(movement.leftImpulse, 0, movement.forwardImpulse), 1F, player.yRot);
									moveVector.multiply(1.3 * flightMult * 2, 0, 1.3 * flightMult * 2);

									boolean moving = movement.up || movement.down || movement.left || movement.right;

									if (ServerFlightHandler.isSpin(player)) {
										ax += (Math.cos(yaw) * flightMult * 200 * 2) / 500;
										az += (Math.sin(yaw) * flightMult * 200 * 2) / 500;
										ay = viewVector.y / 8;
									}

									if (ServerFlightHandler.stableHover && !movement.jumping && !movement.shiftKeyDown && !ServerFlightHandler.isSpin(player) && !ServerFlightHandler.isGliding(player)) {
										ay = Math.max(ay, gravity * 1.1);
									}

									if (moving && !movement.jumping && !movement.shiftKeyDown) {
										maxForward = 0.8 * flightMult * 2;
										moveVector.multiply(1.4 * flightMult * 2, 0, 1.4 * flightMult * 2);
										deltaMovement = new Vec3(Mth.lerp(0.14, deltaMovement.x, moveVector.x), 0, Mth.lerp(0.14, deltaMovement.z, moveVector.z));
										deltaMovement = new Vec3(Mth.clamp(deltaMovement.x, -maxForward, maxForward), 0, Mth.clamp(deltaMovement.z, -maxForward, maxForward));

										deltaMovement = deltaMovement.add(ax, ay, az);

										ax *= 0.9F;
										ay *= 0.9F;
										az *= 0.9F;

										if (!ServerFlightHandler.stableHover) {
											deltaMovement = new Vec3(deltaMovement.x, -(gravity * 2) + deltaMovement.y, deltaMovement.z);
										} else {
											deltaMovement = new Vec3(deltaMovement.x, -gravity + deltaMovement.y, deltaMovement.z);
										}

										player.setDeltaMovement(deltaMovement);
									} else {
										deltaMovement = deltaMovement.multiply(0.99F, 0.98F, 0.99F);
										deltaMovement = new Vec3(Mth.lerp(0.14, deltaMovement.x, moveVector.x), 0, Mth.lerp(0.14, deltaMovement.z, moveVector.z));
										deltaMovement = new Vec3(Mth.clamp(deltaMovement.x, -maxForward, maxForward), 0, Mth.clamp(deltaMovement.z, -maxForward, maxForward));

										deltaMovement = deltaMovement.add(ax, ay, az);

										if (ServerFlightHandler.isSpin(player)) {
											deltaMovement.multiply(10, 10, 10);
										}

										ax *= 0.9F;
										ay *= 0.9F;
										az *= 0.9F;

										if (movement.jumping) {
											deltaMovement = new Vec3(deltaMovement.x, 0.4 + deltaMovement.y, deltaMovement.z);
											player.setDeltaMovement(deltaMovement);
										} else if (movement.shiftKeyDown) {
											deltaMovement = new Vec3(deltaMovement.x, -0.5 + deltaMovement.y, deltaMovement.z);
											player.setDeltaMovement(deltaMovement);
										} else if (wasFlying) { // Don't activate on a regular jump
											double yMotion = hasFood ? -gravity + ay : -(gravity * 4) + ay;
											deltaMovement = new Vec3(deltaMovement.x, yMotion, deltaMovement.z);
											player.setDeltaMovement(deltaMovement);
										}
									}
								}
							} else {
								wasGliding = false;
								wasFlying = false;
								ax = 0;
								az = 0;
								ay = 0;
							}
						} else {
							ax = 0;
							az = 0;
							ay = 0;
						}
					}
				});
			}

			if (event.phase == Phase.END && player.tickCount % 5 == 0) { // TODO :: Some checks to avoid too many packets
				// Delta movement is not part of the regular sync (server itself seems to only keep track of the y value?)
				// TODO :: Check ClientboundSetEntityMotionPacket
				// Currently still used for ServerFlightHandler (there might be some other part which runs for other players too)
				NetworkHandler.CHANNEL.sendToServer(new SyncFlightSpeed(player.getId(), player.getDeltaMovement()));
			}
		}
	}

	@SubscribeEvent
	public static void spin(InputEvent.MouseButton keyInputEvent){
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if(player == null){
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);
		if(!handler.isDragon()){
			return;
		}

		if(Keybind.SPIN_ABILITY.getKey().getValue() == keyInputEvent.getButton()){
			spinKeybind(player, handler);
		}
	}

	private static void spinKeybind(LocalPlayer player, DragonStateHandler handler){
		if(!ServerFlightHandler.isSpin(player) && handler.getMovementData().spinCooldown <= 0 && handler.getMovementData().spinLearned){
			if(ServerFlightHandler.isFlying(player) || ServerFlightHandler.canSwimSpin(player)){
				handler.getMovementData().spinAttack = ServerFlightHandler.spinDuration;
				handler.getMovementData().spinCooldown = ServerFlightHandler.flightSpinCooldown * 20;
				NetworkHandler.CHANNEL.sendToServer(new SyncSpinStatus(player.getId(), handler.getMovementData().spinAttack, handler.getMovementData().spinCooldown, handler.getMovementData().spinLearned));
			}
		}
	}

	@SubscribeEvent
	public static void toggleWings(InputEvent.Key keyInputEvent){
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if(player == null){
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);
		if(handler == null || !handler.isDragon()){
			return;
		}

		boolean currentState = handler.isWingsSpread();
		Vec3 lookVec = player.getLookAngle();

		if(Keybind.SPIN_ABILITY.getKey().getValue() == keyInputEvent.getKey()){
			spinKeybind(player, handler);
		}

		if(jumpToFly && !player.isCreative() && !player.isSpectator()){
			if(minecraft.options.keyJump.isDown()){
				if(keyInputEvent.getAction() == GLFW.GLFW_PRESS){
					if(handler.hasFlight() && !currentState && (lookVec.y > 0.8 || !lookAtSkyForFlight)){
						if(!player.onGround() && !player.isInLava() && !player.isInWater()){
							if(player.getFoodData().getFoodLevel() > ServerFlightHandler.flightHungerThreshold || player.isCreative() || ServerFlightHandler.allowFlyingWithoutHunger){
								NetworkHandler.CHANNEL.sendToServer(new SyncFlyingStatus(player.getId(), true));
							}else{
								if(lastHungerMessage == 0 || lastHungerMessage + TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS) < System.currentTimeMillis()){
									lastHungerMessage = System.currentTimeMillis();
									player.sendSystemMessage(Component.translatable("ds.wings.nohunger"));
								}
							}
						}
					}
				}
			}
		}

		if(Keybind.TOGGLE_WINGS.consumeClick()){
			if(handler.hasFlight()){
				//Allows toggling the wings if food level is above 0, player is creative, wings are already enabled (allows disabling even when hungry) or if config options is turned on
				if(!player.hasEffect(DragonEffects.TRAPPED) && (player.getFoodData().getFoodLevel() > ServerFlightHandler.flightHungerThreshold || player.isCreative() || currentState || ServerFlightHandler.allowFlyingWithoutHunger)){
					NetworkHandler.CHANNEL.sendToServer(new SyncFlyingStatus(player.getId(), !currentState));
					if(notifyWingStatus){
						if(!currentState){
							player.sendSystemMessage(Component.translatable("ds.wings.enabled"));
						}else{
							player.sendSystemMessage(Component.translatable("ds.wings.disabled"));
						}
					}
				}else{
					if(!player.hasEffect(DragonEffects.TRAPPED))
					{
						player.sendSystemMessage(Component.translatable("ds.wings.nohunger"));
					}
				}
			}else{
				player.sendSystemMessage(Component.translatable("ds.you.have.no.wings"));
			}
		}
	}
}