package by.dragonsurvivalteam.dragonsurvival.common.entity.goals;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/** This goal should only be attached to the entity if it is an ally since that flag isn't checked within this goal */
public class SummonerHurtByTargetGoal extends TargetGoal {
    private final Mob mob;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public SummonerHurtByTargetGoal(final Mob mob) {
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

        ownerLastHurtBy = owner.getLastHurtByMob();
        int timestamp = owner.getLastHurtByMobTimestamp();

        if (ownerLastHurtBy == null || this.timestamp == timestamp) {
            return false;
        }

        return canAttack(ownerLastHurtBy, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        mob.setTarget(ownerLastHurtBy);
        LivingEntity owner = mob.getData(DSDataAttachments.SUMMON).getOwner(mob.level());

        if (owner != null) {
            timestamp = owner.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
