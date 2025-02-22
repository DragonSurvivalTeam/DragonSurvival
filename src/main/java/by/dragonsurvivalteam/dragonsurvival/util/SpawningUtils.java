package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

public class SpawningUtils {
    private static BlockPos findRandomSpawnPosition(final Level level, final Vec3 worldPos, int attempts, float radius) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < attempts; i++) {
            float f = level.random.nextFloat() * Mth.TWO_PI;
            double x = worldPos.x + Mth.floor(Mth.cos(f) * radius);
            double z = worldPos.z + Mth.floor(Mth.sin(f) * radius);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z);
            blockPos.set(x, y, z);

            if (level.hasChunksAt(blockPos.getX() - 10, blockPos.getY() - 10, blockPos.getZ() - 10, blockPos.getX() + 10, blockPos.getY() + 10, blockPos.getZ() + 10)) {
                return blockPos;
            }
        }

        return null;
    }

    public static boolean spawn(final Mob mob, final Vec3 position, final Level level, final MobSpawnType type, int attempts, float radius, boolean useSpawnParticles) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        BlockPos spawnPosition = findRandomSpawnPosition(level, position, attempts, radius);

        if (spawnPosition != null) {
            mob.setPos(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());
            EventHooks.finalizeMobSpawn(mob, serverLevel, level.getCurrentDifficultyAt(spawnPosition), type, null);
            level.addFreshEntity(mob);

            if (useSpawnParticles) {
                mob.spawnAnim();
            }

            return true;
        }

        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
    public static boolean isAirOrFluid(final BlockPos position, final Level level, final BlockPlaceContext context) {
        return !level.getFluidState(position).isEmpty() || level.isEmptyBlock(position) || level.getBlockState(position).canBeReplaced(context);
    }
}