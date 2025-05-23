package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
    public void loadAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        rootPos = BlockPos.of(tag.getLong("root"));
    }

    @Override
    protected void saveAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("root", rootPos.asLong());
    }
}