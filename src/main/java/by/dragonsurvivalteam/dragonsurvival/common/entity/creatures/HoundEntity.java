package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.RandomAnimationPicker;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSpecificMobGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.WindupMeleeAttackGoal;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class HoundEntity extends Hunter {
    private RawAnimation currentIdleAnim;
    private boolean isIdleAnimSet = false;

    public static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(HoundEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DID_SLOWDOWN_ATTACK = SynchedEntityData.defineId(HoundEntity.class, EntityDataSerializers.BOOLEAN);

    public HoundEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new WindupMeleeAttackGoal(this, 1, 8));
        this.goalSelector.addGoal(8, new FollowSpecificMobGoal(this, 0.6, 10, 20, target -> target instanceof KnightEntity));
    }

    @Override
    public int getCurrentSwingDuration() {
        return 8;
    }

    public double getRunThreshold() {
        return 0.15;
    }

    public double getWalkThreshold() {
        return 0.01;
    }

    @Override
    protected void defineSynchedData(@NotNull final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIETY, 0);
        builder.define(DID_SLOWDOWN_ATTACK, false);
    }

    public int getVariety() {
        return entityData.get(VARIETY);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundNBT) {
        super.addAdditionalSaveData(compoundNBT);
        compoundNBT.putInt("Variety", entityData.get(VARIETY));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundNBT) {
        super.readAdditionalSaveData(compoundNBT);
        entityData.set(VARIETY, compoundNBT.getInt("Variety"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor pLevel, @NotNull DifficultyInstance pDifficulty, @NotNull MobSpawnType pSpawnType, @Nullable SpawnGroupData pSpawnGroupData) {
        entityData.set(VARIETY, random.nextInt(8));
        return super.finalizeSpawn(pLevel, pDifficulty, pSpawnType, pSpawnGroupData);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "everything", 3, this::fullPredicate));
    }

    private boolean isNotIdle() {
        double movement = AnimationUtils.getMovementSpeed(this);
        return swingTime > 0 || movement > getWalkThreshold() || isInWater();
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        if (ServerConfig.houndSlowdownChance != 0 && entity instanceof LivingEntity) {
            if (random.nextDouble() > ServerConfig.houndSlowdownChance) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
                entityData.set(DID_SLOWDOWN_ATTACK, true);
            } else {
                entityData.set(DID_SLOWDOWN_ATTACK, false);
            }
        }
        return super.doHurtTarget(entity);
    }

    public PlayState fullPredicate(AnimationState<HoundEntity> state) {
        double movement = AnimationUtils.getMovementSpeed(this);

        if (isIdleAnimSet) {
            isIdleAnimSet = !isNotIdle();
        }

        if (swingTime > 0) {
            if (entityData.get(DID_SLOWDOWN_ATTACK)) {
                return state.setAndContinue(SPECIAL_ATTACK);
            } else {
                return state.setAndContinue(ATTACK);
            }
        } else if (isInWater()) {
            return state.setAndContinue(SWIM);
        } else if (movement > getRunThreshold()) {
            return state.setAndContinue(RUN);
        } else if (movement > getWalkThreshold()) {
            return state.setAndContinue(WALK);
        } else {
            return state.setAndContinue(getIdleAnim());
        }
    }

    public RawAnimation getIdleAnim() {
        if (!isIdleAnimSet) {
            currentIdleAnim = IDLE_ANIMS.pickRandomAnimation();
            isIdleAnimSet = true;
        }
        return currentIdleAnim;
    }

    private static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("attack");
    private static final RawAnimation SPECIAL_ATTACK = RawAnimation.begin().thenLoop("special_attack");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");

    private static final RandomAnimationPicker IDLE_ANIMS = new RandomAnimationPicker(
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle1"), 97),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle2"), 3)
    );
}
