package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Consumable.class)
public abstract class ConsumableMixin {
    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$canConsumeDragonFood(final LivingEntity user, final ItemStack stack, final CallbackInfoReturnable<Boolean> callback) {
        FoodProperties original = stack.get(DataComponents.FOOD);
        FoodProperties properties = DragonFoodHandler.getFoodProperties(user, stack, original);

        if (!Objects.equals(properties, original)) {
            callback.setReturnValue(properties != null && (!(user instanceof Player player) || player.canEat(properties.canAlwaysEat())));
        }
    }

    @Inject(method = "onConsume", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$consumeDragonFood(final Level level, final LivingEntity user, final ItemStack stack, final CallbackInfoReturnable<ItemStack> callback) {
        FoodProperties original = stack.get(DataComponents.FOOD);
        FoodProperties properties = DragonFoodHandler.getFoodProperties(user, stack, original);

        if (properties == null || Objects.equals(properties, original)) {
            return;
        }

        Consumable self = (Consumable)(Object)this;
        self.emitParticlesAndSounds(user.getRandom(), user, stack, 16);

        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        stack.getAllOfType(ConsumableListener.class)
            .filter(listener -> !(listener instanceof FoodProperties))
            .forEach(listener -> listener.onConsume(level, user, stack, self));

        properties.onConsume(level, user, stack, self);

        if (!level.isClientSide()) {
            self.onConsumeEffects().forEach(effect -> effect.apply(level, stack, user));
        }

        user.gameEvent(self.animation() == ItemUseAnimation.DRINK ? GameEvent.DRINK : GameEvent.EAT);
        stack.consume(1, user);
        callback.setReturnValue(stack);
    }
}
