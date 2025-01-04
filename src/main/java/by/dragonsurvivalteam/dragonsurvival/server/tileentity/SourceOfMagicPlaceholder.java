package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class SourceOfMagicPlaceholder extends BaseBlockBlockEntity {
    public BlockPos rootPos = BlockPos.ZERO;

    public SourceOfMagicPlaceholder(BlockPos pWorldPosition, BlockState pBlockState) {
        super(DSTileEntities.SOURCE_OF_MAGIC_PLACEHOLDER.get(), pWorldPosition, pBlockState);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        rootPos = BlockPos.of(pTag.getLong("Root"));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.putLong("Root", rootPos.asLong());
    }
}