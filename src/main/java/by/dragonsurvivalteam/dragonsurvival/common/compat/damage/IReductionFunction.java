package by.dragonsurvivalteam.dragonsurvival.common.compat.damage;

@FunctionalInterface
public interface IReductionFunction {
    float modify(DamageContainer container, float reductionIn);
}
