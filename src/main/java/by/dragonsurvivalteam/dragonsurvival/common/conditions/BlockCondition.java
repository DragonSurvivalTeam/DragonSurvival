package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.Block;

public class BlockCondition {
    public static BlockPredicate blocks(final Block... blocks) {
        return BlockPredicate.Builder.block().of(blocks).build();
    }
}
