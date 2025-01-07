package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSpecificMobGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.WindupMeleeAttackGoal;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

public class GriffinEntity extends Hunter {
    private static final EntityDataAccessor<Integer> CURRENT_ATTACK = SynchedEntityData.defineId(GriffinEntity.class, EntityDataSerializers.INT);

    private enum GriffinAttackTypes {
        NONE,
        NORMAL,
        BLINDNESS,
        SLASH_WINGS
    }

    public GriffinEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new WindupMeleeAttackGoal(this, 1.0, 15));
        this.goalSelector.addGoal(8, new FollowSpecificMobGoal(this, 0.6, 5, 20, target -> target instanceof AmbusherEntity));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        return flyingpathnavigation;
    }

    @Override
    public void travel(@NotNull Vec3 pTravelVector) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                this.moveRelative(this.getSpeed(), pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
            }
        }
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity pEntity) {
        return this.getBoundingBox().inflate(ServerConfig.griffinRange).intersects(pEntity.getHitbox());
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(CURRENT_ATTACK, 0);
    }

    private void setCurrentAttack(GriffinAttackTypes attackType) {
        this.entityData.set(CURRENT_ATTACK, attackType.ordinal());
    }

    private GriffinAttackTypes getCurrentAttack() {
        return GriffinAttackTypes.values()[this.entityData.get(CURRENT_ATTACK)];
    }

    @Override
    public int getCurrentSwingDuration() {
        return 30;
    }

    @Override
    public void swing(@NotNull InteractionHand pHand) {
        super.swing(pHand);
        if (this.swinging) {
            double randomRoll = random.nextDouble();
            if (randomRoll > 0.75) {
                setCurrentAttack(GriffinAttackTypes.SLASH_WINGS);
            } else if (randomRoll > 0.5) {
                setCurrentAttack(GriffinAttackTypes.BLINDNESS);
            } else {
                setCurrentAttack(GriffinAttackTypes.NORMAL);
            }
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        if (entity instanceof LivingEntity target) {
            if (getCurrentAttack() == GriffinAttackTypes.BLINDNESS) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            } else if (getCurrentAttack() == GriffinAttackTypes.SLASH_WINGS) {
                if (DragonStateProvider.isDragon(target)) {
                    target.addEffect(new MobEffectInstance(DSEffects.BROKEN_WINGS, 100, 0));
                }
            }
        }
        return super.doHurtTarget(entity);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "everything", 0, this::fullPredicate));
    }

    public PlayState fullPredicate(final AnimationState<GriffinEntity> state) {
        double movement = AnimationUtils.getMovementSpeed(this);

        if (swingTime > 0) {
            return state.setAndContinue(getAttackAnim());
        } else {
            if (movement > 0.01) {
                if (isAggro()) {
                    return state.setAndContinue(FLY_AGGRESSIVE);
                } else {
                    return state.setAndContinue(FLY);
                }
            } else {
                if (!onGround()) {
                    return state.setAndContinue(IDLE_FLY);
                } else {
                    return state.setAndContinue(IDLE);
                }
            }
        }
    }

    private RawAnimation getAttackAnim() {
        switch (getCurrentAttack()) {
            case NORMAL -> {
                return ATTACK;
            }
            case BLINDNESS -> {
                return SPECIAL_ATTACK1;
            }
            case SLASH_WINGS -> {
                return SPECIAL_ATTACK2;
            }

            default -> throw new IllegalStateException("Tried to get attack animation with an invalid attack!");
        }
    }


    private static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("fly_attack");
    private static final RawAnimation SPECIAL_ATTACK1 = RawAnimation.begin().thenLoop("special_attack1");
    private static final RawAnimation SPECIAL_ATTACK2 = RawAnimation.begin().thenLoop("special_attack2");

    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation FLY_AGGRESSIVE = RawAnimation.begin().thenLoop("fly_agressive");

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE_FLY = RawAnimation.begin().thenLoop("idle_fly");

}
