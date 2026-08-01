package by.dragonsurvivalteam.dragonsurvival.mixins.multimine;

import atomicstryker.multimine.client.MultiMineClient;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiMineClient.class)
public abstract class MultiMineClientMixin {
    @Inject(method = "onBlockMineFinishedDamagePlayerItem", at = @At("HEAD"), remap = false)
    private void dragonSurvival$swapStart(final Player player, final int x, final int y, final int z, final CallbackInfo callback) {
        ClawInventoryData.getData(player).swapStart(player, player.level().getBlockState(new BlockPos(x, y, z)));
    }

    @Inject(method = "onBlockMineFinishedDamagePlayerItem", at = @At("RETURN"), remap = false)
    private void dragonSurvival$swapFinish(final Player player, final int x, final int y, final int z, final CallbackInfo callback) {
        ClawInventoryData.getData(player).swapFinish(player);
    }
}
