package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    /** Apply the dragon diet food properties when eating from a placed food block that modifies food values directly (e.g. 'CakeBlock') */
    @Inject(method = "eat(IF)V", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$applyPendingBlockFoodFromValues(final int foodLevelModifier, final float saturationLevelModifier, final CallbackInfo callback) {
        if (DragonFoodHandler.applyPendingBlockFood((FoodData) (Object) this)) {
            callback.cancel();
        }
    }

    /** Same as above, for food blocks that pass their own 'FoodProperties' to 'FoodData#eat' */
    @Inject(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$applyPendingBlockFoodFromProperties(final FoodProperties foodProperties, final CallbackInfo callback) {
        if (DragonFoodHandler.applyPendingBlockFood((FoodData) (Object) this)) {
            callback.cancel();
        }
    }

    /** Remove markers that were never consumed */
    @Inject(method = "tick", at = @At("HEAD"))
    private void dragonSurvival$clearPendingBlockFood(final Player player, final CallbackInfo callback) {
        DragonFoodHandler.clearBlockFood((FoodData) (Object) this);
    }
}
