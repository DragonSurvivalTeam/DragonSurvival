package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @Redirect(method="use", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private void x(ItemStack instance, int pAmount, LivingEntity pEntity, @Local FireworkRocketEntity fireworkRocket) {
        if (fireworkRocket.isAttachedToEntity()) {
            int lvl = EnchantmentUtils.getLevel(pEntity.level(), DSEnchantments.AERODYNAMIC_MASTERY, pEntity);
            if (lvl == 0 || pEntity.getRandom().nextInt(0, lvl) == 0)
                instance.consume(pAmount, pEntity);
        }
    }
}
