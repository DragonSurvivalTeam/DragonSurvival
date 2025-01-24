package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class MixinItem {
	@ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
	private boolean replaceIsEdibleInUse(boolean original, Level level, Player player, InteractionHand hand) {
		if (DragonFoodHandler.disableDragonFoodHandling) {
			return original;
		}

		DragonStateHandler handler = DragonStateProvider.getHandler(player);

		if (handler != null) {
			return DragonFoodHandler.isEdible(player.getItemInHand(hand), handler.getType());
		}

		return original;
	}

	@ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getFoodProperties(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/food/FoodProperties;", remap = false))
	private FoodProperties replaceFoodPropertiesInUse(FoodProperties original, Level pLevel, Player player, InteractionHand pUsedHand, @Local ItemStack stack) {
		if (DragonFoodHandler.disableDragonFoodHandling) {
			return original;
		}

		DragonStateHandler handler = DragonStateProvider.getHandler(player);

		if (handler != null) {
			return DragonFoodHandler.getFoodProperties(stack, handler.getType(), player);
		}

		return original;
	}

	@ModifyExpressionValue (method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
	private boolean replaceIsEdibleInFinishUsingItem(boolean original, ItemStack stack, Level level, LivingEntity entity) {
		if (DragonFoodHandler.disableDragonFoodHandling) {
			return original;
		}

		DragonStateHandler handler = DragonStateProvider.getHandler(entity);

		if (handler != null) {
			return DragonFoodHandler.isEdible(stack, handler.getType());
		}

		return original;
	}
}