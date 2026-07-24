package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

public class DragonRiderWorkbenchBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DragonRiderWorkbenchBlock> CODEC = simpleCodec(DragonRiderWorkbenchBlock::new);

    public DragonRiderWorkbenchBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull MapCodec<DragonRiderWorkbenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
}
