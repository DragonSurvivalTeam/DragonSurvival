package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("firstTick")
    boolean dragonSurvival$isFirstTick();

    @Accessor("dimensions")
    EntityDimensions dragonSurvival$getDimensions();

    @Accessor("vehicle")
    void dragonSurvival$setVehicle(Entity vehicle);

    @Invoker("addPassenger")
    void dragonSurvival$addPassenger(Entity passenger);

    @Invoker("removePassenger")
    void dragonSurvival$removePassenger(Entity passenger);
}
