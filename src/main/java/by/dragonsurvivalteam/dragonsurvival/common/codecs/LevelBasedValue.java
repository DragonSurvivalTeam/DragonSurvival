package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Backport of the level-based values used by the 1.21 ability data.
 */
public interface LevelBasedValue {
    Codec<LevelBasedValue> TYPED_CODEC = Codec.STRING.dispatch(
            "type",
            LevelBasedValue::type,
            LevelBasedValue::codecForType
    );
    Codec<LevelBasedValue> CODEC = Codec.either(Constant.CODEC, TYPED_CODEC).xmap(
            either -> either.map(value -> value, value -> value),
            value -> value instanceof Constant constant ? Either.left(constant) : Either.right(value)
    );

    static Constant constant(final float value) {
        return new Constant(value);
    }

    static Linear perLevel(final float base, final float perLevelAboveFirst) {
        return new Linear(base, perLevelAboveFirst);
    }

    static Linear perLevel(final float perLevel) {
        return perLevel(perLevel, perLevel);
    }

    static Lookup lookup(final List<Float> values, final LevelBasedValue fallback) {
        return new Lookup(values, fallback);
    }

    private static Codec<? extends LevelBasedValue> codecForType(final String type) {
        return switch (type) {
            case Linear.TYPE -> Linear.CODEC.codec();
            case Lookup.TYPE -> Lookup.CODEC.codec();
            default -> throw new IllegalArgumentException("Unknown level-based value type: " + type);
        };
    }

    float calculate(int level);

    String type();

    record Constant(float value) implements LevelBasedValue {
        public static final Codec<Constant> CODEC = Codec.FLOAT.xmap(Constant::new, Constant::value);

        @Override
        public float calculate(final int level) {
            return value;
        }

        @Override
        public String type() {
            return "minecraft:constant";
        }
    }

    record Linear(float base, float perLevelAboveFirst) implements LevelBasedValue {
        public static final String TYPE = "minecraft:linear";
        public static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("base").forGetter(Linear::base),
                Codec.FLOAT.fieldOf("per_level_above_first").forGetter(Linear::perLevelAboveFirst)
        ).apply(instance, Linear::new));

        @Override
        public float calculate(final int level) {
            return base + perLevelAboveFirst * (level - 1);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }

    record Lookup(List<Float> values, LevelBasedValue fallback) implements LevelBasedValue {
        public static final String TYPE = "minecraft:lookup";
        public static final MapCodec<Lookup> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.listOf().fieldOf("values").forGetter(Lookup::values),
                LevelBasedValue.CODEC.fieldOf("fallback").forGetter(Lookup::fallback)
        ).apply(instance, Lookup::new));

        @Override
        public float calculate(final int level) {
            return level > 0 && level <= values.size() ? values.get(level - 1) : fallback.calculate(level);
        }

        @Override
        public String type() {
            return TYPE;
        }
    }
}
