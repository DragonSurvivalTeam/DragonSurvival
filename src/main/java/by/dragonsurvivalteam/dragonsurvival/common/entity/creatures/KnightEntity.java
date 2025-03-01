package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.RandomAnimationPicker;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;

public class KnightEntity extends Hunter {
    @ConfigRange(min = 1)
    @Translation(key = "knight_health", type = Translation.Type.CONFIGURATION, comments = "Base value for the max health attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_health")
    public static double MAX_HEALTH = 40;

    @Override
    public double maxHealthConfig() {
        return MAX_HEALTH;
    }

    @ConfigRange(min = 0)
    @Translation(key = "knight_attack_damage", type = Translation.Type.CONFIGURATION, comments = "Base value for the attack damage attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_damage")
    public static int ATTACK_DAMAGE = 12;

    @Override
    public double attackDamageConfig() {
        return ATTACK_DAMAGE;
    }

    @ConfigRange(min = 0)
    @Translation(key = "knight_attack_knockback", type = Translation.Type.CONFIGURATION, comments = "Base value for the attack knockback attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_attack_knockback")
    public static int ATTACK_KNOCKBACK = 0;

    @Override
    public double attackKnockback() {
        return ATTACK_KNOCKBACK;
    }

    @ConfigRange(min = 0)
    @Translation(key = "knight_movement_speed", type = Translation.Type.CONFIGURATION, comments = "Base value for the movement speed attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_movement_speed")
    public static double MOVEMENT_SPEED = 0.3;

    @Override
    public double movementSpeedConfig() {
        return MOVEMENT_SPEED;
    }

    @ConfigRange(min = 0)
    @Translation(key = "knight_armor", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_armor")
    public static double ARMOR = 10;

    @Override
    public double armorConfig() {
        return ARMOR;
    }

    @ConfigRange(min = 0)
    @Translation(key = "knight_armor_toughness", type = Translation.Type.CONFIGURATION, comments = "Base value for the armor toughness attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_armor_toughness")
    public static double ARMOR_TOUGHNESS = 0;

    @Override
    public double armorToughnessConfig() {
        return ARMOR_TOUGHNESS;
    }

    @ConfigRange(min = 0)
    @Translation(key = "knight_knockback_resistance", type = Translation.Type.CONFIGURATION, comments = "Base value for the knockback resistance attribute")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_knockback_resistance")
    public static double KNOCKBACK_RESISTANCE = 0;

    @Override
    public double knockbackResistanceConfig() {
        return KNOCKBACK_RESISTANCE;
    }

    @ConfigRange(min = 0)
    @Translation(key = "knight_shield_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of knights having a shield")
    @ConfigOption(side = ConfigSide.SERVER, category = {"dragon_hunters", "knight"}, key = "knight_shield_chance")
    public static double SHIELD_CHANCE = 0.1;
    
    public KnightEntity(final EntityType<? extends PathfinderMob> type, final Level level) {
        super(type, level);
    }

    private RawAnimation currentIdleAnim;
    private boolean isIdleAnimSet = false;

    private RawAnimation currentAttackAnim;
    private boolean isAttackAnimSet = false;

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "everything", 3, this::fullPredicate));
        controllers.add(new AnimationController<>(this, "head", 3, this::headPredicate));
    }

    private boolean isNotIdle() {
        double movement = AnimationUtils.getMovementSpeed(this);
        return swingTime > 0 || movement > getWalkThreshold() || isInWater();
    }

    public PlayState fullPredicate(final AnimationState<KnightEntity> state) {
        double movement = AnimationUtils.getMovementSpeed(this);

        if (swingTime == 0) {
            isAttackAnimSet = false;
        }

        if (isIdleAnimSet) {
            isIdleAnimSet = !isNotIdle();
        }

        if (swingTime > 0) {
            return state.setAndContinue(getAttackAnim());
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

    public RawAnimation getAttackAnim() {
        if (!isAttackAnimSet) {
            currentAttackAnim = ATTACK_ANIMS.pickRandomAnimation();
            isAttackAnimSet = true;
        }
        return currentAttackAnim;
    }

    public PlayState headPredicate(final AnimationState<Hunter> state) {
        return state.setAndContinue(HEAD_BLEND);
    }

    @Override
    public int getCurrentSwingDuration() {
        return 17;
    }

    public double getRunThreshold() {
        return 0.15;
    }

    public double getWalkThreshold() {
        return 0.01;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 2.0, true));
    }

    @Override
    public void tick() {
        super.tick();
        applyMagicDisabledDebuff();
    }

    private void applyMagicDisabledDebuff() {
        double detectionRadius = 12.0;
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(detectionRadius));

        for (Player player : players) {
            if (DragonStateProvider.isDragon(player)) {
                player.addEffect(new MobEffectInstance(DSEffects.MAGIC_DISABLED, 240, 0, false, true));
            }
        }
    }

    @Override
    public boolean isBlocking() {
        if (getOffhandItem().getItem() == Items.SHIELD) {
            return random.nextBoolean();
        }
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor serverWorld, @NotNull DifficultyInstance difficultyInstance, @NotNull MobSpawnType spawnReason, @Nullable SpawnGroupData entityData) {
        populateDefaultEquipmentSlots(random, difficultyInstance);
        return super.finalizeSpawn(serverWorld, difficultyInstance, spawnReason, entityData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource randomSource, @NotNull DifficultyInstance difficultyInstance) {
        setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        
        if (random.nextDouble() < SHIELD_CHANCE) {
            setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        }
    }

    private static final RandomAnimationPicker ATTACK_ANIMS = new RandomAnimationPicker(
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("attack1"), 90),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("attack2"), 10)
    );

    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");

    private static final RandomAnimationPicker IDLE_ANIMS = new RandomAnimationPicker(
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle1"), 90),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle2"), 10)
    );

    private static final RawAnimation HEAD_BLEND = RawAnimation.begin().thenLoop("blend_head");
}