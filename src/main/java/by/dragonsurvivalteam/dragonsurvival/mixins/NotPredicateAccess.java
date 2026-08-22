package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Class is protected, meaning we'd need to transform the visibility to directly reference it
@Mixin(targets = "net.minecraft.world.level.levelgen.blockpredicates.NotPredicate")
public interface NotPredicateAccess {
    @Accessor("predicate")
    BlockPredicate dragonSurvival$predicate();
}
