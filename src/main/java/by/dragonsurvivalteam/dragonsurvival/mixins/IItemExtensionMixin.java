package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IItemExtension.class)
public interface IItemExtensionMixin {
    /** Return dragon food properties if the player is a dragon */
    @ModifyReturnValue(method = "getFoodProperties", at = @At("RETURN"))
    private FoodProperties dragonSurvival$getDragonFoodProperties(final FoodProperties original, final ItemStack stack, @Nullable final LivingEntity entity) {
        if (entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                return DragonFoodHandler.getDragonFoodProperties(handler.species(), stack, original);
            }
        }

        return original;
    }
}
