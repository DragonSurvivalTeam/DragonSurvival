package by.dragonsurvivalteam.dragonsurvival.mixins.tool_swap;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MultiPlayerGameMode.class, priority = 1)
public class MultiPlayerGameModeStartMixin {
    @Inject(method = {"startDestroyBlock", "continueDestroyBlock"}, at = @At("HEAD"))
    private void startSwap(final BlockPos blockPosition, final Direction directionFacing, final CallbackInfoReturnable<Boolean> callback) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            ClawInventoryData.getData(localPlayer).swapStart(localPlayer, localPlayer.level().getBlockState(blockPosition));
        }
    }
}
