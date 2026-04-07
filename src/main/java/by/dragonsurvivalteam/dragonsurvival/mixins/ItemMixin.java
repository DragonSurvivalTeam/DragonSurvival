package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$useDragonConsumable(final Level level, final Player player, final InteractionHand hand, final CallbackInfoReturnable<InteractionResult> callback) {
        ItemStack stack = player.getItemInHand(hand);
        Consumable original = stack.get(DataComponents.CONSUMABLE);
        Consumable consumable = DragonFoodHandler.getConsumable(player, stack, original);

        if (consumable != null && !Objects.equals(consumable, original)) {
            callback.setReturnValue(consumable.startConsuming(player, stack, hand));
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$finishUsingDragonConsumable(final ItemStack stack, final Level level, final LivingEntity entity, final CallbackInfoReturnable<ItemStack> callback) {
        Consumable original = stack.get(DataComponents.CONSUMABLE);
        Consumable consumable = DragonFoodHandler.getConsumable(entity, stack, original);

        if (consumable != null && !Objects.equals(consumable, original)) {
            callback.setReturnValue(consumable.onConsume(level, entity, stack));
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$getDragonUseDuration(final ItemStack stack, final LivingEntity user, final CallbackInfoReturnable<Integer> callback) {
        Consumable original = stack.get(DataComponents.CONSUMABLE);
        Consumable consumable = DragonFoodHandler.getConsumable(user, stack, original);

        if (consumable != null && !Objects.equals(consumable, original)) {
            callback.setReturnValue(consumable.consumeTicks());
        }
    }
}
