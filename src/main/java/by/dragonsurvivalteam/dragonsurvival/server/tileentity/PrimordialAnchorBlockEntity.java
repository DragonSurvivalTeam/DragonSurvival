package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PrimordialAnchorBlockEntity extends BaseBlockBlockEntity {
    public PrimordialAnchorBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(DSTileEntities.PRIMORDIAL_ANCHOR.get(), pWorldPosition, pBlockState);
    }

    public static void serverTick(Level level, BlockPos position, BlockState state, PrimordialAnchorBlockEntity blockEntity) {
        if(!level.isClientSide()) {
            if(ServerLifecycleHooks.getCurrentServer().getWorldData().endDragonFightData().dragonKilled()) {
                level.setBlockAndUpdate(position, state.setValue(PrimordialAnchorBlock.BLOODY, true));
            } else {
                level.setBlockAndUpdate(position, state.setValue(PrimordialAnchorBlock.BLOODY, false));
            }
        }
    }
}
