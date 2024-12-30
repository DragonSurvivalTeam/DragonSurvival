package by.dragonsurvivalteam.dragonsurvival.common.entity.goals;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class FollowSummonerGoal extends Goal {
    private final Mob mob;
    private final PathNavigation navigation;
    private final double speedModifier;
    private final float stopDistance;
    private final float startDistance;

    @Nullable private Entity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowSummonerGoal(final Mob mob, double speedModifier, float startDistance, float stopDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.navigation = mob.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        if (!(mob.getNavigation() instanceof GroundPathNavigation) && !(mob.getNavigation() instanceof FlyingPathNavigation)) {
            Functions.logOrThrow("Unsupported mob type for [" + getClass().getSimpleName() + "]");
        }
    }

    @Override
    public boolean canUse() {
        Entity owner = mob.getData(DSDataAttachments.ENTITY_HANDLER).getSummonOwner(mob.level());

        if (shouldFollow(owner, startDistance)) {
            this.owner = owner;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (navigation.isDone()) {
            return false;
        }

        return shouldFollow(owner, stopDistance);
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = mob.getPathfindingMalus(PathType.WATER);
        mob.setPathfindingMalus(PathType.WATER, 0);
    }

    @Override
    public void stop() {
        owner = null;
        navigation.stop();
        mob.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        //noinspection DataFlowIssue -> owner should not be null
        boolean shouldTeleport = mob.distanceToSqr(owner) >= TamableAnimal.TELEPORT_WHEN_DISTANCE_IS_SQ * 2;

        if (!shouldTeleport) {
            mob.getLookControl().setLookAt(owner, 10, mob.getMaxHeadXRot());
        }

        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = adjustedTickDelay(10);

            if (shouldTeleport) {
                teleportToOwner();
            } else {
                navigation.moveTo(owner, speedModifier);
            }
        }
    }

    private boolean shouldFollow(@Nullable final Entity owner, float distance) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        if (mob.distanceToSqr(owner) < distance * distance) {
            return false;
        } else {
            return owner.getData(DSDataAttachments.SUMMONED_ENTITIES).getMovementBehaviour() == SummonedEntities.MovementBehaviour.FOLLOW;
        }
    }

    /** Copied from {@link TamableAnimal#teleportToAroundBlockPos(BlockPos)} */
    private void teleportToOwner() {
        //noinspection DataFlowIssue -> owner should not be null
        BlockPos position = owner.blockPosition();

        for (int i = 0; i < 10; i++) {
            int xOffset = mob.getRandom().nextIntBetweenInclusive(-3, 3);
            int zOffset = mob.getRandom().nextIntBetweenInclusive(-3, 3);

            if (Math.abs(xOffset) >= 2 || Math.abs(zOffset) >= 2) {
                int yOffset = mob.getRandom().nextIntBetweenInclusive(-1, 1);

                if (maybeTeleportTo(position.getX() + xOffset, position.getY() + yOffset, position.getZ() + zOffset)) {
                    return;
                }
            }
        }
    }

    /** Copied from {@link TamableAnimal#maybeTeleportTo(int, int, int)} */
    private boolean maybeTeleportTo(int x, int y, int z) {
        if (!canTeleportTo(BlockPos.containing(x, y, z))) {
            return false;
        } else {
            mob.moveTo(x, y, z, mob.getYRot(), mob.getXRot());
            mob.getNavigation().stop();
            return true;
        }
    }

    /** Copied from {@link TamableAnimal#canTeleportTo(BlockPos)} */
    private boolean canTeleportTo(final BlockPos position) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(mob, position);

        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            BlockPos blockpos = position.subtract(mob.blockPosition());
            return mob.level().noCollision(mob, mob.getBoundingBox().move(blockpos));
        }
    }
}
