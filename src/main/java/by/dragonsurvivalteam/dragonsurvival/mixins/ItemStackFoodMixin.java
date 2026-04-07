package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ItemStack.class)
public abstract class ItemStackFoodMixin {
    @Inject(method = "onUseTick", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$useDragonConsumable(
        final net.minecraft.world.level.Level level,
        final LivingEntity livingEntity,
        final int ticksRemaining,
        final CallbackInfo callback
    ) {
        ItemStack stack = (ItemStack)(Object)this;
        Consumable original = stack.get(DataComponents.CONSUMABLE);
        Consumable consumable = DragonFoodHandler.getConsumable(livingEntity, stack, original);

        if (consumable == null || Objects.equals(consumable, original)) {
            return;
        }

        if (consumable.shouldEmitParticlesAndSounds(ticksRemaining)) {
            consumable.emitParticlesAndSounds(livingEntity.getRandom(), livingEntity, stack, 5);
        }

        KineticWeapon kineticWeapon = stack.get(DataComponents.KINETIC_WEAPON);
        if (kineticWeapon != null && !level.isClientSide()) {
            kineticWeapon.damageEntities(stack, ticksRemaining, livingEntity, livingEntity.getUsedItemHand().asEquipmentSlot());
        } else {
            stack.getItem().onUseTick(level, livingEntity, stack, ticksRemaining);
        }

        callback.cancel();
    }

    @Inject(method = "applyAfterUseComponentSideEffects", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$applyDragonUseRemainder(
        final LivingEntity user,
        final ItemStack stackBeforeUsing,
        final CallbackInfoReturnable<ItemStack> callback
    ) {
        UseRemainder original = stackBeforeUsing.get(DataComponents.USE_REMAINDER);
        UseRemainder useRemainder = DragonFoodHandler.getUseRemainder(user, stackBeforeUsing, original);

        if (Objects.equals(useRemainder, original)) {
            return;
        }

        UseCooldown useCooldown = stackBeforeUsing.get(DataComponents.USE_COOLDOWN);
        int stackCountBeforeUsing = stackBeforeUsing.getCount();
        ItemStack result = (ItemStack)(Object)this;

        if (useRemainder != null) {
            result = useRemainder.convertIntoRemainder(result, stackCountBeforeUsing, user.hasInfiniteMaterials(), user::handleExtraItemsCreatedOnUse);
        }

        if (useCooldown != null) {
            useCooldown.apply(stackBeforeUsing, user);
        }

        callback.setReturnValue(result);
    }
}
