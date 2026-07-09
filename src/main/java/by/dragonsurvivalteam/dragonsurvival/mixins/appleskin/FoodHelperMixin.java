package by.dragonsurvivalteam.dragonsurvival.mixins.appleskin;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.helpers.ConsumableFood;
import squeek.appleskin.helpers.FoodHelper;

import java.util.Objects;

@Mixin(FoodHelper.class)
public class FoodHelperMixin {
    @ModifyReturnValue(method = "isFood", at = @At("RETURN"))
    private static boolean dragonSurvival$isDragonFood(final boolean original, final ItemStack stack, final Player player) {
        return original || DragonFoodHandler.isEdible(player, stack);
    }

    @ModifyReturnValue(method = "getDefaultFoodValues", at = @At("RETURN"))
    private static ConsumableFood dragonSurvival$getDragonFoodValues(final ConsumableFood original, final ItemStack stack, final Player player) {
        FoodProperties food = DragonFoodHandler.getFoodProperties(player, stack, stack.get(DataComponents.FOOD));
        Consumable consumable = DragonFoodHandler.getConsumable(player, stack, stack.get(DataComponents.CONSUMABLE));

        if (food == null || consumable == null) {
            return original;
        }

        if (Objects.equals(food, original.food()) && Objects.equals(consumable, original.consumable())) {
            return original;
        }

        return new ConsumableFood(food, consumable);
    }
}
