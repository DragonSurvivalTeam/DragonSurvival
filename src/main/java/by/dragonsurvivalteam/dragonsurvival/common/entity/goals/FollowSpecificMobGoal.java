package by.dragonsurvivalteam.dragonsurvival.common.entity.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;

import java.util.function.Predicate;

public class FollowSpecificMobGoal extends FollowMobGoal {
    /** Constructs a goal allowing a mob to follow others with a predicate. The mob must have Ground or Flying navigation. */
    public FollowSpecificMobGoal(Mob pMob, double pSpeedModifier, float pStopDistance, float pAreaSize, Predicate<Mob> followPredicate) {
        super(pMob, pSpeedModifier, pStopDistance, pAreaSize);
        this.followPredicate = followPredicate;
    }
}
