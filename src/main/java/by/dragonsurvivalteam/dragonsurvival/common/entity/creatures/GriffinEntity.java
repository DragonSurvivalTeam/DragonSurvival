package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSpecificMobGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.WindupMeleeAttackGoal;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class GriffinEntity extends Hunter {
    @ConfigRange(min = 1)
    @Translation(key = "griffin_health", type = Translation.Type.CONFIGURATION, comments = "Base value for the max health attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_health")
    public static double MAX_HEALTH = 10;

    @Override
    public double maxHealthConfig() {
        return MAX_HEALTH;
    }

    @ConfigRange(min = 0)
    @Translation(key = "griffin_attack_damage", type = Translation.Type.CONFIGURATION, comments = "Base value for the attack damage attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_damage")
    public static int ATTACK_DAMAGE = 2;

    @Override
    public double attackDamageConfig() {
        return ATTACK_DAMAGE;
    }

    @ConfigRange(min = 0)
    @Translation(key = "griffin_attack_knockback", type = Translation.Type.CONFIGURATION, comments = "Base value for the attack knockback attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_attack_knockback")
    public static int ATTACK_KNOCKBACK = 0;

    @Override
    public double attackKnockback() {
        return ATTACK_KNOCKBACK;
    }

    @ConfigRange(min = 0)
    @Translation(key = "griffin_movement_speed", type = Translation.Type.CONFIGURATION, comments = "Base value for the movement speed attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_movement_speed")
    public static double MOVEMENT_SPEED = 0.2;

    @Override
    public double movementSpeedConfig() {
        return MOVEMENT_SPEED;
    }

    @ConfigRange(min = 0)
    @Translation(key = "griffin_flying_speed", type = Translation.Type.CONFIGURATION, comments = "Base value for the flying speed attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_flying_speed")
    public static double FLYING_SPEED = 0.2;

    @ConfigRange(min = 0)
    @Translation(key = "griffin_armor", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_armor")
    public static double ARMOR = 0;

    @Override
    public double armorConfig() {
        return ARMOR;
    }

    @ConfigRange(min = 0)
    @Translation(key = "griffin_armor_toughness", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor toughness attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_armor_toughness")
    public static double ARMOR_TOUGHNESS = 0;

    @Override
    public double armorToughnessConfig() {
        return ARMOR_TOUGHNESS;
    }

    @ConfigRange(min = 0)
    @Translation(key = "griffin_knockback_resistance", type = Translation.Type.CONFIGURATION, comments = "Base value for the knockback resistance attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_knockback_resistance")
    public static double KNOCKBACK_RESISTANCE = 0;

    @Override
    public double knockbackResistanceConfig() {
        return KNOCKBACK_RESISTANCE;
    }

    @ConfigRange(min = 0, max = 256)
    @Translation(key = "griffin_range", type = Translation.Type.CONFIGURATION, comments = "Determines the attack radius of the griffin")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "griffin"}, key = "griffin_range")
    public static Double RANGE = 0.9d;

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
    public @Nullable SpawnGroupData finalizeSpawn(final @NotNull ServerLevelAccessor level, final @NotNull DifficultyInstance difficulty, final @NotNull MobSpawnType spawnType, final @Nullable SpawnGroupData spawnGroupData) {
        setBaseValue(Attributes.FLYING_SPEED, FLYING_SPEED);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
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
        return this.getBoundingBox().inflate(GriffinEntity.RANGE).intersects(pEntity.getHitbox());
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {
    }

    @Override
    protected void defineSynchedData(@NotNull final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CURRENT_ATTACK, 0);
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
