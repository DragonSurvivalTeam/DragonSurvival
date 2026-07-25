package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SourceOfMagicPlaceholder extends BlockEntity {
    public BlockPos rootPos = BlockPos.ZERO;

    public SourceOfMagicPlaceholder(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.SOURCE_OF_MAGIC_PLACEHOLDER.get(), position, state);
    }

    @Override
    public void load(@NotNull final CompoundTag tag) {
        super.load(tag);
        rootPos = BlockPos.of(tag.getLong("root"));
    }

    @Override
    protected void saveAdditional(@NotNull final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("root", rootPos.asLong());
    }
}
