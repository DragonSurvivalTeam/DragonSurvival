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
import net.minecraft.world.phys.shapes.BooleanOp;
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

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

    public static EntityDimensions calculateDimensions(final DragonStateHandler handler, @Nullable final Player player, @Nullable final Pose overridePose) {
        double scale = player != null ? player.getAttributeValue(Attributes.SCALE) : 1;
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

    /**
     * Modified version of {@link Entity#fudgePositionAfterSizeChange(EntityDimensions)} </br>
     * Note that the smooth growth may cause this to be called very often, usually related to: </br>
     * - Growth command / Anything that sets a new stage </br>
     * - High natural growth value </br>
     * - Growth item with a high value </br>
     */
    public static void fudgePositionAfterSizeChange(final Entity entity, final EntityDimensions currentDimension, final EntityDimensions newDimensions) {
        if (entity.noPhysics || ((EntityAccessor) entity).dragonSurvival$isFirstTick()) {
            // Reasonable checks that vanilla is also doing
            return;
        }

        float newWidth = newDimensions.width();
        float newHeight = newDimensions.height();

        if (currentDimension.width() > newWidth && currentDimension.height() > newHeight) {
            return;
        }

        if (entity.level().noBlockCollision(entity, newDimensions.makeBoundingBox(entity.position()))) {
            // Do a minimal check to see if the player is phasing into any blocks
            // It doesn't seem to have a big impact when actual collision happens and skips unneeded shape calculations
            return;
        }

        double yOffset = newHeight / 2;
        AABB expandedBounds = createPlayerBounds(entity, currentDimension, newDimensions, yOffset);
        // We expand the shape of each collision with a smaller height since we usually don't need to adjust the position on the y level
        // The height has a big impact on the performance
        Vec3 newPosition = findNewPosition(entity, expandedBounds, createCollisionShape(entity, expandedBounds, newWidth, newHeight), yOffset);

        if (newPosition != null) {
            // Technically the position should never be null
            // Since it should at least be the original position
            entity.setPos(newPosition.add(0, -yOffset, 0));
        }
    }

    private static Vec3 findNewPosition(final Entity entity, final AABB boundingBox, final VoxelShape collisionShape, final double yOffset) {
        // Don't bother optimizing, since the shape is just discarded anyway
        return Shapes.joinUnoptimized(Shapes.create(boundingBox), collisionShape, BooleanOp.ONLY_FIRST)
                .closestPointTo(entity.position().add(0, yOffset, 0))
                .orElse(null);
    }

    public static AABB createPlayerBounds(final Entity entity, final EntityDimensions currentDimensions, final EntityDimensions newDimensions, final double yOffset) {
        double widthDifference = newDimensions.width() - currentDimensions.width() + Shapes.BIG_EPSILON;
        double heightDifference = newDimensions.height() - currentDimensions.height() + Shapes.BIG_EPSILON;

        AABB boundingBox = AABB.ofSize(entity.position().add(0, yOffset, 0), widthDifference, heightDifference, widthDifference);
        // Inflate adds the sizes fully to min and max, meaning it doubles the size of the actual shape
        // This is used to catch more blocks and therefore collect more positions that the player can be moved to
        return boundingBox.inflate(newDimensions.width(), newDimensions.height(), newDimensions.width());
    }

    public static VoxelShape createCollisionShape(final Entity entity, final AABB boundingBox, double widthExpansion, double heightExpansion) {
        double width = widthExpansion / 2;
        double height = heightExpansion / 2;

        return StreamSupport.stream(entity.level().getBlockCollisions(entity, boundingBox).spliterator(), false)
                .filter(shape -> entity.level().getWorldBorder().isWithinBounds(shape.bounds()))
                .flatMap(shape -> shape.toAabbs().stream())
                // Increase the area in which it can find suitable positions
                .map(aabb -> aabb.inflate(width, height, width))
                .map(Shapes::create)
                // Make sure we don't add any unnecessary optimization calls
                .reduce(Shapes.empty(), (first,second) -> Shapes.joinUnoptimized(first, second, BooleanOp.OR));
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