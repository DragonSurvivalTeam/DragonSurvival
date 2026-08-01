package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
public class FoodDataMixin {
    /** Forge 1.20 checks the item's static food flag before reading stack-sensitive food properties. */
    @ModifyExpressionValue(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
    private boolean dragonSurvival$applyDragonFoodProperties(final boolean original, final Item item, final ItemStack stack, @Nullable final LivingEntity entity) {
        return original || entity instanceof Player player && DragonFoodHandler.isEdible(player, stack);
    }
}
