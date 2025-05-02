package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sets the claw sword to the main hand when attacking to make enchantments and other checks properly work <br>
 * The original main hand is transiently stored in the dragon data
 */
@Mixin(value = Player.class, /* Make sure it happens at the start */ priority = 1)
public abstract class PlayerStartMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    public void dragonSurvival$switchStart(CallbackInfo callback) {
        Player player = (Player) (Object) this;

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        ItemStack tool = ClawToolHandler.getDragonSword(player);

        if (tool.isEmpty()) {
            return;
        }

        ClawInventoryData.getData(player).swapStart(player, tool, ClawInventoryData.Slot.SWORD.ordinal());
    }
}
