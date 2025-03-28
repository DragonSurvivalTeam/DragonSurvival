package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.mixins.EntityAccessor;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class DragonSizeHandler {
    private static final ConcurrentHashMap<String, Boolean> WAS_DRAGON = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void initializeGrowthOnJoin(final EntityJoinLevelEvent event) {
        // There is no entity context when de-serializing the data
        // Therefor we set the growth again, causing a refresh of the dimension
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DragonStateHandler data = DragonStateProvider.getData(serverPlayer);

            if (data.isDragon()) {
                data.setGrowth(serverPlayer, data.getGrowth(), true);
            }
        }
    }

    // This needs to fire as early as possible, since it is the "baseline" for the size of the player
    // Other mods might throw out this baseline or modify it further, but when the player is a dragon
    // that should be the initial size to work with
    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

    public static double calculateDragonEyeHeight(final DragonStateHandler handler, final Player player) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double eyeHeight = handler.body().value().scalingProportions().eyeHeight();
        return applyPose(eyeHeight * scale, overridePose(player), handler.body().value().crouchHeightRatio());
    }

    public static EntityDimensions calculateDimensions(final DragonStateHandler handler, final Player player, @Nullable final Pose overridePose) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        double height = handler.body().value().scalingProportions().height();
        double eyeHeight = handler.body().value().scalingProportions().eyeHeight();
        double width = handler.body().value().scalingProportions().width();

        height = applyPose(height, overridePose, handler.body().value().crouchHeightRatio());
        eyeHeight = applyPose(eyeHeight, overridePose, handler.body().value().crouchHeightRatio());

        return EntityDimensions.scalable((float) (width * scale), (float) (height * scale)).withEyeHeight((float) (eyeHeight * scale));
    }

    public static double applyPose(double height, @Nullable final Pose pose, double crouchHeightRatio) {
        if (pose == Pose.CROUCHING || pose == Pose.FALL_FLYING || pose == Pose.SWIMMING || pose == Pose.SPIN_ATTACK) {
            height *= crouchHeightRatio;
        }

        return height;
    }

    public static Pose overridePose(final Player player) {
        Pose overridePose = getOverridePose(player);
        if (player == null) {
            return overridePose;
        }

        if (Compat.hasModelSwap(player)) {
            player.setForcedPose(null);
            return overridePose;
        }

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

        if ((player.getAbilities().flying || ServerFlightHandler.isFlying(player)) && !player.isSleeping()) {
            pose = Pose.FALL_FLYING;
        } else if (DragonEntity.isConsideredSwimmingForAnimation(player) && player.isSprinting()) {
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

    /** Modified version of {@link Entity#fudgePositionAfterSizeChange(EntityDimensions)} */
    public static void fudgePositionAfterSizeChange(final Entity entity, final EntityDimensions newDimensions) {
        EntityDimensions oldDimensions = ((EntityAccessor) entity).dragonSurvival$getDimensions();
        double widthChange = Math.max(0, oldDimensions.width() - newDimensions.width()) + Shapes.BIG_EPSILON;
        double heightChange = Math.max(0, oldDimensions.height() - newDimensions.height()) + Shapes.BIG_EPSILON;

        Vec3 halfHeightPosition = entity.position().add(0, newDimensions.height() / 2, 0);
        VoxelShape fullHeightVoxelShape = Shapes.create(AABB.ofSize(halfHeightPosition, widthChange, heightChange, widthChange));
        Optional<Vec3> fullHeightFreePosition = entity.level().findFreePosition(entity, fullHeightVoxelShape, halfHeightPosition, oldDimensions.width(), oldDimensions.height(), oldDimensions.width());

        if (fullHeightFreePosition.isPresent()) {
            entity.setPos(fullHeightFreePosition.get().add(0, -oldDimensions.height() / 2, 0));
        } else {
            VoxelShape deltaHeightVoxelShape = Shapes.create(AABB.ofSize(halfHeightPosition, widthChange, Shapes.BIG_EPSILON, widthChange));

            entity.level().findFreePosition(entity, deltaHeightVoxelShape, halfHeightPosition, oldDimensions.width(), newDimensions.height(), oldDimensions.width()).ifPresent(freePosition -> {
                entity.setPos(freePosition.add(0, (double) (-newDimensions.height()) / 20 + Shapes.BIG_EPSILON, 0));
            });
        }
    }

    public static boolean canPoseFit(final Player player, @Nullable final Pose pose) {
        return player.level().noCollision(calculateDimensions(DragonStateProvider.getData(player), player, pose).makeBoundingBox(player.position()).deflate(Shapes.EPSILON));
    }

    @SubscribeEvent
    public static void handleLerpGrowthAndPose(final PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        DragonStateHandler data = DragonStateProvider.getData(player);

        boolean isDragon = data.isDragon(); // TODO :: remove and handle it when reverted to human
        Boolean wasDragon = WAS_DRAGON.put(getKey(player), isDragon);

        if (wasDragon != null && wasDragon && !isDragon) {
            player.setForcedPose(null);
            player.refreshDimensions();
        } else if (isDragon) {
            data.lerpGrowth(player);

            // Required for smooth transitions of the pose (e.g. sneaking)
            DragonSizeHandler.overridePose(event.getEntity());
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