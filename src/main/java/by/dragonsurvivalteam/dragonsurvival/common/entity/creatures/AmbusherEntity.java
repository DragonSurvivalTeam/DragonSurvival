package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTickTimer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.RandomAnimationPicker;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.SpawningUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

public class AmbusherEntity extends Hunter implements RangedAttackMob {

    private boolean isRandomIdleAnimSet = false;
    private boolean hasPlayedReleaseAnimation = false;
    private boolean hasPlayedReinforcementsAnimation = false;
    private boolean isFirstClientTick = true;
    private float nextArrowVelocity = 0.0f;
    private RawAnimation currentIdleAnim;
    private final AnimationTickTimer ambusherTickTimer = new AnimationTickTimer();

    private static final EntityDataAccessor<Boolean> HAS_RELEASED_GRIFFIN = SynchedEntityData.defineId(AmbusherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> NEARBY_DRAGON_PLAYER = SynchedEntityData.defineId(AmbusherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_CALLED_REINFORCEMENTS = SynchedEntityData.defineId(AmbusherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_SUMMONED_REINFORCEMENTS = SynchedEntityData.defineId(AmbusherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RANGED_ATTACK_TIMER = SynchedEntityData.defineId(AmbusherEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AMBUSH_HORN_AND_RELOAD_TIMER = SynchedEntityData.defineId(AmbusherEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GRIFFIN_RELEASE_RELOAD_TIMER = SynchedEntityData.defineId(AmbusherEntity.class, EntityDataSerializers.INT);

    public AmbusherEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1, ServerConfig.ambusherAttackInterval, 5.f) {
            @Override
            public boolean canUse() {
                // Don't go after the player whilst calling reinforcements
                return super.canUse() && !(hasCalledReinforcements() && !hasSummonedReinforcements());
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8, 1.0f));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (isFirstClientTick) {
                // Sync up with the server's data to prevent animations from playing that shouldn't when the entity is loaded
                hasPlayedReleaseAnimation = hasReleasedGriffin();
                hasPlayedReinforcementsAnimation = hasCalledReinforcements();
                isFirstClientTick = false;
            }

            return;
        }

        if (isAggro() && !hasReleasedGriffin()) {
            setHasReleasedGriffin(true);
            summonGriffin();
            LivingEntity target = getTarget();

            if (target != null && target.hasEffect(DSEffects.HUNTER_OMEN)) {
                beginSummonReinforcements();
            } else {
                beginGriffinReleaseReloadTimer();
            }
        }

        if (getRangedAttackTimer() == CROSSBOW_ATTACK_START_TIME) {
            fireArrow();
        }
        if (getRangedAttackTimer() == CROSSBOW_RELOAD_CHARGE_SOUND_TIME) {
            this.playSound(SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), 1.0F, 1.0F);
        }

        if (getRangedAttackTimer() == CROSSBOW_RELOAD_ARROW_PLACE_SOUND_TIME) {
            this.playSound(SoundEvents.CROSSBOW_LOADING_END.value(), 1.0F, 1.0F);
        }

        if (getRangedAttackTimer() < ServerConfig.ambusherAttackInterval && getRangedAttackTimer() >= 0) {
            setRangedAttackTimer(getRangedAttackTimer() + 1);
        } else {
            setRangedAttackTimer(-1);
        }

        if (getAmbushHornTimer() == AMBUSH_HORN_SOUND_START_TIME) {
            this.playSound(SoundEvents.GOAT_HORN_SOUND_VARIANTS.getFirst().value(), 1.0F, 1.0F);
        }

        if (getAmbushHornTimer() == AMBUSH_ARROW_PLACE_SOUND_TIME) {
            this.playSound(SoundEvents.CROSSBOW_LOADING_END.value(), 1.0F, 1.0F);
        }

        if (getAmbushHornTimer() < AMBUSH_ANIM_DURATION && getAmbushHornTimer() >= 0) {
            setAmbushHornTimer(getAmbushHornTimer() + 1);
        } else {
            if (hasCalledReinforcements() && !hasSummonedReinforcements()) {
                summonReinforcements();
            }
            setAmbushHornTimer(-1);
        }

        if (getGriffinReleaseReloadTimer() < GRIFFIN_RELEASE_ANIM_DURATION && getGriffinReleaseReloadTimer() >= 0) {
            setGriffinReleaseReloadTimer(getGriffinReleaseReloadTimer() + 1);
        } else {
            setGriffinReleaseReloadTimer(-1);
        }

        setNearbyDragonPlayer(DragonUtils.isNearbyDragonPlayerToEntity(8.0, this.level(), this));
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity pTarget, float pVelocity) {
        if (getGriffinReleaseReloadTimer() == -1 && getAmbushHornTimer() == -1) {
            setRangedAttackTimer(0);
            nextArrowVelocity = pVelocity;
        }
    }

    private void fireArrow() {
        CrossbowItem tempCrossbowitem = (CrossbowItem) Items.CROSSBOW;
        ItemStack tempCrossbowItemStack = new ItemStack(tempCrossbowitem, 1);
        CrossbowItem.tryLoadProjectiles(this, tempCrossbowItemStack);
        tempCrossbowitem.setDamage(tempCrossbowItemStack, ServerConfig.ambusherDamage);
        tempCrossbowitem.performShooting(this.level(), this, InteractionHand.MAIN_HAND, tempCrossbowItemStack, nextArrowVelocity, 1.0f, this.getTarget());
    }

    @Override
    public @NotNull ItemStack getProjectile(@NotNull ItemStack pWeaponStack) {
        return net.neoforged.neoforge.common.CommonHooks.getProjectile(this, pWeaponStack, new ItemStack(Items.ARROW, 1));
    }

    private void beginSummonReinforcements() {
        setHasCalledReinforcements(true);
        setAmbushHornTimer(0);
    }

    private void beginGriffinReleaseReloadTimer() {
        setGriffinReleaseReloadTimer(0);
    }

    private void summonReinforcements() {
        for (int i = 0; i < ServerConfig.ambusherSpearmanReinforcementCount; i++) {
            Mob mob = DSEntities.HUNTER_SPEARMAN.get().create(this.level());
            SpawningUtils.spawn(mob, this.position(), this.level(), MobSpawnType.MOB_SUMMONED, 20, 3.0f, true);
            mob.setTarget(this.getTarget());
        }

        for (int i = 0; i < ServerConfig.ambusherHoundReinforcementCount; i++) {
            Mob mob = DSEntities.HUNTER_HOUND.get().create(this.level());
            SpawningUtils.spawn(mob, this.position(), this.level(), MobSpawnType.MOB_SUMMONED, 20, 3.0f, true);
            mob.setTarget(this.getTarget());
        }

        Mob mob = DSEntities.HUNTER_KNIGHT.get().create(this.level());
        SpawningUtils.spawn(mob, this.position(), this.level(), MobSpawnType.MOB_SUMMONED, 20, 3.0f, true);
        mob.setTarget(this.getTarget());

        setHasSummonedReinforcements(true);
    }

    private void summonGriffin() {
        Mob mob = DSEntities.HUNTER_GRIFFIN.get().create(this.level());
        SpawningUtils.spawn(mob, this.position().add(0, 2, 0), this.level(), MobSpawnType.MOB_SUMMONED, 20, 3.0f, true);
        mob.setTarget(this.getTarget());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(HAS_RELEASED_GRIFFIN, false);
        pBuilder.define(NEARBY_DRAGON_PLAYER, false);
        pBuilder.define(HAS_CALLED_REINFORCEMENTS, false);
        pBuilder.define(HAS_SUMMONED_REINFORCEMENTS, false);
        pBuilder.define(RANGED_ATTACK_TIMER, -1);
        pBuilder.define(AMBUSH_HORN_AND_RELOAD_TIMER, -1);
        pBuilder.define(GRIFFIN_RELEASE_RELOAD_TIMER, -1);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundNBT) {
        super.addAdditionalSaveData(compoundNBT);
        compoundNBT.putBoolean("HasReleasedGriffin", hasReleasedGriffin());
        compoundNBT.putBoolean("HasCalledReinforcements", hasCalledReinforcements());
        compoundNBT.putBoolean("HasSummonedReinforcements", hasSummonedReinforcements());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundNBT) {
        super.readAdditionalSaveData(compoundNBT);
        setHasReleasedGriffin(compoundNBT.getBoolean("HasReleasedGriffin"));
        setHasCalledReinforcements(compoundNBT.getBoolean("HasCalledReinforcements"));
        setHasSummonedReinforcements(compoundNBT.getBoolean("HasSummonedReinforcements"));
    }

    public boolean hasReleasedGriffin() {
        return this.entityData.get(HAS_RELEASED_GRIFFIN);
    }

    public void setHasReleasedGriffin(boolean hasReleasedGriffin) {
        this.entityData.set(HAS_RELEASED_GRIFFIN, hasReleasedGriffin);
    }

    public int getRangedAttackTimer() {
        return this.entityData.get(RANGED_ATTACK_TIMER);
    }

    public void setRangedAttackTimer(int rangedAttackTimer) {
        this.entityData.set(RANGED_ATTACK_TIMER, rangedAttackTimer);
    }

    public int getAmbushHornTimer() {
        return this.entityData.get(AMBUSH_HORN_AND_RELOAD_TIMER);
    }

    public void setAmbushHornTimer(int ambushHornTimer) {
        this.entityData.set(AMBUSH_HORN_AND_RELOAD_TIMER, ambushHornTimer);
    }

    public int getGriffinReleaseReloadTimer() {
        return this.entityData.get(GRIFFIN_RELEASE_RELOAD_TIMER);
    }

    public void setGriffinReleaseReloadTimer(int griffinReleaseReloadTimer) {
        this.entityData.set(GRIFFIN_RELEASE_RELOAD_TIMER, griffinReleaseReloadTimer);
    }

    public boolean hasCalledReinforcements() {
        return this.entityData.get(HAS_CALLED_REINFORCEMENTS);
    }

    public void setHasCalledReinforcements(boolean hasCalledReinforcements) {
        this.entityData.set(HAS_CALLED_REINFORCEMENTS, hasCalledReinforcements);
    }

    public boolean hasNearbyDragonPlayer() {
        return this.entityData.get(NEARBY_DRAGON_PLAYER);
    }

    public void setNearbyDragonPlayer(boolean nearbyDragonPlayer) {
        this.entityData.set(NEARBY_DRAGON_PLAYER, nearbyDragonPlayer);
    }

    public boolean hasSummonedReinforcements() {
        return this.entityData.get(HAS_SUMMONED_REINFORCEMENTS);
    }

    public void setHasSummonedReinforcements(boolean hasSummonedReinforcements) {
        this.entityData.set(HAS_SUMMONED_REINFORCEMENTS, hasSummonedReinforcements);
    }

    public double getRunThreshold() {
        return 0.15;
    }

    public double getWalkThreshold() {
        return 0.01;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "everything", 3, this::fullPredicate));
        controllers.add(new AnimationController<>(this, "arms", 3, this::armsPredicate));
    }

    public boolean isIdle() {
        double movement = AnimationUtils.getMovementSpeed(this);
        return !(swingTime > 0 || movement > getWalkThreshold());
    }

    public PlayState fullPredicate(final AnimationState<Hunter> state) {
        double movement = AnimationUtils.getMovementSpeed(this);
        boolean isCurrentlyIdlingRandomly = false;
        if (hasReleasedGriffin() && !hasPlayedReleaseAnimation && !hasPlayedReinforcementsAnimation) {
            if (hasCalledReinforcements()) {
                hasPlayedReinforcementsAnimation = true;
                ambusherTickTimer.putAnimation(AMBUSH_AND_GRIFFIN_RELEASE, (double) AMBUSH_ANIM_DURATION);
                state.setAndContinue(AMBUSH_AND_GRIFFIN_RELEASE);
            } else {
                hasPlayedReleaseAnimation = true;
                ambusherTickTimer.putAnimation(ONLY_GRIFFIN_RELEASE, (double) GRIFFIN_RELEASE_ANIM_DURATION);
                state.setAndContinue(ONLY_GRIFFIN_RELEASE);
            }
        } else if (ambusherTickTimer.getDuration(ONLY_GRIFFIN_RELEASE) > 0 || ambusherTickTimer.getDuration(AMBUSH_AND_GRIFFIN_RELEASE) > 0) {
            // Let release animation conclude
            return PlayState.CONTINUE;
        } else {
            if (isInWater()) {
                if (hasReleasedGriffin()) {
                    state.setAndContinue(SWIM_NO_GRIFFIN);
                } else {
                    state.setAndContinue(SWIM);
                }
            } else if (movement > getRunThreshold()) {
                if (hasReleasedGriffin()) {
                    state.setAndContinue(RUN_NO_GRIFFIN);
                } else {
                    state.setAndContinue(RUN);
                }
            } else if (movement > getWalkThreshold()) {
                if (hasReleasedGriffin()) {
                    state.setAndContinue(WALK_NO_GRIFFIN);
                } else {
                    state.setAndContinue(WALK);
                }
            } else {
                if (hasReleasedGriffin()) {
                    state.setAndContinue(IDLE_NO_GRIFFIN);
                } else {
                    if (hasNearbyDragonPlayer()) {
                        state.setAndContinue(IDLE_AGGRESSIVE);
                    } else {
                        isCurrentlyIdlingRandomly = true;
                        state.setAndContinue(getIdleAnim());
                    }
                }
            }
        }

        if (!isCurrentlyIdlingRandomly && isRandomIdleAnimSet) {
            isRandomIdleAnimSet = false;
        }

        return PlayState.CONTINUE;
    }

    public PlayState armsPredicate(final AnimationState<Hunter> state) {
        if (hasReleasedGriffin() && getGriffinReleaseReloadTimer() == -1 && getAmbushHornTimer() == -1) {
            // We check at 1 because the first client tick already sees the value incremented by 1 (we start at 0)
            if (getRangedAttackTimer() == 1) {
                ambusherTickTimer.putAnimation(CROSSBOW_SHOOT_AND_RELOAD_BLEND, (double) CROSSBOW_SHOOT_AND_RELOAD_TIME);
                return state.setAndContinue(CROSSBOW_SHOOT_AND_RELOAD_BLEND);
            } else if (ambusherTickTimer.getDuration(CROSSBOW_SHOOT_AND_RELOAD_BLEND) > 0) {
                // Always let the reload animation conclude
                return PlayState.CONTINUE;
            } else {
                return state.setAndContinue(CROSSBOW_READY_BLEND);
            }
        }

        return PlayState.STOP;
    }

    public RawAnimation getIdleAnim() {
        if (!isRandomIdleAnimSet) {
            currentIdleAnim = IDLE_ANIMS.pickRandomAnimation();
            isRandomIdleAnimSet = true;
        }
        return currentIdleAnim;
    }

    private static final RandomAnimationPicker IDLE_ANIMS = new RandomAnimationPicker(
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle1"), 90),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle2"), 10)
    );

    private static final RawAnimation IDLE_AGGRESSIVE = RawAnimation.begin().thenLoop("idle_agressive_griffin");

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");

    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");

    private static final int AMBUSH_ANIM_DURATION = 83;
    private static final int AMBUSH_HORN_SOUND_START_TIME = 38;
    private static final int AMBUSH_ARROW_PLACE_SOUND_TIME = 74;
    private static final RawAnimation AMBUSH_AND_GRIFFIN_RELEASE = RawAnimation.begin().thenPlay("ambush_and_griffin_release");

    private static final int GRIFFIN_RELEASE_ANIM_DURATION = 42;
    private static final int GRIFFIN_RELEASE_ARROW_PLACE_SOUND_TIME = 33;
    private static final RawAnimation ONLY_GRIFFIN_RELEASE = RawAnimation.begin().thenPlay("griffin_release");

    private static final RawAnimation IDLE_NO_GRIFFIN = RawAnimation.begin().thenLoop("idle_no_griffin");

    private static final RawAnimation WALK_NO_GRIFFIN = RawAnimation.begin().thenLoop("walk_no_griffin");

    private static final RawAnimation RUN_NO_GRIFFIN = RawAnimation.begin().thenLoop("run_no_griffin");

    private static final RawAnimation SWIM_NO_GRIFFIN = RawAnimation.begin().thenLoop("swim_no_griffin");

    private static final RawAnimation CROSSBOW_READY_BLEND = RawAnimation.begin().thenLoop("blend_crossbow_ready");

    public static final int CROSSBOW_SHOOT_AND_RELOAD_TIME = 60;
    // The ambusher shoots his crossbow 4 ticks into the animation
    private static final int CROSSBOW_ATTACK_START_TIME = 4;
    private static final int CROSSBOW_RELOAD_CHARGE_SOUND_TIME = 25;
    private static final int CROSSBOW_RELOAD_ARROW_PLACE_SOUND_TIME = 49;
    private static final RawAnimation CROSSBOW_SHOOT_AND_RELOAD_BLEND = RawAnimation.begin().thenPlay("blend_crossbow_shoot_and_reloading");
}
