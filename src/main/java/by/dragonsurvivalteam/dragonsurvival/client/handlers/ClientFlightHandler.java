package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.FastGlideSound;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;

import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorGameRenderer;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.flight.RequestSpinResync;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlightSpeed;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.TimeUnit;

/**
 * Used in pair with {@link ServerFlightHandler}
 */
@Mod.EventBusSubscriber( Dist.CLIENT )
@SuppressWarnings( "unused" )
public class ClientFlightHandler{

	public static final ResourceLocation SPIN_COOLDOWN = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/spin_cooldown.png");
	public static int lastSync;
	public static boolean wasGliding = false;
	public static boolean wasFlying = false;

	@ConfigOption( side = Dist.CLIENT, category = "flight", key = "notifyWingStatus", comment = "Notifies of wing status in chat message" )
	public static Boolean notifyWingStatus = false;

	@ConfigOption( side = Dist.CLIENT, category = "flight", key = "jumpToFly", comment = "Should flight be activated when jumping in the air" )
	public static Boolean jumpToFly = false;

	@ConfigOption( side = Dist.CLIENT, category = "flight", key = "lookAtSkyForFlight", comment = "Is it required to look up to start flying while jumping, requires that jumpToFly is on" )
	public static Boolean lookAtSkyForFlight = false;

	@ConfigOption( side = Dist.CLIENT, category = "flight", key = "flightZoomEffect", comment = "Should the zoom effect while gliding as a dragon be enabled" )
	public static Boolean flightZoomEffect = true;

	@ConfigOption( side = Dist.CLIENT, category = "flight", key = "flightCameraMovement", comment = "Should the camera movement while gliding as a dragon be enabled" )
	public static Boolean flightCameraMovement = true;

	@ConfigOption( side = Dist.CLIENT, category = "flight", key = "ownSpinParticles", comment = "Should particles from your own spin attack be displayed for you?" )
	public static Boolean ownSpinParticles = true;

	@ConfigOption( side = Dist.CLIENT, category = "flight", key = "othersSpinParticles", comment = "Should other players particles from spin attack be shown for you?" )
	public static Boolean othersSpinParticles = true;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "spin"}, key = "spinCooldownXOffset", comment = "Offset the x position of the spin cooldown indicator in relation to its normal position" )
	public static Integer spinCooldownXOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "spin"}, key = "spinCooldownYOffset", comment = "Offset the y position of the spin cooldown indicator in relation to its normal position" )
	public static Integer spinCooldownYOffset = 0;
	/**
	 * Acceleration
	 */
	static double ax, ay, az;
	static double lastIncrease;
	static float lastZoom = 1f;
	private static long lastHungerMessage;

	@SubscribeEvent
	public static void flightCamera(CameraSetup setup){
		LocalPlayer currentPlayer = Minecraft.getInstance().player;
		Camera info = setup.getCamera();

		if(currentPlayer != null){
			DragonStateHandler dragonStateHandler = DragonUtils.getHandler(currentPlayer);
			AccessorGameRenderer gameRenderer = (AccessorGameRenderer)Minecraft.getInstance().gameRenderer;

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

				if(Minecraft.getInstance().player != null){
					if(flightZoomEffect){
						if(!Minecraft.getInstance().options.getCameraType().isFirstPerson()){
							Vec3 lookVec = currentPlayer.getLookAngle();
							float f = Math.min(Math.max(0.5F, 1F - (float)(((lookVec.y * 5) / 2.5) * 0.5)), 3F);
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
			}
		}
	}

	@SubscribeEvent
	public static void renderFlightCooldown(RenderGameOverlayEvent.Post event){
		Player player = Minecraft.getInstance().player;

		if(player == null || !DragonUtils.isDragon(player) || player.isSpectator()){
			return;
		}

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(!ServerFlightHandler.isFlying(player) && !ServerFlightHandler.canSwimSpin(player)){
				return;
			}

			if(cap.getMovementData().spinLearned && cap.getMovementData().spinCooldown > 0){
				if(event.getType() == ElementType.ALL){
					event.getMatrixStack().pushPose();

					TextureManager textureManager = Minecraft.getInstance().getTextureManager();
					Window window = Minecraft.getInstance().getWindow();
					RenderSystem.setShaderTexture(0, SPIN_COOLDOWN);

					int cooldown = ServerConfig.flightSpinCooldown * 20;
					float f = ((float)cooldown - (float)cap.getMovementData().spinCooldown) / (float)cooldown;

					int k = (window.getGuiScaledWidth() / 2) - (66 / 2);
					int j = window.getGuiScaledHeight() - 96;

					k += spinCooldownXOffset;
					j += spinCooldownYOffset;

					int l = (int)(f * 62);
					Screen.blit(event.getMatrixStack(), k, j, 0, 0, 66, 21, 256, 256);
					Screen.blit(event.getMatrixStack(), k + 4, j + 1, 4, 21, l, 21, 256, 256);

					event.getMatrixStack().popPose();
				}
			}
		});
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


					if(player.tickCount - lastSync >= 20){
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
			double d0 = (player.level.random.nextFloat() - 0.5) * 2;
			double d1 = (player.level.random.nextFloat() - 0.5) * 2;
			double d2 = (player.level.random.nextFloat() - 0.5) * 2;

			double posX = player.position().x + player.getDeltaMovement().x + d0;
			double posY = player.position().y - 1.5 + player.getDeltaMovement().y + d1;
			double posZ = player.position().z + player.getDeltaMovement().z + d2;
			player.level.addParticle(particleData, posX, posY, posZ, player.getDeltaMovement().x * -1, player.getDeltaMovement().y * -1, player.getDeltaMovement().z * -1);
		}
	}

	/**
	 * Controls acceleration
	 */
	@SubscribeEvent
	public static void flightControl(ClientTickEvent tickEvent){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player != null && !player.isPassenger()){
			DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
				if(dragonStateHandler.isDragon()){
					if(ServerFlightHandler.canSwimSpin(player) && ServerFlightHandler.isSpin(player)){
						Input movement = player.input;

						Vec3 motion = player.getDeltaMovement();
						Vec3 lookVec = player.getLookAngle();

						double yaw = Math.toRadians(player.yHeadRot + 90);
						double lookY = lookVec.y;

						double speedLimit = ServerConfig.maxFlightSpeed;
						ax = Mth.clamp(ax, -0.2 * speedLimit, 0.2 * speedLimit);
						az = Mth.clamp(az, -0.2 * speedLimit, 0.2 * speedLimit);

						ax += (Math.cos(yaw) / 500) * 50;
						az += (Math.sin(yaw) / 500) * 50;
						ay = lookVec.y / 8;

						if(lookY < 0){
							motion = motion.add(ax, 0, az);
						}else{
							motion = motion.add(ax, ay, az);
						}
						motion = motion.multiply(0.99F, 0.98F, 0.99F);

						if(motion.length() != player.getDeltaMovement().length()){
							NetworkHandler.CHANNEL.sendToServer(new SyncFlightSpeed(player.getId(), motion));
						}

						player.setDeltaMovement(motion);
						ay = player.getDeltaMovement().y;
					}

					if(dragonStateHandler.isWingsSpread()){
						Input movement = player.input;
						boolean hasFood = player.getFoodData().getFoodLevel() > ServerConfig.flightHungerThreshold || player.isCreative() || ServerConfig.allowFlyingWithoutHunger;


						if(!hasFood){
							ay = Mth.clamp(Math.abs(ay * 4), -0.2 * ServerConfig.maxFlightSpeed, 0.2 * ServerConfig.maxFlightSpeed);
						}

						//start
						if(ServerFlightHandler.isFlying(player)){
							if(!wasFlying){
								wasFlying = true;
							}

							Vec3 motion = player.getDeltaMovement();
							Vec3 lookVec = player.getLookAngle();
							float f6 = player.xRot * ((float)Math.PI / 180F);
							double d9 = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
							double d11 = Math.sqrt(motion.horizontalDistanceSqr());
							double d12 = lookVec.length();
							float f3 = Mth.cos(f6);
							f3 = (float)((double)f3 * (double)f3 * Math.min(1.0D, d12 / 0.4D));
							AttributeInstance gravity = player.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
							double g = gravity.getValue();

							if(ServerFlightHandler.isGliding(player)){
								if(!wasGliding){
									Minecraft.getInstance().getSoundManager().play(new FastGlideSound(player));
									wasGliding = true;
								}
							}

							if(ServerFlightHandler.isGliding(player) || (ax != 0 || az != 0)){
								motion = player.getDeltaMovement().add(0.0D, g * (-1.0D + (double)f3 * 0.75D), 0.0D);

								if(motion.y < 0.0D && d9 > 0.0D){
									double d3 = motion.y * -0.1D * (double)f3;
									motion = motion.add(lookVec.x * d3 / d9, d3, lookVec.z * d3 / d9);
								}

								if(f6 < 0.0F && d9 > 0.0D){
									double d13 = d11 * -Mth.sin(f6) * 0.04D;
									motion = motion.add(-lookVec.x * d13 / d9, d13 * 3.2D, -lookVec.z * d13 / d9);
								}

								if(d9 > 0.0D){
									motion = motion.add((lookVec.x / d9 * d11 - motion.x) * 0.1D, 0.0D, (lookVec.z / d9 * d11 - motion.z) * 0.1D);
								}
								double lookY = lookVec.y;
								double yaw = Math.toRadians(player.yHeadRot + 90);
								if(lookY < 0){
									ax += Math.cos(yaw) / 500;
									az += Math.sin(yaw) / 500;
								}else{
									ax *= 0.99;
									az *= 0.99;
									ay = lookVec.y / 8;
								}
								double speedLimit = ServerConfig.maxFlightSpeed;
								ax = Mth.clamp(ax, -0.2 * speedLimit, 0.2 * speedLimit);
								az = Mth.clamp(az, -0.2 * speedLimit, 0.2 * speedLimit);

								if(ServerFlightHandler.isSpin(player)){
									ax += (Math.cos(yaw) / 500) * 100;
									az += (Math.sin(yaw) / 500) * 100;
									ay = lookVec.y / 8;
								}

								if(ServerFlightHandler.isGliding(player)){
									if(lookY < 0){
										motion = motion.add(ax, 0, az);
									}else{
										motion = motion.add(ax, ay, az);
									}
									motion = motion.multiply(0.99F, 0.98F, 0.99F);

									if(motion.length() != player.getDeltaMovement().length()){
										NetworkHandler.CHANNEL.sendToServer(new SyncFlightSpeed(player.getId(), motion));
									}

									player.setDeltaMovement(motion);
									ay = player.getDeltaMovement().y;
								}
								//end
							}

							if(!ServerFlightHandler.isGliding(player)){
								wasGliding = false;
								double maxForward = 0.5;

								Vec3 moveVector = ClientDragonRender.getInputVector(new Vec3(movement.leftImpulse, 0, movement.forwardImpulse), 1F, player.yRot);
								moveVector.multiply(1.3, 0, 1.3);

								double lookY = lookVec.y;

								boolean moving = movement.up || movement.down || movement.left || movement.right;
								double yaw = Math.toRadians(player.yHeadRot + 90);

								if(ServerFlightHandler.isSpin(player)){
									ax += (Math.cos(yaw) / 500) * 200;
									az += (Math.sin(yaw) / 500) * 200;
									ay = lookVec.y / 8;
								}

								if(moving && !movement.jumping && !movement.shiftKeyDown){
									maxForward = 0.8;
									moveVector.multiply(1.4, 0, 1.4);
									motion = new Vec3(Mth.lerp(0.1, motion.x, moveVector.x), 0, Mth.lerp(0.1, motion.z, moveVector.z));
									motion = new Vec3(Mth.clamp(motion.x, -maxForward, maxForward), 0, Mth.clamp(motion.z, -maxForward, maxForward));

									motion = motion.add(ax, ay, az);

									ax *= 0.9F;
									ay *= 0.9F;
									az *= 0.9F;

									motion = new Vec3(motion.x, -(g * 2) + motion.y, motion.z);

									if(motion.length() != player.getDeltaMovement().length()){
										NetworkHandler.CHANNEL.sendToServer(new SyncFlightSpeed(player.getId(), motion));
									}

									player.setDeltaMovement(motion);
									return;
								}

								motion = motion.multiply(0.99F, 0.98F, 0.99F);
								motion = new Vec3(Mth.lerp(0.1, motion.x, moveVector.x), 0, Mth.lerp(0.1, motion.z, moveVector.z));
								motion = new Vec3(Mth.clamp(motion.x, -maxForward, maxForward), 0, Mth.clamp(motion.z, -maxForward, maxForward));

								motion = motion.add(ax, ay, az);

								if(ServerFlightHandler.isSpin(player)){
									motion.multiply(10, 10, 10);
								}

								ax *= 0.9F;
								ay *= 0.9F;
								az *= 0.9F;

								if(movement.jumping){
									motion = new Vec3(motion.x, 0.4 + motion.y, motion.z);

									if(motion.length() != player.getDeltaMovement().length()){
										NetworkHandler.CHANNEL.sendToServer(new SyncFlightSpeed(player.getId(), motion));
									}

									player.setDeltaMovement(motion);
									return;
								}else if(movement.shiftKeyDown){
									motion = new Vec3(motion.x, -0.5 + motion.y, motion.z);
									if(motion.length() != player.getDeltaMovement().length()){
										NetworkHandler.CHANNEL.sendToServer(new SyncFlightSpeed(player.getId(), motion));
									}

									player.setDeltaMovement(motion);
									return;
								}

								if(wasFlying){ //Dont activate on a regular jump
									double yMotion = hasFood ? -g + ay : -(g * 4) + ay;
									motion = new Vec3(motion.x, yMotion, motion.z);

									if(motion.length() != player.getDeltaMovement().length()){
										NetworkHandler.CHANNEL.sendToServer(new SyncFlightSpeed(player.getId(), motion));
									}

									player.setDeltaMovement(motion);
								}
							}
						}else{
							wasGliding = false;
							wasFlying = false;
							ax = 0;
							az = 0;
							ay = 0;
						}
					}else{
						ax = 0;
						az = 0;
						ay = 0;
					}
				}
			});
		}
	}

	@SubscribeEvent
	public static void spin(InputEvent.RawMouseEvent keyInputEvent){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null){
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);
		if(!handler.isDragon()){
			return;
		}

		if(KeyInputHandler.SPIN_ABILITY.getKey().getValue() == keyInputEvent.getButton()){
			spinKeybind(player, handler);
		}
	}

	private static void spinKeybind(LocalPlayer player, DragonStateHandler handler){
		if(!ServerFlightHandler.isSpin(player) && handler.getMovementData().spinCooldown <= 0 && handler.getMovementData().spinLearned){
			if(ServerFlightHandler.isFlying(player) || ServerFlightHandler.canSwimSpin(player)){
				handler.getMovementData().spinAttack = ServerFlightHandler.spinDuration;
				handler.getMovementData().spinCooldown = ServerConfig.flightSpinCooldown * 20;
				NetworkHandler.CHANNEL.sendToServer(new SyncSpinStatus(player.getId(), handler.getMovementData().spinAttack, handler.getMovementData().spinCooldown, handler.getMovementData().spinLearned));
			}
		}
	}

	@SubscribeEvent
	public static void toggleWings(InputEvent.KeyInputEvent keyInputEvent){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null){
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);
		if(handler == null || !handler.isDragon()){
			return;
		}

		boolean currentState = handler.isWingsSpread();
		Vec3 lookVec = player.getLookAngle();

		if(KeyInputHandler.SPIN_ABILITY.getKey().getValue() == keyInputEvent.getKey()){
			spinKeybind(player, handler);
		}

		if(jumpToFly && !player.isCreative() && !player.isSpectator()){
			if(Minecraft.getInstance().options.keyJump.isDown()){
				if(keyInputEvent.getAction() == GLFW.GLFW_PRESS){
					if(handler.hasWings() && !currentState && (lookVec.y > 0.8 || !lookAtSkyForFlight)){
						if(!player.isOnGround() && !player.isInLava() && !player.isInWater()){
							if(player.getFoodData().getFoodLevel() > ServerConfig.flightHungerThreshold || player.isCreative() || ServerConfig.allowFlyingWithoutHunger){
								NetworkHandler.CHANNEL.sendToServer(new SyncFlyingStatus(player.getId(), true));
							}else{
								if(lastHungerMessage == 0 || lastHungerMessage + TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS) < System.currentTimeMillis()){
									lastHungerMessage = System.currentTimeMillis();
									player.sendMessage(new TranslatableComponent("ds.wings.nohunger"), player.getUUID());
								}
							}
						}
					}
				}
			}
		}

		if(KeyInputHandler.TOGGLE_WINGS.consumeClick()){
			if(handler.hasWings()){
				//Allows toggling the wings if food level is above 0, player is creative, wings are already enabled (allows disabling even when hungry) or if config options is turned on
				if((player.getFoodData().getFoodLevel() > ServerConfig.flightHungerThreshold || player.isCreative()) || currentState || ServerConfig.allowFlyingWithoutHunger){
					NetworkHandler.CHANNEL.sendToServer(new SyncFlyingStatus(player.getId(), !currentState));
					if(notifyWingStatus){
						if(!currentState){
							player.sendMessage(new TranslatableComponent("ds.wings.enabled"), player.getUUID());
						}else{
							player.sendMessage(new TranslatableComponent("ds.wings.disabled"), player.getUUID());
						}
					}
				}else{
					player.sendMessage(new TranslatableComponent("ds.wings.nohunger"), player.getUUID());
				}
			}else{
				player.sendMessage(new TranslatableComponent("ds.you.have.no.wings"), player.getUUID());
			}
		}
	}
}