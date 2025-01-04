package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HelmetTileEntity extends BlockEntity {
    public HelmetTileEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(DSTileEntities.HELMET_TILE.get(), pWorldPosition, pBlockState);
    }
}