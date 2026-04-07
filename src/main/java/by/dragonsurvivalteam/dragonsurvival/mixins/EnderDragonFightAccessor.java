package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.level.dimension.end.EnderDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderDragonFight.class)
public interface EnderDragonFightAccessor {
    @Accessor("dragonKilled")
    boolean dragonSurvival$isDragonKilled();
}
