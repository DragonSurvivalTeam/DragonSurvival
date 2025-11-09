package by.dragonsurvivalteam.dragonsurvival.common.entity.goals;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** This goal should only be attached to the entity if it is an ally since that flag isn't checked within this goal */
public class SummonerTargetedGoal extends TargetGoal {
    private static final int SEARCH_RADIUS = 16 * 2;

    private final Mob mob;
    private int lastTick;

    public SummonerTargetedGoal(final Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        SummonData data = mob.getData(DSDataAttachments.SUMMON);
        LivingEntity owner = data.getOwner(mob.level());

        if (owner == null || data.attackBehaviour != SummonedEntities.AttackBehaviour.DEFENSIVE) {
            return false;
        }

        if (mob.tickCount - lastTick < 20) {
            // To minimize how often this area check happens
            return false;
        }

        lastTick = mob.tickCount;

        List<LivingEntity> targets = mob.level().getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT
                .selector(target -> target instanceof Mob mob && mob.getTarget() == owner), mob, AABB.ofSize(owner.position(), SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS));

        for (LivingEntity entity : targets) {
            // Cannot check this as part of the selector above, since that will run into a 'StackOverflowError'
            // This check is needed though because it also checks whether the mob can even reach the target
            if (canAttack(entity, TargetingConditions.DEFAULT)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void stop() {
        super.stop();
        lastTick = 0;
    }

    @Override
    public void start() {
        SummonData data = mob.getData(DSDataAttachments.SUMMON);
        LivingEntity owner = data.getOwner(mob.level());

        if (owner == null) {
            stop();
            return;
        }

        // Target the closest entity which is targeting the summoner
        List<LivingEntity> targets = mob.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat()
                .selector(entity -> entity instanceof Mob mob && mob.getTarget() == owner), mob, AABB.ofSize(owner.position(), SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS));
        targets.sort(Comparator.comparingDouble(entity -> entity.distanceTo(owner)));

        for (LivingEntity target : targets) {
            if (canAttack(target, TargetingConditions.DEFAULT)) {
                mob.setTarget(target);
                super.start();
                return;
            }
        }

        stop();
    }
}
