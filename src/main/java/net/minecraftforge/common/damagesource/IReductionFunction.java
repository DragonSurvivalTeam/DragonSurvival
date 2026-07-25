package net.minecraftforge.common.damagesource;

@FunctionalInterface
public interface IReductionFunction {
    float modify(DamageContainer container, float reductionIn);
}
