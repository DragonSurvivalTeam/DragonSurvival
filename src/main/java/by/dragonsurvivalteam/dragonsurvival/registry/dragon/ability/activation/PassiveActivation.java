package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.ActivationTrigger;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.ConstantTrigger;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record PassiveActivation(Optional<ManaCost> continuousManaCost, Optional<LevelBasedValue> cooldown, ActivationTrigger trigger) implements Activation {
    public static final PassiveActivation DEFAULT = new PassiveActivation(Optional.empty(), Optional.empty(), ConstantTrigger.INSTANCE);

    public static final MapCodec<PassiveActivation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ManaCost.CODEC.optionalFieldOf("continuous_mana_cost").forGetter(PassiveActivation::continuousManaCost),
            // TODO :: render cooldown in dragon ability screen or somewhere else
            LevelBasedValue.CODEC.optionalFieldOf("cooldown").forGetter(PassiveActivation::cooldown),
            // TODO :: add trigger to description
            ActivationTrigger.CODEC.optionalFieldOf("trigger", ConstantTrigger.INSTANCE).forGetter(PassiveActivation::trigger)
    ).apply(instance, PassiveActivation::new));

    @Override
    public int getCooldown(final int level) {
        return cooldown.map(cooldown -> (int) cooldown.calculate(level))
                .orElseGet(() -> Activation.super.getCooldown(level));
    }

    @Override
    public Type type() {
        return Type.PASSIVE;
    }

    @Override
    public MapCodec<? extends Activation> codec() {
        return CODEC;
    }
}
