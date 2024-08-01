package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.CrossbowAttackGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowMobGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.HunterEntityCheckProcedure;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Shooter extends Hunter implements CrossbowAttackMob{
	private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Shooter.class, EntityDataSerializers.BOOLEAN);

	protected int bolasCooldown;

	public Shooter(EntityType<? extends PathfinderMob> entityType, Level world){
		super(entityType, world);
		// Vary the cooldown per mob so they don't all throw the bolas at the same time
		bolasCooldown = getBolasCooldown();
	}

	protected int getBolasCooldown() {
		return Functions.secondsToTicks(ServerConfig.hunterBolasFrequency + ((random.nextDouble() - 0.5) * ServerConfig.hunterBolasFrequency) / 5.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(3, new CrossbowAttackGoal(this, 1.0, 8.0F));
		this.goalSelector.addGoal(7, new FollowMobGoal<>(KnightEntity.class, this, 15));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Shooter.class).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 1, true, false, living -> living.hasEffect(MobEffects.BAD_OMEN) || living.hasEffect(DragonEffects.ROYAL_CHASE)));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, Monster.class, false, false) {
			@Override
			public boolean canUse() {
				Entity entity = Shooter.this;
				return super.canUse() && HunterEntityCheckProcedure.execute(entity);
			}

			@Override
			public boolean canContinueToUse() {
				Entity entity = Shooter.this;
				return super.canContinueToUse() && HunterEntityCheckProcedure.execute(entity);
			}
		});
	}

	@Override
	public void tick(){
		super.tick();

		if(!ServerConfig.hunterHasBolas) {
			return;
		}

		LivingEntity target = getTarget();
		if(target instanceof Player){
			if(bolasCooldown == 0){
				if(target.hasEffect(DragonEffects.TRAPPED)) {
					// Wait to throw the bolas until the player is no longer trapped
					return;
				}
				performBolasThrow(target);
				bolasCooldown = getBolasCooldown();
			} else {
				bolasCooldown--;
			}
		}
	}

    public void performBolasThrow(LivingEntity target){
            Bolas bolas = new Bolas(this, level());
			Vec3 targetPos = target.position();
			if(target instanceof Player player)
			{
				DragonStateHandler handler = DragonStateProvider.getHandler(player);

				if (handler == null || !handler.isDragon()) {
					targetPos = targetPos.add(0, player.getEyeHeight(), 0);
				} else {
					targetPos = targetPos.add(0, DragonSizeHandler.calculateDragonEyeHeight(handler.getSize(), ServerConfig.hitboxGrowsPastHuman), 0);
				}
			}

			Vec3 rawShootDirection = targetPos.subtract(bolas.getEyePosition());
			float distance = (float) rawShootDirection.length();

			// Adjust the launch angle to account for gravity as the target is further away (could be much smarter but this works good enough for now)
			targetPos = targetPos.add(0, distance / 10.f, 0);

			// Also lead their shot a bit
			targetPos = targetPos.add(target.getDeltaMovement().scale(distance / 5.f));

			Vec3 shootDirection = targetPos.subtract(bolas.getEyePosition()).normalize();
			// TODO: Maybe add an inaccuracy config option? Or calculate it based off of difficulty level in some way like for skeletons?
            bolas.shoot(shootDirection.x, shootDirection.y, shootDirection.z, 1.6F, 0.98f);
            playSound(SoundEvents.WITCH_THROW, 1.0F, 0.4F);
            level().addFreshEntity(bolas);
	}

	@Override
	public IllagerArmPose getArmPose(){
		if(isChargingCrossbow()){
			return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
		}else if(isHolding(item -> item.getItem() instanceof CrossbowItem)){
			return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
		}else{
			return isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
		}
	}

	public boolean isChargingCrossbow(){
		return entityData.get(IS_CHARGING_CROSSBOW);
	}

	@Override
	public void setChargingCrossbow(boolean p_213671_1_){
		entityData.set(IS_CHARGING_CROSSBOW, p_213671_1_);
	}

	@Override
	public void shootCrossbowProjectile(LivingEntity p_230284_1_, ItemStack p_230284_2_, Projectile p_230284_3_, float p_230284_4_){
		shootCrossbowProjectile(this, p_230284_1_, p_230284_3_, p_230284_4_, 1.6F);
	}

	@Override
	public void onCrossbowAttackPerformed(){
		noActionTime = 0;
		ItemStack crossbow = getItemInHand(InteractionHand.MAIN_HAND);
		addArrow(crossbow);
	}

	private void addArrow(ItemStack stack){
		CompoundTag compoundNBT = stack.getOrCreateTag();
		ListTag listNBT = compoundNBT.getList("ChargedProjectiles", 10);
		CompoundTag nbt = new CompoundTag();
		new ItemStack(Items.ARROW).save(nbt);
		listNBT.add(nbt);
		compoundNBT.put("ChargedProjectiles", listNBT);
	}

	@Override
	public void performRangedAttack(LivingEntity p_82196_1_, float p_82196_2_){
		performCrossbowAttack(this, 1.6F);
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(IS_CHARGING_CROSSBOW, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundNBT){
		super.addAdditionalSaveData(compoundNBT);
		compoundNBT.putInt("Bolas cooldown", bolasCooldown);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundNBT){
		super.readAdditionalSaveData(compoundNBT);
		bolasCooldown = compoundNBT.getInt("Bolas cooldown");
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance p_180481_1_){
		ItemStack stack = new ItemStack(Items.CROSSBOW);
		addArrow(stack);
		setItemSlot(EquipmentSlot.MAINHAND, stack);
	}
	@Override
	public boolean removeWhenFarAway(double distance){
		return !hasCustomName() && tickCount >= Functions.minutesToTicks(ServerConfig.hunterDespawnDelay);
	}
}