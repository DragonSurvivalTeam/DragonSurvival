package by.dragonsurvivalteam.dragonsurvival.common.entity.goals;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FearData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber
public class DynamicAvoidEntityGoal extends Goal {
    // For performance reasons
    private static final int MAX_DISTANCE = 64 * 64;

    private final PathfinderMob mob;
    private final TargetingConditions avoidCondition;
    private final PathNavigation navigation;

    private Path path;

    private Player toAvoid;
    private float walkSpeed;
    private float sprintSpeed;

    public DynamicAvoidEntityGoal(final PathfinderMob mob) {
        this.mob = mob;
        this.avoidCondition = TargetingConditions.forCombat();
        this.navigation = mob.getNavigation();
    }

    @SubscribeEvent
    public static void attachGoal(final EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof PathfinderMob mob) {
            mob.goalSelector.addGoal(5, new DynamicAvoidEntityGoal(mob));
        }
    }

    @Override
    public boolean canUse() {
        selectPlayer();

        if (toAvoid == null) {
            return false;
        }

        Vec3 fleePosition = DefaultRandomPos.getPosAway(mob, 16, 7, toAvoid.position());

        if (fleePosition == null) {
            return false;
        }

        if (toAvoid.distanceToSqr(fleePosition) < toAvoid.distanceToSqr(mob)) {
            // New position is closer to the player than the current one
            return false;
        }

        path = navigation.createPath(BlockPos.containing(fleePosition), 0);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (navigation.isDone()) {
            return false;
        }

        if (toAvoid != null) {
            return toAvoid.getExistingData(DSDataAttachments.FEAR).map(fear -> fear.getData(mob).distance() != FearData.NO_FEAR).orElse(false);
        }

        return true;
    }

    @Override
    public void start() {
        navigation.moveTo(path, walkSpeed);
    }

    @Override
    public void stop() {
        toAvoid = null;
    }

    @Override
    public void tick() {
        if (mob.distanceToSqr(toAvoid) < 49) {
            mob.getNavigation().setSpeedModifier(sprintSpeed);
        } else {
            mob.getNavigation().setSpeedModifier(walkSpeed);
        }
    }

    private void selectPlayer() {
        double closestDistanceTo = Integer.MAX_VALUE;

        for (Player player : mob.level().players()) {
            FearData fear = player.getExistingData(DSDataAttachments.FEAR).orElse(null);

            if (fear == null) {
                continue;
            }

            double distanceTo = player.position().distanceToSqr(mob.position());

            if (distanceTo > closestDistanceTo || distanceTo > MAX_DISTANCE) {
                continue;
            }

            FearData.Data data = fear.getData(mob);

            if (data.distance() == FearData.NO_FEAR) {
                continue;
            }

            if (avoidCondition.range(data.distance()).test(mob, player)) {
                toAvoid = player;
                walkSpeed = data.walkSpeed();
                sprintSpeed = data.sprintSpeed();
            }
        }
    }
}
