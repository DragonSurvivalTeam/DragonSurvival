package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTimer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.CommonTraits;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowMobGoal;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.HunterEntityCheckProcedure;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class KnightEntity extends PathfinderMob implements IAnimatable, DragonHunter, CommonTraits{
	AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
	AnimationTimer animationTimer = new AnimationTimer();

	public KnightEntity(EntityType<? extends PathfinderMob> p_i48576_1_, Level world){
		super(p_i48576_1_, world);
	}

	@Override
	public void registerControllers(AnimationData data){
		data.addAnimationController(new AnimationController<>(this, "everything", 3, event -> {
			AnimationBuilder animationBuilder = new AnimationBuilder();

			AnimationController<KnightEntity> animationController = event.getController();
			double movement = getMovementSpeed(this);
			if(swingTime > 0){
				Animation animation = animationController.getCurrentAnimation();
				if(animation != null){
					String name = animation.animationName;
					switch(name){
						case "attack":
							if(animationTimer.getDuration("attack2") <= 0){
								if(random.nextBoolean()){
									animationTimer.putAnimation("attack", 17d, animationBuilder);
								}else{
									animationTimer.putAnimation("attack2", 17d, animationBuilder);
								}
							}
							break;
						case "attack2":
							if(animationTimer.getDuration("attack") <= 0){
								if(random.nextBoolean()){
									animationTimer.putAnimation("attack", 17d, animationBuilder);
								}else{
									animationTimer.putAnimation("attack2", 17d, animationBuilder);
								}
							}
							break;
						default:
							if(random.nextBoolean()){
								animationTimer.putAnimation("attack", 17d, animationBuilder);
							}else{
								animationTimer.putAnimation("attack2", 17d, animationBuilder);
							}
					}
				}
			}
			if(movement > 0.5){
				animationBuilder.addAnimation("run");
			}else if(movement > 0.05){
				animationBuilder.addAnimation("walk");
			}else{
				Animation animation = animationController.getCurrentAnimation();
				if(animation == null){
					animationTimer.putAnimation("idle", 88d, animationBuilder);
				}else{
					String name = animation.animationName;
					switch(name){
						case "idle":
							if(animationTimer.getDuration("idle") <= 0){
								if(random.nextInt(2000) == 0){
									animationTimer.putAnimation("idle_2", 145d, animationBuilder);
								}
							}
							break;
						case "walk":
						case "run":
							animationTimer.putAnimation("idle", 88d, animationBuilder);
							break;
						case "idle_2":
							if(animationTimer.getDuration("idle_2") <= 0){
								animationTimer.putAnimation("idle", 88d, animationBuilder);
							}
							break;
					}
				}
			}
			animationController.setAnimation(animationBuilder);
			return PlayState.CONTINUE;
		}));
	}

	@Override
	public AnimationFactory getFactory(){
		return animationFactory;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Hunter.class).setAlertOthers());
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 1, true, false, living -> living.hasEffect(MobEffects.BAD_OMEN) || living.hasEffect(DragonEffects.ROYAL_CHASE)));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, Monster.class, false, false) {
			@Override
			public boolean canUse() {
				Entity entity = KnightEntity.this;
				return super.canUse() && HunterEntityCheckProcedure.execute(entity);
			}

			@Override
			public boolean canContinueToUse() {
				Entity entity = KnightEntity.this;
				return super.canContinueToUse() && HunterEntityCheckProcedure.execute(entity);
			}
		});
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 2.0, false) {
			@Override
			protected double getAttackReachSqr(LivingEntity entity) {
				return (double) (4.0 + entity.getBbWidth() * entity.getBbWidth());
			}
		});
		this.targetSelector.addGoal(6, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(7, new FollowMobGoal<>(PrinceHorseEntity.class, this, 15));
		this.goalSelector.addGoal(8, new FollowMobGoal<>(PrincesHorseEntity.class, this, 15));
		this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.1d));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
	}

	@Override
	public int getExperienceReward(){
		return 5 + random.nextInt(5);
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

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverWorld, DifficultyInstance difficultyInstance, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag nbt){
		populateDefaultEquipmentSlots(random, difficultyInstance);
		return super.finalizeSpawn(serverWorld, difficultyInstance, spawnReason, entityData, nbt);
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance){
		setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
		if(random.nextDouble() < ServerConfig.knightShieldChance){
			setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
		}
	}
}