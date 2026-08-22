package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ClimbingHandler {
    public static boolean canClimb(final LivingEntity entity, final ClimbableData data) {
        // 'LivingEntity#travel' already checks this for y increment
        // But 'LivingEntity#handleRelativeFrictionAndCalculateMovement' also increments when jumping
        // Which would mean if "all" blocks are climbable, you'd be able to float in the air with no nearby wall
        if (!entity.horizontalCollision) {
            return false;
        }

        if (entity instanceof Player) {
            // Need to handle direction player faces, since xxa / zza is consistent based on the input
            // (pressing left, right, etc. always sets the same value)
            // Meaning player faces north -> left is west / player faces east -> left is north
            Direction facing = entity.getDirection();
            Direction inputDirection;

            if (Math.signum(entity.xxa) != 0) {
                inputDirection = entity.xxa > 0
                        ? facing.getCounterClockWise()
                        : facing.getClockWise();

                if (checkBlock(entity, inputDirection, data)) {
                    return true;
                }
            }

            if (Math.signum(entity.zza) != 0) {
                inputDirection = entity.zza > 0
                        ? facing
                        : facing.getOpposite();

                if (checkBlock(entity, inputDirection, data)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean checkBlock(final LivingEntity entity, final Direction direction, final ClimbableData data) {
        if (!(entity.level() instanceof WorldGenLevel level)) {
            return false;
        }

        BlockPos climbingPosition = entity.blockPosition().relative(direction);
        boolean canClimb = data.canClimb(level, climbingPosition);

        if (canClimb) {
            data.climbPosition = climbingPosition;
        }

        return canClimb;
    }
}
