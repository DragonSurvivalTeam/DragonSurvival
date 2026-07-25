package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.items.armor.PermanentEnchantmentItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Forge's enchantment-level event is not called while vanilla reads tooltip NBT.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapOperation(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"
            )
    )
    private void dragonSurvival$appendPermanentEnchantments(final List<Component> tooltips, final ListTag enchantments,
                                                            final Operation<Void> original) {
        ItemStack instance = (ItemStack) (Object) this;

        if (instance.getItem() instanceof PermanentEnchantmentItem) {
            ItemStack copy = instance.copy();
            EnchantmentHelper.setEnchantments(instance.getAllEnchantments(), copy);
            original.call(tooltips, copy.getEnchantmentTags());
        } else {
            original.call(tooltips, enchantments);
        }
    }
}
