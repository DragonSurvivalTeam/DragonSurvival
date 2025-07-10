package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Undoes the switch from {@link PlayerStartMixin}
 */
@Mixin(value = Player.class, /* Make sure it happens at the end */ priority = 10_000)
public abstract class PlayerEndMixin {
    @Inject(method = "attack", at = @At("RETURN"))
    public void switchEnd(CallbackInfo callback) {
        Player player = (Player) (Object) this;

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        ClawInventoryData.getData(player).swapFinish(player);
    }
}
