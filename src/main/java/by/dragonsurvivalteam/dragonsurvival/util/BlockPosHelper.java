package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlockPosHelper {
    public static BlockPos get(final Vec3i input) {
        return new BlockPos(input);
    }

    public static BlockPos get(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    public static BlockPos get(double x, double y, double z) {
        return get((int) x, (int) y, (int) z);
    }

    public static BlockPos get(float x, float y, float z) {
        return get((int) x, (int) y, (int) z);
    }

    public static BlockPos get(final Vec3 input) {
        return get(input.x(), input.y(), input.z());
    }

    public static Iterable<BlockPos> betweenClosed(final AABB aabb) {
        return BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ));
    }
}
