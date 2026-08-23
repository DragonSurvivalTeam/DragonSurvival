package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.network.magic.ClimbCheck;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncClimbFlag;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
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
    public static boolean canClimb(final LivingEntity entity, final ClimbableData data) {
        if (entity.level() instanceof WorldGenLevel level) {
            SyncClimbFlag.ClimbingType previous = data.climbingType;

            boolean canClimb = handleServer(entity, data, level);

            if (previous != data.climbingType) {
                PacketDistributor.sendToPlayersTrackingEntity(entity, new SyncClimbFlag(entity.getId(), data.climbingType));
            }

            return canClimb;
        }

        return handleClient(entity, data);
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
            data.climbingType = SyncClimbFlag.ClimbingType.NONE;
            return false;
        }

        BlockPos ceilingPosition = null;
        BlockPos wallPosition = null;

        for (BlockPos position : data.trackedClimbPositions) {
            if (!data.canClimb(level, position, entity)) {
                continue;
            }

            if (position.getY() >= entity.getBoundingBox().getMaxPosition().y()) {
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
            data.isCeilingClimbing = ceilingPosition != null;
            data.climbingType = data.isCeilingClimbing ? SyncClimbFlag.ClimbingType.CEILING : SyncClimbFlag.ClimbingType.WALL;
            return true;
        }

        data.climbingType = SyncClimbFlag.ClimbingType.NONE;
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
            climbablePositions.add(BlockPos.containing(entity.getX(), entity.getBoundingBox().getMaxPosition().y() + 0.01, entity.getZ()));
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

            if (position.getY() >= entity.getBoundingBox().getMaxPosition().y()) {
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
}
