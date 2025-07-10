package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.RandomAnimationPicker;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSpecificMobGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.WindupMeleeAttackGoal;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class SpearmanEntity extends Hunter {
    @ConfigRange(min = 1)
    @Translation(key = "spearman_health", type = Translation.Type.CONFIGURATION, comments = "Base value for the max health attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_health")
    public static double MAX_HEALTH = 24;

    @Override
    public double maxHealthConfig() {
        return MAX_HEALTH;
    }

    @ConfigRange(min = 0)
    @Translation(key = "spearman_attack_damage", type = Translation.Type.CONFIGURATION, comments = "Base value for the attack damage attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_damage")
    public static int ATTACK_DAMAGE = 6;

    @Override
    public double attackDamageConfig() {
        return ATTACK_DAMAGE;
    }

    @ConfigRange(min = 0)
    @Translation(key = "spearman_attack_knockback", type = Translation.Type.CONFIGURATION, comments = "Base value for the attack knockback attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_attack_knockback")
    public static int ATTACK_KNOCKBACK = 0;

    @Override
    public double attackKnockback() {
        return ATTACK_KNOCKBACK;
    }

    @ConfigRange(min = 0)
    @Translation(key = "spearman_movement_speed", type = Translation.Type.CONFIGURATION, comments = "Base value for the movement speed attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_movement_speed")
    public static double MOVEMENT_SPEED = 0.35;

    @Override
    public double movementSpeedConfig() {
        return MOVEMENT_SPEED;
    }

    @ConfigRange(min = 0)
    @Translation(key = "spearman_armor", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_armor")
    public static double ARMOR = 2;

    @Override
    public double armorConfig() {
        return ARMOR;
    }

    @ConfigRange(min = 0)
    @Translation(key = "spearman_armor_toughness", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor toughness attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_armor_toughness")
    public static double ARMOR_TOUGHNESS = 0;

    @Override
    public double armorToughnessConfig() {
        return ARMOR_TOUGHNESS;
    }

    @ConfigRange(min = 0)
    @Translation(key = "spearman_knockback_resistance", type = Translation.Type.CONFIGURATION, comments = "Base value for the knockback resistance attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_knockback_resistance")
    public static double KNOCKBACK_RESISTANCE = 0;

    @Override
    public double knockbackResistanceConfig() {
        return KNOCKBACK_RESISTANCE;
    }

    @ConfigRange(min = 0, max = 256)
    @Translation(key = "spearman_bonus_horizontal_reach", type = Translation.Type.CONFIGURATION, comments = "Additional horizontal reach for the spearman")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_bonus_horizontal_reach")
    public static double HORIZONTAL_REACH = 0.5;

    @ConfigRange(min = 0, max = 256)
    @Translation(key = "spearman_bonus_vertical_reach", type = Translation.Type.CONFIGURATION, comments = "Additional vertical reach for the spearman")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "spearman"}, key = "spearman_bonus_vertical_reach")
    public static double VERTICAL_REACH = 2.5;

    public SpearmanEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    private RawAnimation currentIdleAnim;
    private boolean isIdleAnimSet = false;

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new WindupMeleeAttackGoal(this, 1.0, 13));
        this.goalSelector.addGoal(8, new FollowSpecificMobGoal(this, 0.6, 10, 20, target -> target instanceof KnightEntity));
    }

    public double getRunThreshold() {
        return 0.15;
    }

    public double getWalkThreshold() {
        return 0.01;
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity pEntity) {
        return this.getAttackBoundingBox().inflate(HORIZONTAL_REACH, VERTICAL_REACH, HORIZONTAL_REACH).intersects(pEntity.getHitbox());
    }

    @Override
    public int getCurrentSwingDuration() {
        return 17;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "everything", 3, this::fullPredicate));
        controllers.add(new AnimationController<>(this, "head", 3, this::headPredicate));
        controllers.add(new AnimationController<>(this, "arms", 3, this::armsPredicate));
        controllers.add(new AnimationController<>(this, "legs", 3, this::legsPredicate));
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (!this.isAlive()) {
            return super.mobInteract(pPlayer, pHand);
        } else {
            if (itemstack.getItem() == DSItems.SPEARMAN_PROMOTION.value()) {
                if (!this.level().isClientSide) {
                    // Copied from witch conversion code
                    Mob leader = DSEntities.HUNTER_LEADER.get().create(this.level());
                    leader.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    leader.finalizeSpawn((ServerLevel) this.level(), this.level().getCurrentDifficultyAt(leader.blockPosition()), MobSpawnType.CONVERSION, null);
                    leader.setNoAi(this.isNoAi());
                    if (this.hasCustomName()) {
                        leader.setCustomName(this.getCustomName());
                        leader.setCustomNameVisible(this.isCustomNameVisible());
                    }

                    leader.setPersistenceRequired();
                    net.neoforged.neoforge.event.EventHooks.onLivingConvert(this, leader);
                    this.level().addFreshEntity(leader);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, this.getSoundSource(), 2.0F, 1.0F);
                    this.discard();
                }

                for (int i = 0; i < 20; i++) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX() + (this.random.nextDouble() - 0.5D) * 2D, this.getY() + this.random.nextDouble() * 2D, this.getZ() + (this.random.nextDouble() - 0.5D) * 2D, (this.random.nextDouble() - 0.5D) * 0.5D, this.random.nextDouble() * 0.5D, (this.random.nextDouble() - 0.5D) * 0.5D);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(pPlayer, pHand);
    }

    private boolean isNotIdle() {
        double movement = AnimationUtils.getMovementSpeed(this);
        return swingTime > 0 || movement > getWalkThreshold() || isAggro();
    }

    public PlayState fullPredicate(final AnimationState<SpearmanEntity> state) {
        if (isNotIdle()) {
            isIdleAnimSet = false;
            return PlayState.STOP;
        }

        return state.setAndContinue(getIdleAnim());
    }

    public PlayState headPredicate(final AnimationState<SpearmanEntity> state) {
        return state.setAndContinue(HEAD_BLEND);
    }

    public PlayState armsPredicate(final AnimationState<SpearmanEntity> state) {
        if (swingTime > 0) {
            return state.setAndContinue(ATTACK_BLEND);
        } else if (isAggro()) {
            return state.setAndContinue(AGGRO_BLEND);
        } else if (isNotIdle()) {
            return state.setAndContinue(WALK_ARMS_BLEND);
        }

        return PlayState.STOP;
    }

    public PlayState legsPredicate(final AnimationState<SpearmanEntity> state) {
        double movement = AnimationUtils.getMovementSpeed(this);

        if (movement > getRunThreshold()) {
            return state.setAndContinue(RUN_BLEND);
        } else if (movement > getWalkThreshold()) {
            return state.setAndContinue(WALK_BLEND);
        } else if (isAggro()) {
            return state.setAndContinue(IDLE_BLEND);
        }

        return PlayState.STOP;
    }

    public RawAnimation getIdleAnim() {
        if (!isIdleAnimSet) {
            currentIdleAnim = IDLE_ANIMS.pickRandomAnimation();
            isIdleAnimSet = true;
        }
        return currentIdleAnim;
    }

    private static final RandomAnimationPicker IDLE_ANIMS = new RandomAnimationPicker(
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle1"), 90),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle2"), 9),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle3"), 1)
    );

    private static final RawAnimation WALK_BLEND = RawAnimation.begin().thenLoop("blend_walk");

    private static final RawAnimation RUN_BLEND = RawAnimation.begin().thenLoop("blend_run");

    private static final RawAnimation IDLE_BLEND = RawAnimation.begin().thenLoop("blend_idle");

    private static final RawAnimation WALK_ARMS_BLEND = RawAnimation.begin().thenLoop("blend_walk_arms");

    private static final RawAnimation ATTACK_BLEND = RawAnimation.begin().thenLoop("blend_attack");

    private static final RawAnimation AGGRO_BLEND = RawAnimation.begin().thenLoop("blend_aggro");

    private static final RawAnimation HEAD_BLEND = RawAnimation.begin().thenLoop("blend_head");
}