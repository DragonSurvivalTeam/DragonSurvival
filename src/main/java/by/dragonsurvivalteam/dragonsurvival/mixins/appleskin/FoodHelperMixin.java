package by.dragonsurvivalteam.dragonsurvival.mixins.appleskin;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.helpers.FoodHelper;

@Mixin(FoodHelper.class)
public class FoodHelperMixin {
    @ModifyReturnValue(method = "query", at=@At("RETURN"))
    private static FoodHelper.QueriedFoodResult dragonSurvival$query(FoodHelper.QueriedFoodResult original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) Player player) {
        if (original == null) {
            if (DragonStateProvider.isDragon(player)) {
                if (DragonFoodHandler.isEdible(player, itemStack)) {
                    FoodProperties foodProperties = DragonFoodHandler.getDragonFoodProperties(DragonStateProvider.getData(player).species(), itemStack, itemStack.getFoodProperties(player));
                    FoodValuesEvent foodValuesEvent = new FoodValuesEvent(player, itemStack, foodProperties, foodProperties);
                    NeoForge.EVENT_BUS.post(foodValuesEvent);
                    return new FoodHelper.QueriedFoodResult(foodValuesEvent.defaultFoodProperties, foodValuesEvent.modifiedFoodProperties, itemStack);
                }
            }
        }
        return original;
    }
}
