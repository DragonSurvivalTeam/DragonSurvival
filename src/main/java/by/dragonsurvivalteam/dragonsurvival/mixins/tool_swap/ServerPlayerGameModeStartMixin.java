package by.dragonsurvivalteam.dragonsurvival.mixins.tool_swap;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayerGameMode.class, priority = 1)
public class ServerPlayerGameModeStartMixin {
    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void dragonSurvival$startSwap(final BlockPos blockPosition, final ServerboundPlayerActionPacket.Action action, final Direction face, int maxBuildHeight, int sequence, final CallbackInfo callback) {
        ClawInventoryData.getData(player).swapStart(player, player.level().getBlockState(blockPosition));
    }

    @Inject(method = "incrementDestroyProgress", at = @At("HEAD"))
    private void dragonSurvival$startSwap(final BlockState state, final BlockPos position, final int startTick, final CallbackInfoReturnable<Float> cir) {
        ClawInventoryData.getData(player).swapStart(player, state);
    }

    @Shadow
    @Final
    protected ServerPlayer player;
}
