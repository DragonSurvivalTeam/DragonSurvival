package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("dimensions")
    EntityDimensions dragonSurvival$getDimensions();

    @Invoker("collide")
    Vec3 dragonSurvival$collide(Vec3 velocity);
}
