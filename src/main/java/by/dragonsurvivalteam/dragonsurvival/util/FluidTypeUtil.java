package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

public final class FluidTypeUtil {
    public static FluidType getEyeFluidType(final Entity entity) {
        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        FluidState fluidState = entity.level().getFluidState(eyePos);

        if (!fluidState.isEmpty() && entity.getEyeY() <= eyePos.getY() + fluidState.getHeight(entity.level(), eyePos)) {
            return fluidState.getType().getFluidType();
        }

        return NeoForgeMod.EMPTY_TYPE.value();
    }
}
