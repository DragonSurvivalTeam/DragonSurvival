package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// Class is protected, meaning we'd need to transform the visibility to directly reference it
@Mixin(targets = "net.minecraft.world.level.levelgen.blockpredicates.CombiningPredicate")
public interface CombiningPredicateAccess {
    @Accessor("predicates")
    List<BlockPredicate> dragonSurvival$predicates();
}
