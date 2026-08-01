package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class IItemExtensionMixin {
    /** Forge 1.20 checks the vanilla edible flag before consulting stack-sensitive food properties. */
    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
    private boolean dragonSurvival$allowDragonFoodUse(final boolean original, final Level level, final Player player, final InteractionHand hand) {
        return original || DragonFoodHandler.isEdible(player, player.getItemInHand(hand));
    }

    /** Forge 1.20 also checks the vanilla edible flag before delegating completed item use to the entity. */
    @ModifyExpressionValue(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
    private boolean dragonSurvival$finishDragonFoodUse(final boolean original, final ItemStack stack, final Level level, final LivingEntity entity) {
        return original || entity instanceof Player player && DragonFoodHandler.isEdible(player, stack);
    }

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
