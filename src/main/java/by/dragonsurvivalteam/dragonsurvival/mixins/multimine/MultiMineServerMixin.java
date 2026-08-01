package by.dragonsurvivalteam.dragonsurvival.mixins.multimine;

import atomicstryker.multimine.common.MultiMineServer;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiMineServer.class)
public abstract class MultiMineServerMixin {
    @Inject(
            method = "onClientSentPartialBlockPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    shift = At.Shift.AFTER,
                    remap = true
            ),
            remap = false
    )
    private void dragonSurvival$swapStart(final ServerPlayer player, final int x, final int y, final int z, final float value, final CallbackInfo callback) {
        ClawInventoryData.getData(player).swapStart(player, player.level().getBlockState(new BlockPos(x, y, z)));
    }

    @Inject(method = "onClientSentPartialBlockPacket", at = @At("RETURN"), remap = false)
    private void dragonSurvival$swapFinish(final ServerPlayer player, final int x, final int y, final int z, final float value, final CallbackInfo callback) {
        ClawInventoryData.getData(player).swapFinish(player);
    }
}
