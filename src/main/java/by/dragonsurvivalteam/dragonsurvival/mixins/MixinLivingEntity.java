package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin( LivingEntity.class )
public abstract class MixinLivingEntity extends Entity{
	@Shadow public abstract ItemStack getMainHandItem();
	@Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);
	@Shadow protected ItemStack useItem;
	@Shadow protected int useItemRemaining;

	public MixinLivingEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_){
		super(p_i48580_1_, p_i48580_2_);
	}

	@Redirect( method = "collectEquipmentChanges", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;" ) )
	private ItemStack grantDragonSwordAttributes(LivingEntity entity, EquipmentSlot slotType){
		if (slotType == EquipmentSlot.MAINHAND) {
			Object self = this;

			if (self instanceof Player player && DragonUtils.isDragon(player)) {
				DragonStateHandler cap = DragonUtils.getHandler(entity);

				if (ToolUtils.shouldUseDragonTools(getMainHandItem())) {
					// Without this the item in the dragon slot for the sword would not grant any of its attributes
					ItemStack sword = cap.getClawToolData().getClawsInventory().getItem(0);

					if (!sword.isEmpty()) {
						return sword;
					}
				}
			}
		}

		return getItemBySlot(slotType);
	}

	@Inject( at = @At( "HEAD" ), method = "rideableUnderWater()Z", cancellable = true )
	public void dragonRideableUnderWater(CallbackInfoReturnable<Boolean> ci){
		Object self = this;

		if (self instanceof Player) {
			if (DragonUtils.isDragon(this)) {
				ci.setReturnValue(true);
			}
		}
	}

	@Inject( at = @At( "HEAD" ), method = "eat", cancellable = true )
	public void dragonEat(Level level, ItemStack itemStack, CallbackInfoReturnable<ItemStack> ci){
		if(DragonFoodHandler.disableDragonFoodHandling){
			return;
		}

		Object self = this;

		if (self instanceof Player) {
			DragonStateProvider.getCap(this).ifPresent(dragonStateHandler -> {
				if (dragonStateHandler.isDragon()) {
					if (DragonFoodHandler.isDragonEdible(itemStack.getItem(), dragonStateHandler.getType())) {
						level.playSound(null, getX(), getY(), getZ(), getEatingSound(itemStack), SoundSource.NEUTRAL, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
						addEatEffect(itemStack, level, (LivingEntity) (Object) this);
						if (!((Object) this instanceof Player) || !((Player) (Object) this).getAbilities().instabuild) {
							itemStack.shrink(1);
						}
					}
					ci.setReturnValue(itemStack);
				}
			});
		}
	}

	@Shadow
	private void addEatEffect(ItemStack itemStack, Level level, LivingEntity object){
		throw new IllegalStateException("Mixin failed to shadow addEatEffect()");
	}

	@Shadow
	public SoundEvent getEatingSound(ItemStack itemStack){
		throw new IllegalStateException("Mixin failed to shadow getEatingSound()");
	}

	@Inject( at = @At( "HEAD" ), method = "addEatEffect", cancellable = true )
	public void addDragonEatEffect(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfo ci){
		if(DragonFoodHandler.disableDragonFoodHandling){
			return;
		}

		Object self = this;

		if (self instanceof Player) {
			DragonStateProvider.getCap(this).ifPresent(dragonStateHandler -> {
				if (dragonStateHandler.isDragon()) {
					Item item = itemStack.getItem();
					if (DragonFoodHandler.isDragonEdible(item, dragonStateHandler.getType())) {

						for (Pair<MobEffectInstance, Float> pair : DragonFoodHandler.getDragonFoodProperties(item, dragonStateHandler.getType()).getEffects()) {
							if (!level.isClientSide && pair.getFirst() != null) {
								if (pair.getFirst() != null && pair.getFirst().getEffect() != MobEffects.HUNGER && level.random.nextFloat() < pair.getSecond()) {
									livingEntity.addEffect(new MobEffectInstance(pair.getFirst()));
								}
								if (pair.getFirst().getEffect() == MobEffects.HUNGER) {
									if (livingEntity.hasEffect(MobEffects.HUNGER)) {
										int amp = livingEntity.getEffect(MobEffects.HUNGER).getAmplifier();
										livingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, pair.getFirst().getDuration(), pair.getFirst().getAmplifier() + 1 + amp));
										if (level.random.nextFloat() < 0.25F * amp) {
											livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, pair.getFirst().getDuration(), 0));
										}
									} else if (level.random.nextFloat() < pair.getSecond()) {
										livingEntity.addEffect(new MobEffectInstance(pair.getFirst()));
									}
								}
							}
						}
					}
					ci.cancel();
				}
			});
		}
	}

	@Inject( at = @At( "HEAD" ), method = "shouldTriggerItemUseEffects", cancellable = true )
	public void shouldDragonTriggerItemUseEffects(CallbackInfoReturnable<Boolean> ci) {
		if(DragonFoodHandler.disableDragonFoodHandling){
			return;
		}

		Object self = this;

		if (self instanceof Player) {
			DragonStateProvider.getCap(this).ifPresent(dragonStateHandler -> {
				if (dragonStateHandler.isDragon()) {
					int i = getUseItemRemainingTicks();
					FoodProperties food = useItem.getItem().getFoodProperties();
					boolean flag = food != null && food.isFastFood();
					flag = flag || i <= DragonFoodHandler.getUseDuration(useItem, dragonStateHandler.getType()) - 7;
					ci.setReturnValue(flag && i % 4 == 0);
				}
			});
		}
	}

	@Shadow
	public int getUseItemRemainingTicks(){
		throw new IllegalStateException("Mixin failed to shadow getUseItemRemainingTicks()");
	}

	@Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I", shift = Shift.AFTER ), method = "onSyncedDataUpdated" )
	public void onDragonSyncedDataUpdated(EntityDataAccessor<?> data, CallbackInfo ci){
		if(DragonFoodHandler.disableDragonFoodHandling){
			return;
		}

		Object self = this;

		if (self instanceof Player) {
			DragonStateProvider.getCap(this).ifPresent(dragonStateHandler -> {
				if (dragonStateHandler.isDragon()) {
					useItemRemaining = DragonFoodHandler.getUseDuration(useItem, dragonStateHandler.getType());
				}
			});
		}
	}

	@Inject( at = @At( value = "HEAD" ), method = "triggerItemUseEffects", cancellable = true )
	public void triggerDragonItemUseEffects(ItemStack stack, int count, CallbackInfo ci){
		if(DragonFoodHandler.disableDragonFoodHandling){
			return;
		}

		Object self = this;

		if (self instanceof Player) {
			DragonStateProvider.getCap(this).ifPresent(dragonStateHandler -> {
				if (dragonStateHandler.isDragon() && !stack.isEmpty() && isUsingItem() && stack.getUseAnimation() == UseAnim.NONE && DragonFoodHandler.isDragonEdible(stack.getItem(), dragonStateHandler.getType())) {
					spawnItemParticles(stack, count);
					playSound(getEatingSound(stack), 0.5F + 0.5F * (float) random.nextInt(2), (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
					ci.cancel();
				}
			});
		}
	}

	@Unique private DamageSource dragonSurvival$damageSource;

	@Inject(method = "hurt", at = @At(value = "HEAD"))
	public void storeDamageSource(final DamageSource damageSource, float amount, final CallbackInfoReturnable<Boolean> cir) {
		dragonSurvival$damageSource = damageSource;
	}

	@ModifyArg(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"), index = 0)
	public double disableKnockbackForMagic(double strength) {
		if (dragonSurvival$damageSource instanceof EntityDamageSource) {
			String id = dragonSurvival$damageSource.msgId;

			if (id.equals("onFire") || id.equals("magic")) {
				return 0;
			}
		}

		return strength;
	}

	@Shadow
	private void spawnItemParticles(ItemStack stack, int count){
		throw new IllegalStateException("Mixin failed to shadow spawnItemParticles()");
	}

	@Shadow
	public boolean isUsingItem(){
		throw new IllegalStateException("Mixin failed to shadow isUsingItem()");
	}
}