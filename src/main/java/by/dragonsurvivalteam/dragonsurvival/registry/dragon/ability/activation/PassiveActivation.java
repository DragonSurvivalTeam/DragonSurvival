package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record PassiveActivation(Optional<ManaCost> continuousManaCost) implements Activation {
    public static final MapCodec<PassiveActivation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ManaCost.CODEC
                    .validate(cost -> cost.manaCostType() == ManaCost.ManaCostType.RESERVED ? DataResult.success(cost) : DataResult.error(() -> "Passive activation only supports [reserved] continuous mana cost"))
                    .optionalFieldOf("continuous_mana_cost").forGetter(PassiveActivation::continuousManaCost)
    ).apply(instance, PassiveActivation::new));

    @Override
    public Type type() {
        return Type.PASSIVE;
    }

    @Override
    public MapCodec<? extends Activation> codec() {
        return CODEC;
    }
}
