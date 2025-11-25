package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.lang.reflect.InvocationTargetException;

public interface BlockStateCollisionShapeAccess {
    VoxelShape dragonSurvival$getOriginalCollisionShape(BlockGetter world, BlockPos pos, CollisionContext context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
