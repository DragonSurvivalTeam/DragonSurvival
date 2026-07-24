package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.util.Expression;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public class MiscCodecs {
    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumType) {
        return Codec.STRING.validate(string -> {
            try {
                Enum.valueOf(enumType, string);
                return DataResult.success(string);
            } catch (NullPointerException | IllegalArgumentException ignored) {
                return DataResult.error(() -> String.format("[%s] is not a valid entry of [%s]", string, Arrays.toString(enumType.getEnumConstants())));
            }
        }).xmap(string -> Enum.valueOf(enumType, string), Enum::name);
    }

    public static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = new StreamCodec<>() {
        public @NotNull Vec3 decode(@NotNull final ByteBuf buffer) {
            return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        public void encode(final @NotNull ByteBuf buffer, @NotNull final Vec3 input) {
            buffer.writeDouble(input.x());
            buffer.writeDouble(input.y());
            buffer.writeDouble(input.z());
        }
    };

    public static final StreamCodec<ByteBuf, Vec2> VEC2_STREAM_CODEC = new StreamCodec<>() {
        public @NotNull Vec2 decode(@NotNull final ByteBuf buffer) {
            return new Vec2(buffer.readFloat(), buffer.readFloat());
        }

        public void encode(final @NotNull ByteBuf buffer, @NotNull final Vec2 input) {
            buffer.writeFloat(input.x);
            buffer.writeFloat(input.y);
        }
    };

    public static Codec<Expression> expressionCodec(final String... variables) {
        return Codec.STRING.validate(value -> {
            try {
                Expression expression = new Expression(value);

                for (String variable : variables) {
                    expression.setVariable(variable, new BigDecimal(Math.random()));
                }

                expression.eval();
            } catch (Exception exception) {
                return DataResult.error(() -> "[" + value + "] is not a valid expression: [" + exception.getMessage() + "]");
            }

            return DataResult.success(value);
        }).xmap(Expression::new, Expression::getExpression);
    }

    public static <T> Codec<T> optionalCodec(final Codec<Optional<T>> codec) {
        return codec.xmap(optional -> optional.orElse(null), Optional::ofNullable);
    }

    /**
     * Allows specifying {@link com.mojang.serialization.Codec#optionalField(String, com.mojang.serialization.Codec, boolean)}}
     * when working with conditional codecs, without having to work with 'Optional&lt;Optional&lt;Something&gt;&gt;'
     **/
    public static <T> Codec<T> conditional(final Codec<T> codec) {
        return optionalCodec(ConditionalOps.createConditionalCodec(codec));
    }

    public static Codec<MinMaxBounds.Doubles> percentageBounds() {
        return MinMaxBounds.Doubles.CODEC.validate(value -> {
            boolean isValid = true;

            if (value.min().isPresent()) {
                double min = value.min().get();

                if (min < 0 || min > 1) {
                    isValid = false;
                }
            }

            if (value.max().isPresent()) {
                double max = value.max().get();

                if (max < 0 || max > 1) {
                    isValid = false;
                }
            }

            return isValid ? DataResult.success(value) : DataResult.error(() -> "Percentage check must be between 0 and 1: [" + value + "]");
        });
    }

    public static Codec<Double> doubleRange(double min, double max) {
        return Codec.DOUBLE.validate(value -> value >= min && value <= max
                ? DataResult.success(value)
                : DataResult.error(() -> "Value must be within range [" + min + ";" + max + "]: " + value)
        );
    }

    public record Bounds(double min, double max) {
        public static final Codec<Bounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("min").forGetter(Bounds::min),
                Codec.DOUBLE.fieldOf("max").forGetter(Bounds::max)
        ).apply(instance, instance.stable(Bounds::new)));

        public boolean matches(double value) {
            return min <= value && value <= max;
        }
    }

    public static Codec<Bounds> bounds() {
        return Bounds.CODEC.validate(value -> {
            if (value.min() >= 1 && value.max() > value.min()) {
                return DataResult.success(value);
            } else {
                return DataResult.error(() -> "Min must be at least 1 and max must be larger than min " + value);
            }
        });
    }

    public record DestructionData(EntityPredicate entityPredicate, BlockPredicate blockPredicate, double crushingGrowth, double blockDestructionGrowth, double crushingDamageScalar) {
        public static final Codec<DestructionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.CODEC.fieldOf("entity_predicate").forGetter(DestructionData::entityPredicate),
                BlockPredicate.CODEC.fieldOf("block_predicate").forGetter(DestructionData::blockPredicate),
                Codec.DOUBLE.fieldOf("crushing_growth").forGetter(DestructionData::crushingGrowth),
                Codec.DOUBLE.fieldOf("block_destruction_growth").forGetter(DestructionData::blockDestructionGrowth),
                Codec.DOUBLE.fieldOf("crushing_damage_scalar").forGetter(DestructionData::crushingDamageScalar)
        ).apply(instance, instance.stable(DestructionData::new)));

        public boolean isCrushingAllowed(double growth) {
            return growth >= crushingGrowth;
        }

        public boolean isBlockDestructionAllowed(double growth) {
            return growth >= blockDestructionGrowth;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
        public boolean isDestructionAllowed(double growth) {
            return isCrushingAllowed(growth) || isBlockDestructionAllowed(growth);
        }
    }
}
