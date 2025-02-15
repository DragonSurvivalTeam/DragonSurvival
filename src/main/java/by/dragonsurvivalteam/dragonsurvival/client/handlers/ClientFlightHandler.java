package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.FastGlideSound;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SpinDurationAndCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.flight.ToggleFlight;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.ActionWithTimedCooldown;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.TickedCooldown;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.Objects;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

/** Used in pair with {@link ServerFlightHandler} */
@EventBusSubscriber(Dist.CLIENT)
public class ClientFlightHandler {
    @Translation(comments = "You have §cno levitation skill§r. Use a §cPrimordial Anchor§r in The End to learn this skill.")
    private static final String FLIGHT_EFFECT_DISABLED = Translation.Type.GUI.wrap("message.flight_effect_disabled");

    @Translation(comments = "Your species does not have the ability to fly")
    private static final String NO_FLIGHT_EFFECT = Translation.Type.GUI.wrap("message.no_flight_effect");

    @Translation(comments = "Your wings are blocked or broken")
    private static final String WINGS_BLOCKED = Translation.Type.GUI.wrap("message.wings_blocked");

    @Translation(key = "jump_to_fly", type = Translation.Type.CONFIGURATION, comments = "If enabled flight will be activated when jumping in the air")
    @ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "jump_to_fly")
    public static Boolean jumpToFly = false;

    @Translation(key = "look_at_sky_for_flight", type = Translation.Type.CONFIGURATION, comments = "If enabled together with [jump_to_fly] you will be required to look at the sky to start flying")
    @ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "lookAtSkyForFlight")
    public static Boolean lookAtSkyForFlight = false;

    @Translation(key = "flight_zoom_effect", type = Translation.Type.CONFIGURATION, comments = "Enable / Disable a zoom effect while gliding as a dragon")
    @ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "flight_zoom_effect")
    public static Boolean flightZoomEffect = true;

    @Translation(key = "flight_camera_movement", type = Translation.Type.CONFIGURATION, comments = "Enable / Disable camera movement while gliding as a dragon")
    @ConfigOption(side = ConfigSide.CLIENT, category = "flight", key = "flight_camera_movement")
    public static Boolean flightCameraMovement = true;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "spin_cooldown_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset to the x position of the spin cooldown indicator")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spin_cooldown_x_offset")
    public static Integer spinCooldownXOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "spin_cooldown_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset to the y position of the spin cooldown indicator")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spin_cooldown_y_offset")
    public static Integer spinCooldownYOffset = 0;

    @ConfigRange(min = 0.0f, max = 32.0f)
    @Translation(key = "base_dragon_camera_offset", type = Translation.Type.CONFIGURATION, comments = "Base offset for the dragon's third person camera (is multiplied and scaled by the other factors)")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "base_dragon_camera_offset")
    public static Float baseDragonCameraOffset = 16.0f;

    @ConfigRange(min = 0.0f, max = 32.0f)
    @Translation(key = "flat_dragon_camera_offset", type = Translation.Type.CONFIGURATION, comments = "Flat offset for the dragon's third person camera (is added after all other factors are combined)")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "flat_dragon_camera_offset")
    public static Float flatDragonCameraOffset = 1.0f;

    @ConfigRange(min = 0.0f, max = 1.0f)
    @Translation(key = "dragon_camera_minimum_scale", type = Translation.Type.CONFIGURATION, comments = "The scale at which the dragon's third person camera will stop zooming in")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "dragon_camera_minimum_scale")
    public static Float dragonCameraMinimumScale = 0.5f;

    @ConfigRange(min = 0.0f, max = 2.0f)
    @Translation(key = "dragon_camera_scale_factor", type = Translation.Type.CONFIGURATION, comments = "How much scale impacts the third person camera distance")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "dragon_camera_scale_factor")
    public static Float dragonCameraScaleFactor = 0.15f;

    @Translation(key = "disable_size_camera_modifications", type = Translation.Type.CONFIGURATION, comments = "Disable all size-based camera modifications from DS")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "disable_camera_modifications")
    public static Boolean disableSizeCameraModifications = false;

    private static final ResourceLocation SPIN_COOLDOWN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/spin_cooldown.png");

    private static final ActionWithTimedCooldown HUNGER_MESSAGE_WITH_COOLDOWN = new ActionWithTimedCooldown(30_000, () -> {
        Player localPlayer = DragonSurvival.PROXY.getLocalPlayer();
        if (localPlayer == null) return;
        localPlayer.sendSystemMessage(Component.translatable(LangKey.MESSAGE_NO_HUNGER));
    });

    public static int lastSync;
    public static boolean wasGliding;
    public static boolean wasFlying;

    /** Acceleration */
    static double ax, ay, az;
    static float lastIncrease;
    static float lastZoom = 1f;

    // These are the drag values from vanilla Elytra flying. See isFallFlying() section in LivingEntity#travel()
    private static final Vec3 ELYTRA_FLY_DRAG = new Vec3(0.99, 0.98, 0.99);

    // 7 ticks is the value used to trigger creative flight (in LocalPlayer#aiStep()).
    // This ignores checking the ground, letting the dragon start flying even in tight spaces.
    private static final TickedCooldown jumpFlyCooldown = new TickedCooldown(7);
    private static boolean lastJumpInputState; // We need to track the rising edge manually

    @SubscribeEvent
    public static void flightCamera(CalculateDetachedCameraDistanceEvent event) {

        DragonStateProvider.getOptional(DragonSurvival.PROXY.getLocalPlayer()).ifPresent(handler -> {
            if (handler.isDragon()) {
                float visualScale = (float)handler.getVisualScale(DragonSurvival.PROXY.getLocalPlayer(), Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
                if(disableSizeCameraModifications) {
                    event.setDistance((event.getDistance()) / event.getEntityScalingFactor() * visualScale);
                } else {
                    event.setDistance((event.getDistance() + baseDragonCameraOffset) / event.getEntityScalingFactor() * Math.max(visualScale, dragonCameraMinimumScale) * dragonCameraScaleFactor + flatDragonCameraOffset);
                }
            }
        });
    }

    @SubscribeEvent
    public static void flightCamera(ViewportEvent.ComputeCameraAngles setup) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer currentPlayer = minecraft.player;
        Camera info = setup.getCamera();

        if (currentPlayer != null && currentPlayer.isAddedToLevel() && DragonStateProvider.isDragon(currentPlayer)) {
            GameRenderer gameRenderer = minecraft.gameRenderer;

            if (ServerFlightHandler.isGliding(currentPlayer)) {
                if (setup.getCamera().isDetached()) {
                    if (flightCameraMovement) {
                        Vec3 lookVec = currentPlayer.getLookAngle();
                        float increase = (float) Mth.clamp(lookVec.y * 10, 0, lookVec.y * 5);
                        float gradualIncrease = Mth.lerp(0.25f, lastIncrease, increase);
                        info.move(0, gradualIncrease, 0);
                        lastIncrease = gradualIncrease;
                    }
                }

                if (minecraft.player != null) {
                    if (flightZoomEffect) {
                        if (!minecraft.options.getCameraType().isFirstPerson()) {
                            Vec3 lookVec = currentPlayer.getLookAngle();
                            float f = Math.min(Math.max(0.5F, 1F - (float) (lookVec.y * 5 / 2.5 * 0.5)), 3F);
                            float newZoom = Mth.lerp(0.25f, lastZoom, f);
                            gameRenderer.zoom = newZoom;
                            lastZoom = newZoom;
                        }
                    }
                }
            } else {
                if (lastIncrease > 0) {
                    if (flightCameraMovement) {
                        lastIncrease = Mth.lerp(0.25f, lastIncrease, 0);
                        info.move(0, lastIncrease, 0);
                    }
                }

                if (lastZoom != 1) {
                    if (flightZoomEffect) {
                        lastZoom = Mth.lerp(0.25f, lastZoom, 1f);
                        gameRenderer.zoom = lastZoom;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderFlightCooldown(RenderGuiLayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || player.isSpectator()) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        if (!ServerFlightHandler.isFlying(player) && !ServerFlightHandler.canSwimSpin(player)) {
            return;
        }

        FlightData spin = FlightData.getData(player);
        if (spin.hasSpin && spin.cooldown > 0) {
            if (event.getName() == VanillaGuiLayers.AIR_LEVEL) {
                Window window = Minecraft.getInstance().getWindow();

                int cooldown = ServerFlightHandler.flightSpinCooldown * 20;
                float cooldownProgress = ((float) cooldown - (float) spin.cooldown) / (float) cooldown;

                int x = window.getGuiScaledWidth() / 2 - 66 / 2;
                int y = window.getGuiScaledHeight() - 96;

                x += spinCooldownXOffset;
                y += spinCooldownYOffset;

                int width = (int) (cooldownProgress * 62);
                event.getGuiGraphics().blit(SPIN_COOLDOWN, x, y, 0, 0, 66, 21, 256, 256);
                event.getGuiGraphics().blit(SPIN_COOLDOWN, x + 4, y + 1, 4, 21, width, 21, 256, 256);
            }
        }
    }

    @SubscribeEvent
    public static void flightParticles(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        FlightData spin = FlightData.getData(player);

        if (!DragonStateProvider.isDragon(player) || spin.duration <= 0) {
            return;
        }

        if (ServerFlightHandler.canSwimSpin(player) && ServerFlightHandler.isSpin(player)) {
            spawnSpinParticle(player, player.isInWater() ? ParticleTypes.BUBBLE_COLUMN_UP : ParticleTypes.LAVA);
        }

        if (EnchantmentUtils.getLevel(player, Enchantments.FIRE_ASPECT) > 0) {
            spawnSpinParticle(player, ParticleTypes.LAVA);
        } else if (EnchantmentUtils.getLevel(player, Enchantments.KNOCKBACK) > 0) {
            spawnSpinParticle(player, ParticleTypes.EXPLOSION);
        } else if (EnchantmentUtils.getLevel(player, Enchantments.SWEEPING_EDGE) > 0) {
            spawnSpinParticle(player, ParticleTypes.SWEEP_ATTACK);
        } else if (EnchantmentUtils.getLevel(player, Enchantments.SHARPNESS) > 0) {
            spawnSpinParticle(player, new DustParticleOptions(new Vector3f(1f, 1f, 1f), 1f));
        } else if (EnchantmentUtils.getLevel(player, Enchantments.SMITE) > 0) {
            spawnSpinParticle(player, ParticleTypes.ENCHANT);
        } else if (EnchantmentUtils.getLevel(player, Enchantments.BANE_OF_ARTHROPODS) > 0) {
            spawnSpinParticle(player, ParticleTypes.DRIPPING_OBSIDIAN_TEAR);
        }
    }

    /** Controls acceleration */
    @SubscribeEvent
    public static void flightControl(final ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player != null && !player.isPassenger() && !Minecraft.getInstance().isPaused()) {
            if (!player.hasEffect(MobEffects.LEVITATION)) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> {
                    if (handler.isDragon()) {
                        Double flightSpeedMultiplier = player.getAttributeValue(DSAttributes.FLIGHT_SPEED);

                        Vec3 viewVector = player.getLookAngle();
                        double yaw = Math.toRadians(player.getYHeadRot() + 90);

                        // Only apply while in water (not while flying)
                        if (ServerFlightHandler.canSwimSpin(player) && ServerFlightHandler.isSpin(player)) {
                            Vec3 deltaMovement = player.getDeltaMovement();

                            double maxFlightSpeed = ServerFlightHandler.maxFlightSpeed;
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

                        if (FlightData.getData(player).isWingsSpread()) {
                            Input movement = player.input;

                            if (!hasEnoughFoodToStartFlight(player) || player.isCreative()) {
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
                                double gravity = player.getAttributeValue(Attributes.GRAVITY);

                                if (ServerFlightHandler.isGliding(player)) {
                                    if (!wasGliding) {
                                        Minecraft.getInstance().getSoundManager().play(new FastGlideSound(player));
                                        wasGliding = true;
                                    }
                                }

                                if (ServerFlightHandler.isGliding(player) || ax != 0 || az != 0) {
                                    deltaMovement = player.getDeltaMovement().add(0.0D, gravity * (-1.0D + (double) verticalDelta * 0.75D), 0.0D);

                                    if (deltaMovement.y < 0 && horizontalView > 0) {
                                        double downwardMomentum = deltaMovement.y * -0.1D * (double) verticalDelta * flightSpeedMultiplier;
                                        deltaMovement = deltaMovement.add(viewVector.x * downwardMomentum / horizontalView, downwardMomentum, viewVector.z * downwardMomentum / horizontalView);
                                    }

                                    if (pitch < 0 && horizontalView > 0) {
                                        // Handle movement when the player makes turns
                                        double delta = horizontalMovement * -Mth.sin(pitch) * 0.04D * flightSpeedMultiplier;
                                        deltaMovement = deltaMovement.add(-viewVector.x * delta / horizontalView, delta * 3.2D, -viewVector.z * delta / horizontalView);
                                    }

                                    if (horizontalView > 0) {
                                        deltaMovement = deltaMovement.add((viewVector.x * flightSpeedMultiplier / horizontalView * horizontalMovement - deltaMovement.x) * 0.1D, 0.0D, (viewVector.z * flightSpeedMultiplier / horizontalView * horizontalMovement - deltaMovement.z) * 0.1D);
                                    }

                                    // Increase speed while flying down or height when flying up
                                    if (viewVector.y < 0) {
                                        ax += (Math.cos(yaw) * flightSpeedMultiplier * 2) / 500;
                                        az += (Math.sin(yaw) * flightSpeedMultiplier * 2) / 500;
                                    } else {
                                        ay = viewVector.y / 4;
                                        ax *= 0.98;
                                        az *= 0.98;
                                    }

                                    double speedLimit = ServerFlightHandler.maxFlightSpeed * flightSpeedMultiplier;
                                    ax = Mth.clamp(ax, -0.4 * speedLimit, 0.4 * speedLimit);
                                    az = Mth.clamp(az, -0.4 * speedLimit, 0.4 * speedLimit);

                                    if (ServerFlightHandler.isSpin(player)) {
                                        ax += (Math.cos(yaw) * flightSpeedMultiplier * 100 * 2) / 500;
                                        az += (Math.sin(yaw) * flightSpeedMultiplier * 100 * 2) / 500;
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

                                        deltaMovement = deltaMovement.multiply(ELYTRA_FLY_DRAG);

                                        player.setDeltaMovement(deltaMovement);
                                        ay = player.getDeltaMovement().y;
                                    }
                                }

                                if (!ServerFlightHandler.isGliding(player)) {
                                    wasGliding = false;
                                    double maxForward = 0.5 * flightSpeedMultiplier * 2;

                                    Vec3 moveVector = getInputVector(new Vec3(movement.leftImpulse, 0, movement.forwardImpulse), 1F, player.getYRot());
                                    moveVector.multiply(1.3 * flightSpeedMultiplier * 2, 0, 1.3 * flightSpeedMultiplier * 2);

                                    boolean moving = movement.up || movement.down || movement.left || movement.right;

                                    if (ServerFlightHandler.isSpin(player)) {
                                        ax += (Math.cos(yaw) * flightSpeedMultiplier * 200 * 2) / 500;
                                        az += (Math.sin(yaw) * flightSpeedMultiplier * 200 * 2) / 500;
                                        ay = viewVector.y / 8;
                                    }

                                    if (ServerFlightHandler.stableHover && !movement.jumping && !movement.shiftKeyDown && !ServerFlightHandler.isSpin(player) && !ServerFlightHandler.isGliding(player)) {
                                        ay = Math.max(ay, gravity * 1.1);
                                    }

                                    if (moving && !movement.jumping && !movement.shiftKeyDown) {
                                        maxForward = 0.8 * flightSpeedMultiplier * 2;
                                        moveVector.multiply(1.4 * flightSpeedMultiplier * 2, 0, 1.4 * flightSpeedMultiplier * 2);
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
                                            double yMotion = hasEnoughFoodToStartFlight(player) ? -gravity + ay : -(gravity * 4) + ay;
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

    @SubscribeEvent
    public static void toggleWings(final InputEvent.Key event) {
        Pair<Player, DragonStateHandler> data = KeyHandler.checkAndGet(event, Keybind.TOGGLE_FLIGHT, true);

        if (data == null) {
            return;
        }

        PacketDistributor.sendToServer(new ToggleFlight(ToggleFlight.Activation.MANUAL, ToggleFlight.Result.NONE));
    }

    /**
     * Checks the conditions for jumping to start flight, and tries to enable wings if successful. <br>
     * Handles error messages.
     */
    private static void tryJumpToFly(final Player player) {
        if (!jumpToFly) {
            return;
        }

        // This only handles the requirements to trigger jump-to-fly. Other conditions are handled by enableWings()
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (lookAtSkyForFlight && player.getLookAngle().y() <= 0.8) {
            return;
        }

        if (player.isInLava() || player.isInWater()) {
            return;
        }

        PacketDistributor.sendToServer(new ToggleFlight(ToggleFlight.Activation.JUMP, ToggleFlight.Result.NONE));
    }

    public static void handleToggleResult(final ToggleFlight.Activation activation, final ToggleFlight.Result result) {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        FlightData flight = FlightData.getData(player);

        if (activation == ToggleFlight.Activation.MANUAL) {
            switch (result) {
                case NO_WINGS -> player.sendSystemMessage(Component.translatable(NO_FLIGHT_EFFECT));
                case ALREADY_DISABLED -> player.sendSystemMessage(Component.translatable(FLIGHT_EFFECT_DISABLED));
                case WINGS_BLOCKED -> player.sendSystemMessage(Component.translatable(WINGS_BLOCKED));
                case NO_HUNGER -> player.sendSystemMessage(Component.translatable(LangKey.MESSAGE_NO_HUNGER));
            }
        } else if (result == ToggleFlight.Result.NO_HUNGER) {
            HUNGER_MESSAGE_WITH_COOLDOWN.tryRun();
        }

        switch (result) {
            case WINGS_BLOCKED, NO_WINGS, NO_HUNGER, SUCCESS_DISABLED, ALREADY_DISABLED -> flight.areWingsSpread = false;
            case SUCCESS_ENABLED, ALREADY_ENABLED -> flight.areWingsSpread = true;
        }
    }

    @SubscribeEvent
    public static void triggerSpin(final InputEvent.Key event) {
        Pair<Player, DragonStateHandler> data = KeyHandler.checkAndGet(event, Keybind.SPIN_ABILITY, true);

        if (data == null) {
            return;
        }

        doSpin(data.getFirst());
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        jumpFlyCooldown.tick();
        boolean isJumping = Minecraft.getInstance().options.keyJump.isDown();

        if (isJumping && !lastJumpInputState && /* Check for double jump */ !jumpFlyCooldown.trySet()) {
            tryJumpToFly(player);
        }

        lastJumpInputState = isJumping;
    }

    private static void doSpin(final Player player) {
        if (ServerFlightHandler.isSpin(player)) {
            return;
        }

        FlightData spin = FlightData.getData(player);

        if (!spin.hasSpin || spin.cooldown > 0) {
            return;
        }

        if (ServerFlightHandler.isFlying(player) || ServerFlightHandler.canSwimSpin(player)) {
            spin.duration = ServerFlightHandler.SPIN_DURATION;
            spin.cooldown = Functions.secondsToTicks(ServerFlightHandler.flightSpinCooldown);
            PacketDistributor.sendToServer(new SpinDurationAndCooldown(player.getId(), spin.duration, spin.cooldown));
        }
    }

    private static void spawnSpinParticle(Player player, ParticleOptions particleData) {
        for (int i = 0; i < 20; i++) {
            double d0 = (player.getRandom().nextFloat() - 0.5) * 2;
            double d1 = (player.getRandom().nextFloat() - 0.5) * 2;
            double d2 = (player.getRandom().nextFloat() - 0.5) * 2;

            double posX = player.position().x + player.getDeltaMovement().x + d0;
            double posY = player.position().y - 1.5 + player.getDeltaMovement().y + d1;
            double posZ = player.position().z + player.getDeltaMovement().z + d2;
            player.level().addParticle(particleData, posX, posY, posZ, player.getDeltaMovement().x * -1, player.getDeltaMovement().y * -1, player.getDeltaMovement().z * -1);
        }
    }

    public static boolean hasEnoughFoodToStartFlight(Player player) {
        return player.getFoodData().getFoodLevel() > ServerFlightHandler.flightHungerThreshold;
    }

    public static Vec3 getInputVector(final Vec3 movement, float frictionSpeed, float yRot) {
        double movementStrength = movement.lengthSqr();

        if (movementStrength < Shapes.EPSILON) {
            return Vec3.ZERO;
        } else {
            Vec3 vector3d = (movementStrength > 1 ? movement.normalize() : movement).scale(frictionSpeed);
            float f = Mth.sin(yRot * ((float) Math.PI / 180F));
            float f1 = Mth.cos(yRot * ((float) Math.PI / 180F));
            return new Vec3(vector3d.x * (double) f1 - vector3d.z * (double) f, vector3d.y, vector3d.z * (double) f1 + vector3d.x * (double) f);
        }
    }
}
