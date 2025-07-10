package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import com.mojang.serialization.MapCodec;

public record ConstantTrigger() implements ActivationTrigger {
    public static final ConstantTrigger INSTANCE = new ConstantTrigger();
    public static final MapCodec<ConstantTrigger> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public TriggerType type() {
        return TriggerType.CONSTANT;
    }

    @Override
    public MapCodec<? extends ActivationTrigger> codec() {
        return CODEC;
    }
}
