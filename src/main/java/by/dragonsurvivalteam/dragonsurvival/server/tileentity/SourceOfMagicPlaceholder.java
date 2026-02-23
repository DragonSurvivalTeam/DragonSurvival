package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class SourceOfMagicPlaceholder extends BlockEntity {
    public BlockPos rootPos = BlockPos.ZERO;

    public SourceOfMagicPlaceholder(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.SOURCE_OF_MAGIC_PLACEHOLDER.get(), position, state);
    }

    @Override
    public void loadAdditional(@NotNull ValueInput valueInput) {
        super.loadAdditional(valueInput);
        rootPos = BlockPos.of(valueInput.getLong("root").orElseThrow());
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putLong("root", rootPos.asLong());
    }
}