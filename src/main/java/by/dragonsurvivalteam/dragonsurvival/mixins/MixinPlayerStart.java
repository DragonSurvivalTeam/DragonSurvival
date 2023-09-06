package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, priority = 1) // To make sure it's the first call in the method
public class MixinPlayerStart {
    // Did not notice any problems running on a server - but you could exclude the client thread from running this by checking `player instanceof ServerPlayer`

    @Inject(method = "attack", at = @At("HEAD"))
    public void switchStart(Entity target, CallbackInfo ci) {
        Object self = this;
        Player player = (Player) self;

        if (!DragonUtils.isDragon(player)) {
            return;
        }

        ItemStack toolSlot = ClawToolHandler.getDragonSword(player);
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (toolSlot != ItemStack.EMPTY) {
            player.setItemInHand(InteractionHand.MAIN_HAND, toolSlot);

            DragonStateHandler handler = DragonUtils.getHandler(player);
            handler.getClawToolData().getClawsInventory().setItem(0, ItemStack.EMPTY); // There is no real need to reset it here but doesn't hurt to do it
            handler.storedMainHand = mainHand;
            handler.switchedItems = true;
        }
    }
}
