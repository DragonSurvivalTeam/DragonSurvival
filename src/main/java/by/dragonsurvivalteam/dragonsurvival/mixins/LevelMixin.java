package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClimbingHandler;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.magic.ClimbCheck;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
    private void dragonSurvival$refreshClimbCheckOnBlockChange(final BlockPos position, final BlockState state, final int flags, final int recursionLeft, final CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) {
            // No block change
            return;
        }

        Level self = (Level) (Object) this;

        if (!(self instanceof ServerLevel level)) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            ClimbableData data = AttachmentManager.getExistingData(player, DSDataAttachments.CLIMBABLE_DATA).orElse(null);

            if (data == null || data.trackedClimbPositions == null || !data.trackedClimbPositions.contains(position)) {
                continue;
            }

            PacketDistributor.sendToPlayer(player, new ClimbCheck(ClimbingHandler.filterPositions(data, level, player, data.trackedClimbPositions)));
        }
    }
}
