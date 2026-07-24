package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.items.armor.PermanentEnchantmentItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/**
 * The event {@link net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent} is not called for tooltips <br>
 * So we need to add tooltips for permanent enchantments ourselves
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapOperation(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", ordinal = 3))
    public void dragonSurvival$getTooltipLines(ItemStack instance, DataComponentType<ItemEnchantments> component, Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, Operation<Void> original) {
        if (instance.getItem() instanceof PermanentEnchantmentItem) {
            HolderLookup.Provider lookup = context.registries();

            if (lookup == null) {
                return;
            }

            // Pass all enchantments to the tooltip logic
            ItemEnchantments oldEnchantments = instance.get(DataComponents.ENCHANTMENTS);
            // TODO :: should we only pass the component + the (merged) permanent enchantments (for mod compatibility / performance)?
            instance.set(DataComponents.ENCHANTMENTS, instance.getAllEnchantments(lookup.lookupOrThrow(Registries.ENCHANTMENT)));
            instance.addToTooltip(DataComponents.ENCHANTMENTS, context, consumer, flag);
            instance.set(DataComponents.ENCHANTMENTS, oldEnchantments);
        } else {
            original.call(instance, component, context, consumer, flag);
        }
    }
}
