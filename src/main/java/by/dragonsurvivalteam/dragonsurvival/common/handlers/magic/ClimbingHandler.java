package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.network.magic.ClimbCheck;
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
            if (data.trackedClimbPositions == null) {
                return false;
            }

            for (BlockPos position : data.trackedClimbPositions) {
                if (data.canClimb(level, position)) {
                    data.climbPosition = position;
                    return true;
                }
            }

            return false;
        }

        Direction facing = entity.getDirection();
        Set<BlockPos> climbablePositions = new HashSet<>();

        // The server does not store this collision check nor the xxa / zza values, making this a client-only check
        if (entity.horizontalCollision) {
            if (Math.signum(entity.xxa) != 0) {
                Direction inputDirection = entity.xxa > 0
                        ? facing.getCounterClockWise()
                        : facing.getClockWise();

                climbablePositions.add(entity.blockPosition().relative(inputDirection));
            }

            if (Math.signum(entity.zza) != 0) {
                Direction inputDirection = entity.zza > 0
                        ? facing
                        : facing.getOpposite();

                climbablePositions.add(entity.blockPosition().relative(inputDirection));
            }
        }

        if (climbablePositions.isEmpty() && !entity.onGround()) {
            BlockPos base = entity.blockPosition();

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                climbablePositions.add(base.relative(direction));
            }
        }

        if (!climbablePositions.equals(Objects.requireNonNullElse(data.trackedClimbPositions, Set.of()))) {
            data.trackedClimbPositions = climbablePositions.isEmpty() ? null : climbablePositions;
            PacketDistributor.sendToServer(new ClimbCheck(climbablePositions));
        }

        if (climbablePositions.isEmpty()) {
            return false;
        }

        for (BlockPos position : climbablePositions) {
            if (data.isApprovedClimbPosition(position)) {
                data.climbPosition = position;
                return true;
            }
        }

        return false;
    }

    /** Checks whether the supplied positions are climbable */
    public static Set<BlockPos> filterPositions(@Nullable final ClimbableData data, final WorldGenLevel level, @Unmodifiable final Collection<BlockPos> positions) {
        if (data == null || data.isEmpty() || positions.isEmpty()) {
            return Set.of();
        }

        Set<BlockPos> climbablePositions = new HashSet<>(positions);
        climbablePositions.removeIf(position -> !data.canClimb(level, position));

        return climbablePositions;
    }
}
