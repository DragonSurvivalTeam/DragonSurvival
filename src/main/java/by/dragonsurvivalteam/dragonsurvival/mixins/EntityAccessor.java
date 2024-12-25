package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Dynamic
    @Accessor("dragonSurvival$previousEyeInFluidType")
    FluidType getPreviousEyeInFluidType();

    @Dynamic
    @Accessor("dragonSurvival$previousEyeInFluidType")
    void setPreviousEyeInFluidType(FluidType fluidType);

    @Dynamic
    @Accessor("dragonSurvival$eyeInFluidTypeLastTick")
    FluidType getEyeInFluidTypeLastTick();

    @Dynamic
    @Accessor("dragonSurvival$eyeInFluidTypeLastTick")
    void setEyeInFluidTypeLastTick(FluidType fluidType);
}
