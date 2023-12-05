package by.dragonsurvivalteam.dragonsurvival.mixins.tool_swap;

import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerGameMode.class, priority = 10_000)
public class MixinServerPlayerGameModeEnd {
    @Inject(method = "handleBlockBreakAction", at = @At("RETURN"))
    private void finishSwap(final BlockPos blockPosition, final ServerboundPlayerActionPacket.Action action, final Direction face, int maxBuildHeight, int sequence, final CallbackInfo callback) {
        ToolUtils.swapFinish(player);
    }

    @Shadow @Final protected ServerPlayer player;
}
