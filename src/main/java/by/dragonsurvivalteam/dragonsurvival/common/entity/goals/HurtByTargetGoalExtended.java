package by.dragonsurvivalteam.dragonsurvival.common.entity.goals;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class HurtByTargetGoalExtended extends HurtByTargetGoal {
    @Nullable private Class<? extends Mob>[] toHeedAlert;

    public HurtByTargetGoalExtended(final PathfinderMob mob, final Class<?>... ignoreDamagedFrom) {
        super(mob, ignoreDamagedFrom);
    }

    @SafeVarargs
    public final HurtByTargetGoal setHeeders(final Class<? extends Mob>... reinforcementTypes) {
        this.toHeedAlert = reinforcementTypes;
        return this;
    }

    @Override
    protected void alertOthers() {
        if (this.toHeedAlert != null) {
            for (Class<? extends Mob> heedType : this.toHeedAlert) {
                alertOthers(heedType);
            }
        }
    }

    protected void alertOthers(final Class<? extends Mob> type) {
        double distance = getFollowDistance();
        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(distance, 10, distance);

        mob.level().getEntitiesOfClass(type, aabb, EntitySelector.NO_SPECTATORS).forEach(otherMob -> {
            if (otherMob == mob || otherMob.getTarget() != null) {
                return;
            }

            LivingEntity lastHurtBy = mob.getLastHurtByMob();

            if (lastHurtBy == null || otherMob.isAlliedTo(lastHurtBy)) {
                return;
            }

            if (toIgnoreAlert != null) {
                for (Class<?> toIgnore : toIgnoreAlert) {
                    if (otherMob.getClass() == toIgnore) {
                        return;
                    }
                }
            }

            alertOther(otherMob, lastHurtBy);
        });
    }
}
