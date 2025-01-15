package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class BlockCondition {
    public static BlockPredicate blocks(final Block... blocks) {
        return BlockPredicate.matchesBlocks(blocks);
    }
}
