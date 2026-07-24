package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HelmetBlockEntity extends BlockEntity {
    public HelmetBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(DSBlockEntities.HELMET.get(), pWorldPosition, pBlockState);
    }
}