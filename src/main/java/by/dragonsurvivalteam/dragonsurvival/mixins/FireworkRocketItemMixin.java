package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    /** If the {@link DSEnchantments#AERODYNAMIC_MASTERY} enchantment is present don't always shrink the itemstack */
    @WrapWithCondition(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private boolean dragonSurvival$applyEnchantmentEffect(ItemStack instance, int amount, LivingEntity entity, @Local FireworkRocketEntity rocket) {
        int level = EnchantmentUtils.getLevel(entity, DSEnchantments.AERODYNAMIC_MASTERY);
        return level == 0 || entity.getRandom().nextInt(0, level) == 0;
    }
}
