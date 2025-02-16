package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record SimpleActivation(
        Optional<LevelBasedValue> initialManaCost,
        Optional<LevelBasedValue> castTime,
        Optional<LevelBasedValue> cooldown,
        Notification notification,
        boolean canMoveWhileCasting,
        Optional<Sound> sound,
        Optional<Animations> animations
) implements Activation {
    public static final MapCodec<SimpleActivation> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("initial_mana_cost").forGetter(SimpleActivation::initialManaCost),
            LevelBasedValue.CODEC.optionalFieldOf("cast_time").forGetter(SimpleActivation::castTime),
            LevelBasedValue.CODEC.optionalFieldOf("cooldown").forGetter(SimpleActivation::cooldown),
            Notification.CODEC.optionalFieldOf("notification", Notification.DEFAULT).forGetter(SimpleActivation::notification),
            Codec.BOOL.optionalFieldOf("can_move_while_casting", true).forGetter(SimpleActivation::canMoveWhileCasting),
            Sound.CODEC
                    .validate(sound -> sound.looping().isPresent() ? DataResult.error(() -> "Simple activation does not support [looping] sounds") : DataResult.success(sound))
                    .optionalFieldOf("sound").forGetter(SimpleActivation::sound),
            Animations.CODEC
                    .validate(animations -> animations.looping().isPresent() ? DataResult.error(() -> "Simple activation does not support [looping] animations") : DataResult.success(animations))
                    .optionalFieldOf("animations").forGetter(SimpleActivation::animations)
    ).apply(instance, SimpleActivation::new));

    @Override
    public Type type() {
        return Type.SIMPLE;
    }

    @Override
    public MapCodec<? extends Activation> codec() {
        return CODEC;
    }

    @Override
    public float getInitialManaCost(final int level) {
        return initialManaCost.map(cost ->cost.calculate(level))
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
