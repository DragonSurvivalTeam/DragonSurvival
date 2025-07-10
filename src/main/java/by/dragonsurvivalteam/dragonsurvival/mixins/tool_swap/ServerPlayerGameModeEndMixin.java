package by.dragonsurvivalteam.dragonsurvival.mixins.tool_swap;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayerGameMode.class, priority = 10_000)
public class ServerPlayerGameModeEndMixin {
    @Inject(method = "handleBlockBreakAction", at = @At("RETURN"))
    private void dragonSurvival$finishSwap(final CallbackInfo callback) {
        ClawInventoryData.getData(player).swapFinish(player);
    }

    @Inject(method = "incrementDestroyProgress", at = @At("RETURN"))
    private void dragonSurvival$finishSwap(final CallbackInfoReturnable<Float> callback) {
        ClawInventoryData.getData(player).swapFinish(player);
    }

    @Shadow
    @Final
    protected ServerPlayer player;
}
