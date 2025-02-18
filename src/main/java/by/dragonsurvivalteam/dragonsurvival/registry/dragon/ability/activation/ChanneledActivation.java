package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record ChanneledActivation(
        Optional<LevelBasedValue> initialManaCost,
        Optional<ManaCost> continuousManaCost,
        Optional<LevelBasedValue> castTime,
        Optional<LevelBasedValue> cooldown,
        Notification notification,
        boolean canMoveWhileCasting,
        Optional<Sound> sound,
        Optional<Animations> animations
) implements Activation {
    public static final MapCodec<ChanneledActivation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("initial_mana_cost").forGetter(ChanneledActivation::initialManaCost),
            ManaCost.CODEC
                    .validate(cost -> cost.manaCostType() == ManaCost.ManaCostType.TICKING ? DataResult.success(cost) : DataResult.error(() -> "Channeled activation only supports [ticking] continuous mana cost"))
                    .optionalFieldOf("continuous_mana_cost").forGetter(ChanneledActivation::continuousManaCost),
            LevelBasedValue.CODEC.optionalFieldOf("cast_time").forGetter(ChanneledActivation::castTime),
            LevelBasedValue.CODEC.optionalFieldOf("cooldown").forGetter(ChanneledActivation::cooldown),
            Notification.CODEC.optionalFieldOf("notification", Notification.DEFAULT).forGetter(ChanneledActivation::notification),
            Codec.BOOL.optionalFieldOf("can_move_while_casting", true).forGetter(ChanneledActivation::canMoveWhileCasting),
            Sound.CODEC.optionalFieldOf("sound").forGetter(ChanneledActivation::sound),
            Animations.CODEC.optionalFieldOf("animations").forGetter(ChanneledActivation::animations)
    ).apply(instance, ChanneledActivation::new));

    @Override
    public Type type() {
        return Type.CHANNELED;
    }

    @Override
    public MapCodec<? extends Activation> codec() {
        return CODEC;
    }

    @Override
    public float getInitialManaCost(final int level) {
        return initialManaCost.map(cost -> cost.calculate(level))
                .orElseGet(() -> Activation.super.getInitialManaCost(level));
    }

    @Override
    public int getCastTime(final int level) {
        return castTime.map(time -> (int) time.calculate(level))
                .orElseGet(() -> Activation.super.getCastTime(level));
    }

    @Override
    public int getCooldown(final int level) {
        return cooldown.map(cooldown -> (int) cooldown.calculate(level))
                .orElseGet(() -> Activation.super.getCooldown(level));
    }
}
