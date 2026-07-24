package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.structures.EndPlatformHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @ModifyVariable(method = "getPortalDestination", at = @At(value = "STORE"), ordinal = 1)
    private BlockPos modifyBlockPosForEndSpawnPoint(final BlockPos original, @Local(argsOnly = true) final Entity entity, @Local(argsOnly = true) final ServerLevel level) {
        if (entity instanceof Player player && level.dimension() == ServerLevel.OVERWORLD) {
            BlockPos spawnPoint = EndPlatformHandler.getSpawnPoint(player);
            return spawnPoint != null ? spawnPoint : original;
        }

        return original;
    }

    // We need to bump the player up a tiny bit to prevent them from getting stuck in the floor when teleporting to the end
    @ModifyExpressionValue(method = "getPortalDestination", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;subtract(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 modifySubtractToPlaceDragonSlightlyAboveSpawnPoint(final Vec3 original, @Local(argsOnly = true) final Entity entity, @Local(argsOnly = true) final ServerLevel level) {
        if (DragonStateProvider.isDragon(entity)) {
            return original.add(0, 0.1f, 0);
        }

        return original;
    }

    @WrapOperation(method = "getPortalDestination", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/EndPlatformFeature;createEndPlatform(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Z)V"))
    private void spawnDragonPlatform(final ServerLevelAccessor accessor, final BlockPos position, boolean dropBlocks, final Operation<Void> original, @Local(argsOnly = true) final Entity entity) {
        if (!(entity instanceof Player player)) {
            original.call(accessor, position, dropBlocks);
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            original.call(accessor, position, dropBlocks);
            return;
        }

        if (!EndPlatformHandler.placePlatform(player, accessor.getLevel(), position)) {
            original.call(accessor, position, dropBlocks);
        }
    }
}
