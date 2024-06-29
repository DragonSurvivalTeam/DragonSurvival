package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.sounds.FastGlideSound;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.ActionWithCooldown;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.Window;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

/** Used in pair with {@link ServerFlightHandler} */
@EventBusSubscriber(Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientFlightHandler {
	@ConfigRange(min = 0, max = 60)
	@ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "levitationAfterEffect", comment = "For how many seconds wings are disabled after the levitation effect has ended")
	public static Integer levitationAfterEffect = 3;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "jumpToFly", comment = "Should flight be activated when jumping in the air")
	public static Boolean jumpToFly = false;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "lookAtSkyForFlight", comment = "Is it required to look up to start flying while jumping, requires that jumpToFly is on")
	public static Boolean lookAtSkyForFlight = false;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "flightZoomEffect", comment = "Should the zoom effect while gliding as a dragon be enabled")
	public static Boolean flightZoomEffect = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "flightCameraMovement", comment = "Should the camera movement while gliding as a dragon be enabled")
	public static Boolean flightCameraMovement = true;

	@ConfigRange(min = -1000, max = 1000)
	@ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spinCooldownXOffset", comment = "Offset the x position of the spin cooldown indicator in relation to its normal position")
	public static Integer spinCooldownXOffset = 0;

	@ConfigRange(min = -1000, max = 1000)
	@ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spinCooldownYOffset", comment = "Offset the y position of the spin cooldown indicator in relation to its normal position")
	public static Integer spinCooldownYOffset = 0;

	public static final ResourceLocation SPIN_COOLDOWN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/spin_cooldown.png");
	public static int lastSync;
	public static boolean wasGliding;
	public static boolean wasFlying;

	/** Acceleration */
	static double ax, ay, az; // TODO :: Turn into vector?
	static float lastIncrease;
	static float lastZoom = 1f;

	private static final ActionWithCooldown hungerMessageWithCooldown = new ActionWithCooldown(30_000, () -> {
		var localPlayer = Minecraft.getInstance().player;
		if (localPlayer == null) return;
		localPlayer.sendSystemMessage(Component.translatable("ds.wings.nohunger"));
	});
	private static int levitationLeft;

	@SubscribeEvent
	public static void flightCamera(CalculateDetachedCameraDistanceEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if (handler.isDragon()) {
				// I'm not entirely sure why 20 works here, but it seems to be the magic number that
				// keeps the dragon's size from the camera's perspective constant.
				float offset = (float) ((handler.getSize() - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / 20);
				event.setDistance(event.getDistance() + offset + 1.5f);
			}
		});
	}

	@SubscribeEvent
	public static void flightCamera(ViewportEvent.ComputeCameraAngles setup){
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer currentPlayer = minecraft.player;
		Camera info = setup.getCamera();

		if(currentPlayer != null && currentPlayer.isAddedToWorld() && DragonStateProvider.isDragon(currentPlayer)){
			DragonStateHandler dragonStateHandler = DragonStateProvider.getOrGenerateHandler(currentPlayer);
			GameRenderer gameRenderer = minecraft.gameRenderer;

			if(ServerFlightHandler.isGliding(currentPlayer)){
				if(setup.getCamera().isDetached()){

					if(flightCameraMovement){
						Vec3 lookVec = currentPlayer.getLookAngle();
						float increase = (float) Mth.clamp(lookVec.y * 10, 0, lookVec.y * 5);
						float gradualIncrease = Mth.lerp(0.25f, lastIncrease, increase);
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
							gameRenderer.zoom = newZoom;
							lastZoom = newZoom;
						}
					}
				}
			}else{
				if(lastIncrease > 0){
					if(flightCameraMovement){
						lastIncrease = Mth.lerp(0.25f, lastIncrease, 0);
						info.move(0, lastIncrease, 0);
					}
				}

				if(lastZoom != 1){
					if(flightZoomEffect){
						lastZoom = Mth.lerp(0.25f, lastZoom, 1f);
						gameRenderer.zoom = lastZoom;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void renderFlightCooldown(RenderGuiLayerEvent.Post event){
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;

		if (player == null || player.isSpectator()) {
			return;
		}

		DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);

		if (!handler.isDragon()) {
			return;
		}

		if (!ServerFlightHandler.isFlying(player) && !ServerFlightHandler.canSwimSpin(player)) {
			return;
		}

		if (handler.getMovementData().spinLearned && handler.getMovementData().spinCooldown > 0) {
			if (event.getName() == VanillaGuiLayers.AIR_LEVEL) {
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
	@OnlyIn(Dist.CLIENT)
	public static void flightParticles(PlayerTickEvent.Post playerTickEvent){
		Player player = playerTickEvent.getEntity();
		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if(handler.isDragon()){
				if(handler.getMovementData().spinAttack > 0){

					// TODO: Removed because I don't think it does anything. Prove me wrong!
					/*if(player.tickCount - lastSync >= 20){
						//Request the server to resync the status of a spin if it is has been too long since the last update
						NetworkHandler.CHANNEL.sendToServer(new RequestSpinResync());
					}*/

					if(ServerFlightHandler.canSwimSpin(player) && ServerFlightHandler.isSpin(player)){
						spawnSpinParticle(player, player.isInWater() ? ParticleTypes.BUBBLE_COLUMN_UP : ParticleTypes.LAVA);
					}

					Holder<Enchantment> fireAspect = player.level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.FIRE_ASPECT);
					Holder<Enchantment> knockback = player.level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.KNOCKBACK);
					Holder<Enchantment> sweepingEdge = player.level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.SWEEPING_EDGE);
					Holder<Enchantment> sharpness = player.level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.SHARPNESS);
					Holder<Enchantment> smite = player.level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.SMITE);
					Holder<Enchantment> baneOfArthropods = player.level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.BANE_OF_ARTHROPODS);

					if(EnchantmentHelper.getEnchantmentLevel(fireAspect, player) > 0){
						spawnSpinParticle(player, ParticleTypes.LAVA);
					}else if(EnchantmentHelper.getEnchantmentLevel(knockback, player) > 0){
						spawnSpinParticle(player, ParticleTypes.EXPLOSION);
					}else if(EnchantmentHelper.getEnchantmentLevel(sweepingEdge, player) > 0){
						spawnSpinParticle(player, ParticleTypes.SWEEP_ATTACK);
					}else if(EnchantmentHelper.getEnchantmentLevel(sharpness, player) > 0){
						spawnSpinParticle(player, new DustParticleOptions(new Vector3f(1f, 1f, 1f), 1f));
					}else if(EnchantmentHelper.getEnchantmentLevel(smite, player) > 0){
						spawnSpinParticle(player, ParticleTypes.ENCHANT);
					}else if(EnchantmentHelper.getEnchantmentLevel(baneOfArthropods, player) > 0){
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
	public static void flightControl(final ClientTickEvent.Pre event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;

		if (player != null && !player.isPassenger()) {
			if (player.hasEffect(MobEffects.LEVITATION)) {
				/* TODO
				To make fall damage work you'd have to:
					- call `player.resetFallDistance()` when the levitation effect is applied (MobEffectEvent.Added)
					- add a check in ServerFlightHandler#changeFallDistance
				*/
				levitationLeft = Functions.secondsToTicks(levitationAfterEffect);
			} else if (levitationLeft > 0) {
				// TODO :: Set to 0 once ground is reached?
				levitationLeft--;
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
							// TODO :: Magic numbers at various places
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

							player.setDeltaMovement(deltaMovement);
							ay = player.getDeltaMovement().y;
						}

						if (handler.isWingsSpread()) {
							Input movement = player.input;
							boolean hasFood = player.getFoodData().getFoodLevel() > ServerFlightHandler.flightHungerThreshold || player.isCreative();

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
								double gravity = player.getAttribute(Attributes.GRAVITY).getValue();

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
										} else if (Math.abs(horizontalMovement) > 0.4) {
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

									Vec3 moveVector = getInputVector(new Vec3(movement.leftImpulse, 0, movement.forwardImpulse), 1F, player.getYRot());
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
		}
	}

	// FIXME: spin and toggleWings are duplicates for handling mouse and keyboard inputs
	// Handling should be done by checking KeyMappings while listening to ClientTickEvent.Post via #consumeClick()
	// Spin is currently checked for both times, but toggleWings only for the keyboard
	@SubscribeEvent
	public static void spin(InputEvent.MouseButton.Pre keyInputEvent) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) return;

		DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);
		if (!handler.isDragon()) return;

		if (KeyInputHandler.SPIN_ABILITY.getKey().getValue() == keyInputEvent.getButton()) {
			spinKeybind(player, handler);
		}
	}

	private static void spinKeybind(LocalPlayer player, DragonStateHandler handler) {
		if (ServerFlightHandler.isSpin(player)) return;
		if (handler.getMovementData().spinCooldown > 0) return;
		if (!handler.getMovementData().spinLearned) return;

		if (ServerFlightHandler.isFlying(player) || ServerFlightHandler.canSwimSpin(player)) {
			handler.getMovementData().spinAttack = ServerFlightHandler.spinDuration;
			handler.getMovementData().spinCooldown = ServerFlightHandler.flightSpinCooldown * 20;
			PacketDistributor.sendToServer(
					new SyncSpinStatus.Data(
							player.getId(),
							handler.getMovementData().spinAttack,
							handler.getMovementData().spinCooldown,
							handler.getMovementData().spinLearned
					)
			);
		}
	}

	@SubscribeEvent
	public static void toggleWings(InputEvent.Key keyInputEvent) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}

		DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);
		if (handler == null || !handler.isDragon()) {
			return;
		}

		if (KeyInputHandler.SPIN_ABILITY.getKey().getValue() == keyInputEvent.getKey()) {
			spinKeybind(player, handler);
		}

		if (jumpToFly && minecraft.options.keyJump.isDown() && keyInputEvent.getAction() == GLFW.GLFW_PRESS) {
			tryJumpToFly(player, handler);
		}

		if (KeyInputHandler.TOGGLE_WINGS.consumeClick()) {
			tryManualToggleWings(player, handler);
		}
	}

	private static void tryManualToggleWings(LocalPlayer player, DragonStateHandler handler) {
		if (!handler.hasFlight()) {
			player.sendSystemMessage(Component.translatable("ds.you.have.no.wings"));
			return;
		}

		if (isTrapped(player)) return;

		//Allows toggling the wings if food level is above 0, player is creative, wings are already enabled (allows disabling even when hungry) or if config options is turned on
		// NOTE: Folding wings is currently disallowed when trapped, but the Trapped status effect already folds wings
		boolean isWingsSpread = handler.isWingsSpread();
		if (isWingsSpread || player.isCreative() || hasEnoughFoodToStartFlight(player)) {
			PacketDistributor.sendToServer(new SyncFlyingStatus.Data(player.getId(), !isWingsSpread));
			return;
		}

		player.sendSystemMessage(Component.translatable("ds.wings.nohunger"));
	}

	private static void tryJumpToFly(LocalPlayer player, DragonStateHandler handler) {
		if (player.isCreative() || player.isSpectator()) return;

		if (!handler.hasFlight()) return;
		if (handler.isWingsSpread()) return;

		if (isTrapped(player)) return;

		Vec3 lookVec = player.getLookAngle();
		if (lookAtSkyForFlight && lookVec.y <= 0.8) return;

		if (player.onGround() || player.isInLava() || player.isInWater()) return;

		if (hasEnoughFoodToStartFlight(player)) { // Creative players are already disallowed from using jump-to-fly
			PacketDistributor.sendToServer(new SyncFlyingStatus.Data(player.getId(), true));
			return;
		}

		hungerMessageWithCooldown.tryRun();
	}

	private static boolean isTrapped(LivingEntity entity) {
		return entity.hasEffect(DSEffects.TRAPPED);
	}

	private static boolean hasEnoughFoodToStartFlight(Player player) {
		return player.getFoodData().getFoodLevel() > ServerFlightHandler.flightHungerThreshold;
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
