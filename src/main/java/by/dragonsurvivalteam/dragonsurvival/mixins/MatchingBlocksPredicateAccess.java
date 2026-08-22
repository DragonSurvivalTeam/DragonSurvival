package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Class is protected, meaning we'd need to transform the visibility to directly reference it
@Mixin(targets = "net.minecraft.world.level.levelgen.blockpredicates.MatchingBlocksPredicate")
public interface MatchingBlocksPredicateAccess {
    @Accessor("blocks")
    HolderSet<Block> dragonSurvival$blocks();
}
