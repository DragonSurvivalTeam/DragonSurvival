package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class DragonSizeHandler {
    // TODO :: Add timestamp and clear cache
    private static final ConcurrentHashMap<String, Boolean> WAS_DRAGON = new ConcurrentHashMap<>(20);
    private static final ConcurrentHashMap<String, Double> LAST_SIZE = new ConcurrentHashMap<>(20);

    @SubscribeEvent
    public static void getDragonSize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        Pose overridePose = overridePose(player);
        EntityDimensions newDimensions = calculateDimensions(handler, player, overridePose);
        event.setNewSize(new EntityDimensions(newDimensions.width(), newDimensions.height(), newDimensions.eyeHeight(), event.getOldSize().attachments(), event.getOldSize().fixed()));
    }

    public static double calculateDragonHeight(DragonStateHandler handler, Player player) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double height = calculateRawDragonHeight(handler.getSize()) * handler.getBody().value().heightMultiplier();
        return applyPose(height * scale, overridePose(player), handler.getBody().value().hasExtendedCrouch());
    }

    public static double calculateDragonEyeHeight(DragonStateHandler handler, Player player) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double eyeHeight = calculateRawDragonEyeHeight(handler.getSize()) * handler.getBody().value().heightMultiplier();
        return applyPose(eyeHeight * scale, overridePose(player), handler.getBody().value().hasExtendedCrouch());
    }

    public static EntityDimensions calculateDimensions(DragonStateHandler handler, Player player, Pose overridePose) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double height = calculateRawDragonHeight(handler.getSize()) * handler.getBody().value().heightMultiplier();
        double eyeHeight = calculateRawDragonEyeHeight(handler.getSize()) * handler.getBody().value().heightMultiplier();
        double width = calculateRawDragonWidth(handler.getSize());

        height = applyPose(height, overridePose, handler.getBody().value().hasExtendedCrouch());
        eyeHeight = applyPose(eyeHeight, overridePose, handler.getBody().value().hasExtendedCrouch());

        return EntityDimensions.scalable((float) (width * scale), (float) (height * scale)).withEyeHeight((float) (eyeHeight * scale));
    }

    public static double calculateRawDragonHeight(double size) {
        return (size + 4.0D) / 20.0D;
    }

    public static double calculateRawDragonWidth(double size) {
        return (3.0D * size + 62.0D) / 260.0D; // 0.4 -> Config Dragon Max;
    }

    public static double calculateRawDragonEyeHeight(double size) {
        return (11.0D * size + 54.0D) / 260.0D; // 0.8 -> Config Dragon Max
    }

    // TODO :: what exactly do these numbers mean?
    public static double applyPose(double height, Pose pose, boolean hasExtendedCrouch) {
        if (pose == Pose.CROUCHING) {
            height *= (hasExtendedCrouch ? 3d / 6d : 5d / 6d);
        } else if (pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK) {
            height *= 7.0D / 12.0D;
        }
        return height;
    }

    public static Pose overridePose(final Player player) {
        if (player == null) {
            return Pose.STANDING;
        }

        Pose overridePose = getOverridePose(player);

        if (player.getForcedPose() != overridePose) {
            player.setForcedPose(overridePose);

            if (player.level().isClientSide() && Minecraft.getInstance().getCameraEntity() != player) {
                player.refreshDimensions();
            }
        }

        return overridePose;
    }

    public static Pose getOverridePose(Player player) {
        if (player != null) {
            boolean swimming = (player.isInWaterOrBubble() || SwimData.getData(player).canSwimIn(player.getMaxHeightFluidType())) && player.isSprinting() && !player.isPassenger();
            boolean flying = ServerFlightHandler.isFlying(player);
            boolean spinning = player.isAutoSpinAttack();
            boolean crouching = player.isShiftKeyDown();
            if (flying && !player.isSleeping()) {
                return Pose.FALL_FLYING;
            } else if (swimming || (player.isInWaterOrBubble() || player.isInLava()) && !canPoseFit(player, Pose.STANDING) && canPoseFit(player, Pose.SWIMMING)) {
                return Pose.SWIMMING;
            } else if (spinning) {
                return Pose.SPIN_ATTACK;
            } else if (crouching || !canPoseFit(player, Pose.STANDING) && canPoseFit(player, Pose.CROUCHING)) {
                return Pose.CROUCHING;
            }
        }
        return Pose.STANDING;
    }

    public static boolean canPoseFit(Player player, Pose pose) {
        return player.level().noCollision(calculateDimensions(DragonStateProvider.getData(player), player, pose).makeBoundingBox(player.position()));
    }

    @SubscribeEvent
    public static void playerTick(final PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        // In cases where client and server runs on the same machine
        // Only using the player id results in one side not refreshing the dimensions
        String playerIdSide = player.getId() + (player.level().isClientSide() ? "client" : "server");

        DragonStateProvider.getOptional(player).ifPresent(handler -> {
            if (handler.isDragon()) {
                overridePose(player);

                if (!WAS_DRAGON.getOrDefault(playerIdSide, false)) {
                    player.refreshDimensions();
                    WAS_DRAGON.put(playerIdSide, true);
                } else if (LAST_SIZE.getOrDefault(playerIdSide, 20.0) != handler.getSize()) {
                    player.refreshDimensions();
                    LAST_SIZE.put(playerIdSide, handler.getSize());
                }
            } else if (WAS_DRAGON.getOrDefault(playerIdSide, false)) {
                player.setForcedPose(null);
                player.refreshDimensions();
                WAS_DRAGON.put(playerIdSide, false);
            }
        });
    }
}