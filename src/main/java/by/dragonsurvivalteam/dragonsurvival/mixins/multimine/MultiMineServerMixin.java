package by.dragonsurvivalteam.dragonsurvival.mixins.multimine;

import atomicstryker.multimine.common.MultiMineServer;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiMineServer.class)
public abstract class MultiMineServerMixin {
    @Inject(method = "onClientSentPartialBlockPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0, shift = At.Shift.BY, by = 2))
    private void dragonSurvival$swapStart(final ServerPlayer player, final int x, final int y, final int z, final float value, final CallbackInfo callback, @Local(name = "iblockstate") final BlockState state) {
        ClawInventoryData.getData(player).swapStart(player, state);
    }

    @Inject(method = "onClientSentPartialBlockPacket", at = @At(value = "RETURN"))
    private void dragonSurvival$swapStart(final ServerPlayer player, final int x, final int y, final int z, final float value, final CallbackInfo callback) {
        ClawInventoryData.getData(player).swapFinish(player);
    }
}
