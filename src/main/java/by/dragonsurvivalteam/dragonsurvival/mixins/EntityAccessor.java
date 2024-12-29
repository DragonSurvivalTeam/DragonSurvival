package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("reapplyPosition")
    void dragonSurvival$reapplyPosition();
}
