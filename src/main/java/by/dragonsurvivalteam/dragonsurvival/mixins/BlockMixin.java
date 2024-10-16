package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method="playerDestroy", at=@At(value="HEAD"))
    public void playerDestroy$dragonSurvival(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, BlockEntity pBlockEntity, ItemStack pTool, CallbackInfo ci) {
        if (pPlayer instanceof ServerPlayer serverPlayer && pPlayer.isInLava()) {
            DSAdvancementTriggers.MINE_BLOCK_UNDER_LAVA.get().trigger(serverPlayer, (Block)(Object)(this));
        }
    }
}
