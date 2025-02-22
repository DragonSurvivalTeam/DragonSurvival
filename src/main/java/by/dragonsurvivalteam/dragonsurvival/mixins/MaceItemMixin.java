package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MaceItem.class)
public class MaceItemMixin {
    @ModifyReturnValue(method = "canSmashAttack", at = @At(value = "RETURN"))
    private static boolean disallowFlyingDragonsFromSmashAttacking(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        if (entity instanceof Player player) {
            return original && !ServerFlightHandler.isFlying(player);
        } else {
            return original;
        }
    }
}
