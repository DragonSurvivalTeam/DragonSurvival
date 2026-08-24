package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.network.magic.ClimbCheck;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncClimbFlag;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.util.CeilingClimbDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ParametersAreNonnullByDefault
public class ClimbingHandler {
    /** To differentiate between the actual ceiling and a wall position that is above the entity */
    private static final double CEILING_TOLERANCE = 0.1;

    public static boolean canClimb(final LivingEntity entity, final ClimbableData data) {
        if (entity.level() instanceof WorldGenLevel level) {
            SyncClimbFlag.ClimbingType previous = data.getClimbingType();
            boolean canClimb = handleServer(entity, data, level);

            if (previous != data.getClimbingType()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SyncClimbFlag(entity.getId(), data.getClimbingType()));
                entity.refreshDimensions();
            }

            return canClimb;
        }

        return handleClient(entity, data);
    }

    public static boolean canDescend(final LivingEntity entity, final ClimbableData data) {
        if (!entity.isShiftKeyDown()) {
            return false;
        }

        if (!data.isCeilingClimbing()) {
            return true;
        }

        for (int offset = 1; offset <= Math.ceil(CeilingClimbDimensions.getUnmodifiedHeight(entity)); offset++) {
            //noinspection DataFlowIssue -> 'climgPosition' is not null at this point
            if (entity.level().getBlockState(data.climbPosition.below(offset)).blocksMotion()) {
                return false;
            }
        }

        return true;
    }

    /** Checks whether the supplied positions are climbable */
    public static Set<BlockPos> filterPositions(@Nullable final ClimbableData data, final WorldGenLevel level, final LivingEntity entity, @Unmodifiable final Collection<BlockPos> positions) {
        if (data == null || data.isEmpty() || positions.isEmpty()) {
            return Set.of();
        }

        Set<BlockPos> climbablePositions = new HashSet<>(positions);
        climbablePositions.removeIf(position -> !data.canClimb(level, position, entity));

        return climbablePositions;
    }

    private static boolean handleServer(final LivingEntity entity, final ClimbableData data, final WorldGenLevel level) {
        if (data.trackedClimbPositions == null) {
            data.setClimbingType(SyncClimbFlag.ClimbingType.NONE);
            return false;
        }

        BlockPos ceilingPosition = null;
        BlockPos wallPosition = null;

        for (BlockPos position : data.trackedClimbPositions) {
            if (!data.canClimb(level, position, entity)) {
                continue;
            }

            if (isCeilingPosition(entity, position)) {
                ceilingPosition = position;
                break;
            } else if (wallPosition == null) {
                wallPosition = position;
            }
        }

        // Prioritize ceiling so a transition from wall-climbing to ceiling-climbing is possible
        BlockPos climbPosition = ceilingPosition != null ? ceilingPosition : wallPosition;

        if (climbPosition != null) {
            data.climbPosition = climbPosition;

            if (ceilingPosition != null) {
                data.setClimbingType(SyncClimbFlag.ClimbingType.CEILING);
            } else {
                data.setClimbingType(SyncClimbFlag.ClimbingType.WALL);
            }

            return true;
        }

        data.setClimbingType(SyncClimbFlag.ClimbingType.NONE);
        return false;
    }

    /** Server does not store the relevant variables */
    private static boolean handleClient(final LivingEntity entity, final ClimbableData data) {
        Direction facing = entity.getDirection();
        Set<BlockPos> climbablePositions = new HashSet<>();

        // Allows for a better transition back to wall-climbing
        boolean attemptedWallClimb = false;

        if (entity.horizontalCollision) {
            if (Math.signum(entity.xxa) != 0) {
                Direction inputDirection = entity.xxa > 0
                        ? facing.getCounterClockWise()
                        : facing.getClockWise();

                BlockPos position = entity.blockPosition().relative(inputDirection);
                climbablePositions.add(position);

                if (data.isApprovedClimbPosition(position)) {
                    attemptedWallClimb = true;
                }
            }

            if (Math.signum(entity.zza) != 0) {
                Direction inputDirection = entity.zza > 0
                        ? facing
                        : facing.getOpposite();

                BlockPos position = entity.blockPosition().relative(inputDirection);
                climbablePositions.add(position);

                if (data.isApprovedClimbPosition(position)) {
                    attemptedWallClimb = true;
                }
            }
        }

        // Handles the sticking to the wall part / sliding down
        // Since in those cases there will be no active collision / xxa or zza change
        if (climbablePositions.isEmpty() && !entity.onGround()) {
            BlockPos base = entity.blockPosition();

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                climbablePositions.add(base.relative(direction));
            }
        }

        if (!attemptedWallClimb && data.canClimbCeilings() && !entity.onGround()) {
            climbablePositions.add(BlockPos.containing(entity.getX(), entity.getBoundingBox().getMaxPosition().y() + CEILING_TOLERANCE, entity.getZ()));
        }

        if (!climbablePositions.equals(Objects.requireNonNullElse(data.trackedClimbPositions, Set.of()))) {
            data.trackedClimbPositions = climbablePositions.isEmpty() ? null : climbablePositions;
            PacketDistributor.sendToServer(new ClimbCheck(climbablePositions));
        }

        if (climbablePositions.isEmpty()) {
            return false;
        }

        BlockPos ceilingPosition = null;
        BlockPos wallPosition = null;

        for (BlockPos position : climbablePositions) {
            if (!data.isApprovedClimbPosition(position)) {
                continue;
            }

            if (isCeilingPosition(entity, position)) {
                ceilingPosition = position;
                break;
            } else if (wallPosition == null) {
                wallPosition = position;
            }
        }

        // Prioritize ceiling so a transition from wall-climbing to ceiling-climbing is possible
        BlockPos climbingPosition = ceilingPosition != null ? ceilingPosition : wallPosition;

        if (climbingPosition != null) {
            data.climbPosition = climbingPosition;
            data.isCeilingClimbing = ceilingPosition != null;
            return true;
        }

        return false;
    }

    /** The ceiling is only relevant while it is in contact with the top of the bounding box */
    private static boolean isCeilingPosition(final LivingEntity entity, final BlockPos position) {
        return Math.abs(position.getY() - entity.getBoundingBox().getMaxPosition().y()) <= CEILING_TOLERANCE;
    }
}
