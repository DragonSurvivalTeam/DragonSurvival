package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class DragonSizeHandler {
    private static final ConcurrentHashMap<String, Boolean> WAS_DRAGON = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void initializeSizeOnJoin(final EntityJoinLevelEvent event) {
        // There is no entity context when de-serializing the data
        // Therefor we set the size again, causing a refresh of the dimension
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DragonStateHandler data = DragonStateProvider.getData(serverPlayer);

            if (data.isDragon()) {
                data.setDesiredSize(serverPlayer, data.getSize());
            }
        }
    }

    @SubscribeEvent
    public static void getDragonSize(final EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.previousPose == null) {
            handler.previousPose = overridePose(player);
        }

        EntityDimensions newDimensions = calculateDimensions(handler, player, handler.previousPose);
        event.setNewSize(new EntityDimensions(newDimensions.width(), newDimensions.height(), newDimensions.eyeHeight(), event.getOldSize().attachments(), event.getOldSize().fixed()));
    }

    public static double calculateDragonHeight(final DragonStateHandler handler, final Player player) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double height = calculateRawDragonHeight(handler.getSize()) * handler.body().value().heightMultiplier();
        return applyPose(height * scale, overridePose(player), handler.body().value().hasExtendedCrouch());
    }

    public static double calculateDragonEyeHeight(final DragonStateHandler handler, final Player player) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double eyeHeight = calculateRawDragonEyeHeight(handler.getSize()) * handler.body().value().heightMultiplier();
        return applyPose(eyeHeight * scale, overridePose(player), handler.body().value().hasExtendedCrouch());
    }

    public static EntityDimensions calculateDimensions(final DragonStateHandler handler, final Player player, @Nullable final Pose overridePose) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double height = calculateRawDragonHeight(handler.getSize()) * handler.body().value().heightMultiplier();
        double eyeHeight = calculateRawDragonEyeHeight(handler.getSize()) * handler.body().value().heightMultiplier();
        double width = calculateRawDragonWidth(handler.getSize());

        height = applyPose(height, overridePose, handler.body().value().hasExtendedCrouch());
        eyeHeight = applyPose(eyeHeight, overridePose, handler.body().value().hasExtendedCrouch());

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

    public static double applyPose(double height, @Nullable final Pose pose, boolean hasExtendedCrouch) {
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
        }

        DragonStateHandler data = DragonStateProvider.getData(player);
        data.previousPose = overridePose;
        return overridePose;
    }

    public static Pose getOverridePose(final Player player) {
        if (player == null) {
            return Pose.STANDING;
        }

        Pose pose;

        if (ServerFlightHandler.isFlying(player) && !player.isSleeping()) {
            pose = Pose.FALL_FLYING;
        } else if (SwimData.getData(player).canSwimIn(player.getMaxHeightFluidType()) && player.isSprinting() && !player.isPassenger()) {
            pose = Pose.SWIMMING;
        } else if (player.isAutoSpinAttack()) {
            pose = Pose.SPIN_ATTACK;
        } else if (player.isShiftKeyDown()) {
            pose = Pose.CROUCHING;
        } else {
            pose = Pose.STANDING;
        }

        if (player.isSpectator() || player.isPassenger() || canPoseFit(player, pose)) {
            return pose;
        } else if (canPoseFit(player, Pose.CROUCHING)) {
            return Pose.CROUCHING;
        }

        return Pose.STANDING;
    }

    public static boolean canPoseFit(final Player player, @Nullable final Pose pose) {
        return player.level().noCollision(calculateDimensions(DragonStateProvider.getData(player), player, pose).makeBoundingBox(player.position()).deflate(Shapes.EPSILON));
    }

    @SubscribeEvent
    public static void handleLerpSize(final PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        DragonStateHandler data = DragonStateProvider.getData(player);

        boolean isDragon = data.isDragon();
        Boolean wasDragon = WAS_DRAGON.put(getKey(player), isDragon);

        if (wasDragon != null && wasDragon && !isDragon) {
            player.setForcedPose(null);
            player.refreshDimensions();
        } else if (isDragon) {
            data.lerpSize(player);
            if(player.level().isClientSide()) {
                // We need to do this special handling for the client so that the pose update looks smooth for the client, without updating using poses that are actually incorrect
                // when doing the pose/refreshSize calculations on the server
                DragonSizeHandler.overridePose(player);
                player.refreshDimensions();
            }
        }
    }

    @SubscribeEvent
    public static void removeMapEntry(final EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            WAS_DRAGON.remove(getKey(player));
        }
    }

    private static String getKey(final Player player) {
        return player.getId() + (player.level().isClientSide() ? "client" : "server");
    }
}