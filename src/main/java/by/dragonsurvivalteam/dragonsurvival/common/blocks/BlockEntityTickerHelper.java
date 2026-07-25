package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

final class BlockEntityTickerHelper {
    private BlockEntityTickerHelper() {}

    static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> create(
            final BlockEntityType<A> actualType,
            final BlockEntityType<E> expectedType,
            final BlockEntityTicker<? super E> ticker
    ) {
        return expectedType == actualType ? cast(ticker) : null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> cast(final BlockEntityTicker<? super E> ticker) {
        return (BlockEntityTicker<A>) ticker;
    }
}
