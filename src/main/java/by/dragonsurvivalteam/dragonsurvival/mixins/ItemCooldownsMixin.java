package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemCooldowns.class)
public abstract class ItemCooldownsMixin {
    @ModifyReturnValue(method = "isOnCooldown", at = @At("RETURN"))
    private boolean dragonSurvival$overrideDragonSoulCheck(final boolean original, @Local(argsOnly = true) final Item item) {
        if (item == DSItems.DRAGON_SOUL.value()) {
            // We only use the item cooldown for the visuals
            return false;
        }

        return original;
    }
}
