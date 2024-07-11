package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.data.DataDamageTypeTagsProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin( LivingEntity.class )
public abstract class MixinLivingEntity extends Entity{
	@Shadow public abstract ItemStack getMainHandItem();
	@Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);
	@Shadow protected ItemStack useItem;

	@Shadow public abstract void knockback(double pStrength, double pX, double pZ);

	public MixinLivingEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_){
		super(p_i48580_1_, p_i48580_2_);
	}

	@Redirect( method = "collectEquipmentChanges", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;" ) )
	private ItemStack grantDragonSwordAttributes(LivingEntity entity, EquipmentSlot slotType){
		if (slotType == EquipmentSlot.MAINHAND) {
			if ((LivingEntity)(Object)this instanceof Player player) {
				if(DragonUtils.isDragon(player)) {
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
		}

		return getItemBySlot(slotType);
	}

	@ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
	private boolean dragonSurvival$replaceIsEdibleInEat(boolean original, @Local(argsOnly = true) ItemStack stack) {
		LivingEntity entity = (LivingEntity) (Object) this;
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);

		if (handler != null && handler.isDragon()) {
			return DragonFoodHandler.isEdible(stack, handler.getType());
		}

		return original;
	}

	@ModifyExpressionValue(method = "addEatEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
	private boolean dragonSurvival$replaceIsEdibleInAddEatEffect(boolean original, @Local(argsOnly = true) ItemStack stack) {
		LivingEntity entity = (LivingEntity) (Object) this;
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);

		if (handler != null && handler.isDragon()) {
			return DragonFoodHandler.isEdible(stack, handler.getType());
		}

		return original;
	}

	@ModifyExpressionValue(method = "addEatEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getFoodProperties(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/food/FoodProperties;", remap = false))
	private FoodProperties dragonSurvival$replaceFoodPropertiesInAddEatEffect(FoodProperties original, ItemStack food, Level pLevel, LivingEntity livingEntity) {
		LivingEntity entity = (LivingEntity) (Object) this;
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);

		if (handler != null && handler.isDragon()) {
			return DragonFoodHandler.getFoodProperties(food, handler.getType(), entity);
		}

		return original;
	}

	@Unique
	private int dragonSurvival$getHumanOrDragonUseDuration(int original) {
		if ((LivingEntity) (Object) this instanceof Player player) {
			DragonStateHandler handler = DragonStateProvider.getHandler(player);

			if (handler != null && handler.isDragon()) {
				return DragonFoodHandler.getUseDuration(useItem, handler.getType());
			}
		}

		return original;
	}

	@ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
	private int dragonSurvival$replaceUseDurationInShouldTriggerItemUseEffects(int original) {
		return dragonSurvival$getHumanOrDragonUseDuration(original);
	}

	@ModifyExpressionValue(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
	private int dragonSurvival$replaceUseDurationInSyncedDataUpdated(int original) {
		return dragonSurvival$getHumanOrDragonUseDuration(original);
	}

	@ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getFoodProperties(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/food/FoodProperties;", remap = false))
	private FoodProperties dragonSurvival$replaceGetFoodPropertiesInShouldTriggerItemUseEffects(FoodProperties original) {
		LivingEntity entity = (LivingEntity) (Object) this;
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);

		if (handler != null && handler.isDragon()) {
			return DragonFoodHandler.getFoodProperties(useItem, handler.getType(), entity);
		}

		return original;
	}

	@WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void dragonSurvival$disableKnockbackForMagic(LivingEntity instance, double pX, double pZ, double pStrength, Operation<Void> original, @Local(argsOnly = true) final DamageSource damageSource) {
		if (damageSource.is(DataDamageTypeTagsProvider.NO_KNOCKBACK)) {
			this.knockback(0, pX, pZ);
		} else {
			original.call(instance, pX, pZ, pStrength);
		}
	}

	@ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
	private UseAnim dragonSurvival$replaceGetUseAnimationInTriggerItemUseEffects0(UseAnim original, ItemStack stack, int amount) {
		LivingEntity entity = (LivingEntity) (Object) this;
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);

		if (handler != null && handler.isDragon()) {
			return DragonFoodHandler.isEdible(stack, handler.getType()) ? UseAnim.EAT : original;
		}

		return original;
	}
}