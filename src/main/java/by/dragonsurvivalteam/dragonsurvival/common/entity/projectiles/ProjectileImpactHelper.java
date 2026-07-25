package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

final class ProjectileImpactHelper {
    private ProjectileImpactHelper() {
    }

    static void breakImpactBlock(final Projectile projectile, final ProjectileData.GeneralData data, final BlockHitResult result) {
        Level level = projectile.level();
        BlockPos position = result.getBlockPos();

        if (level.isClientSide() || !data.isImpactProjectile() || !projectile.mayInteract(level, position)) {
            return;
        }

        BlockState state = level.getBlockState(position);
        if (state.is(Blocks.DECORATED_POT)) {
            level.setBlock(position, state.setValue(BlockStateProperties.CRACKED, true), 4);
            level.destroyBlock(position, true, projectile);
        } else if (state.is(Blocks.CHORUS_FLOWER)) {
            level.destroyBlock(position, true, projectile);
        }
    }
}
