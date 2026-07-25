package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public abstract class IItemExtensionMixin {
    /** Return dragon food properties if the player is a dragon */
    public @Nullable FoodProperties getFoodProperties(final ItemStack stack, @Nullable final LivingEntity entity) {
        FoodProperties original = getFoodProperties();

        if (entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                return DragonFoodHandler.getDragonFoodProperties(handler.species(), stack, original);
            }
        }

        return original;
    }

    @Shadow
    public abstract FoodProperties getFoodProperties();
}
