package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTimer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.CommonTraits;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowMobGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.HunterEntityCheckProcedure;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KnightEntity extends PathfinderMob implements GeoEntity, DragonHunter, CommonTraits {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final AnimationTimer animationTimer = new AnimationTimer();

	public KnightEntity(final EntityType<? extends PathfinderMob> type, final Level level) {
		super(type, level);
	}

	@Override
	public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
		// TODO :: Similar to other entities - extract into method?
		controllers.add(new AnimationController<>(this, "everything", 3, state -> {
			AnimationController<KnightEntity> animationController = state.getController();
			double movement = getMovementSpeed(this);

			if (swingTime > 0) {
				AnimationProcessor.QueuedAnimation currentAnimation = animationController.getCurrentAnimation();

				if (currentAnimation != null) {
					switch (currentAnimation.animation().name()) {
						case "attack" -> {
							if (animationTimer.getDuration("attack2") <= 0) {
								if (random.nextBoolean()) {
									animationTimer.putAnimation("attack", 17d);
									return state.setAndContinue(ATTACK);
								} else {
									animationTimer.putAnimation("attack2", 17d);
									return state.setAndContinue(ATTACK_2);
								}
							}
						}
						case "attack2" -> {
							if (animationTimer.getDuration("attack") <= 0) {
								if (random.nextBoolean()) {
									animationTimer.putAnimation("attack", 17d);
									return state.setAndContinue(ATTACK);
								} else {
									animationTimer.putAnimation("attack2", 17d);
									return state.setAndContinue(ATTACK_2);
								}
							}
						}
						default -> {
							if (random.nextBoolean()) {
								animationTimer.putAnimation("attack", 17d);
								return state.setAndContinue(ATTACK);
							} else {
								animationTimer.putAnimation("attack2", 17d);
								return state.setAndContinue(ATTACK_2);
							}
						}
					}
				}
			}

			// TODO 1.20 :: AnimationUtils.createAnimation
			if (movement > 0.5) {
				return state.setAndContinue(WALK);
			} else if (movement > 0.05) {
				return state.setAndContinue(RUN);
			} else {
				AnimationProcessor.QueuedAnimation currentAnimation = animationController.getCurrentAnimation();

				if (currentAnimation == null) {
					animationTimer.putAnimation("idle", 88d);
					return state.setAndContinue(IDLE);
				} else {
					switch (currentAnimation.animation().name()) {
						case "idle" -> {
							if (animationTimer.getDuration("idle") <= 0) {
								if (random.nextInt(2000) == 0) {
									animationTimer.putAnimation("idle_2", 145d);
									return state.setAndContinue(IDLE_2);
								}
							}
						}
						case "walk", "run" -> {
							animationTimer.putAnimation("idle", 88d);
							return state.setAndContinue(IDLE);
						}
						case "idle_2" -> {
							if (animationTimer.getDuration("idle_2") <= 0) {
								animationTimer.putAnimation("idle", 88d);
								return state.setAndContinue(IDLE);
							}
						}
					}
				}
			}

			return PlayState.CONTINUE;
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Hunter.class).setAlertOthers());
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 1, true, false, living -> living.hasEffect(MobEffects.BAD_OMEN) || living.hasEffect(DSEffects.ROYAL_CHASE)));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, false, false) {
			@Override
			public boolean canUse() {
				return super.canUse() && HunterEntityCheckProcedure.execute(KnightEntity.this);
			}

			@Override
			public boolean canContinueToUse() {
				return super.canContinueToUse() && HunterEntityCheckProcedure.execute(KnightEntity.this);
			}
		});
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 2.0, false));
		this.targetSelector.addGoal(6, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.1d));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
	}

	@Override
	public void tick(){
		updateSwingTime();
		super.tick();
	}

	@Override
	public boolean removeWhenFarAway(double distance){
		return !hasCustomName() && tickCount >= Functions.minutesToTicks(ServerConfig.hunterDespawnDelay);
	}

	@Override
	public boolean isBlocking(){
		if(getOffhandItem().getItem() == Items.SHIELD){
			return random.nextBoolean();
		}
		return false;
	}

	@Nullable public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverWorld, DifficultyInstance difficultyInstance, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData){
		populateDefaultEquipmentSlots(random, difficultyInstance);
		return super.finalizeSpawn(serverWorld, difficultyInstance, spawnReason, entityData);
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance){
		setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
		if(random.nextDouble() < ServerConfig.knightShieldChance){
			setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
		}
	}

	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
	private static final RawAnimation ATTACK_2 = RawAnimation.begin().thenPlay("attack2");
	private static final RawAnimation RUN = RawAnimation.begin().thenPlay("run");
	private static final RawAnimation WALK = RawAnimation.begin().thenPlay("walk");
	private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
	private static final RawAnimation IDLE_2 = RawAnimation.begin().thenPlay("idle_2");
}