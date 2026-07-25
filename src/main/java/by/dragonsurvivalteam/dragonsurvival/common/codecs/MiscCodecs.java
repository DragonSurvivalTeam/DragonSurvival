package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.util.Expression;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class MiscCodecs {
    private static final Gson LOOT_CONDITION_GSON = Deserializers.createConditionSerializer().create();
    private static final Codec<MinMaxBounds.Doubles> DOUBLE_BOUNDS_CODEC = jsonCodec(MinMaxBounds.Doubles::fromJson, MinMaxBounds.Doubles::serializeToJson);
    public static final Codec<EntityPredicate> ENTITY_PREDICATE_CODEC = jsonCodec(EntityPredicate::fromJson, EntityPredicate::serializeToJson);
    public static final Codec<LootItemCondition> LOOT_ITEM_CONDITION_CODEC = jsonCodec(
            json -> LOOT_CONDITION_GSON.fromJson(json, LootItemCondition.class),
            LOOT_CONDITION_GSON::toJsonTree
    );

    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumType) {
        return ExtraCodecs.validate(Codec.STRING, string -> {
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
        return ExtraCodecs.validate(Codec.STRING, value -> {
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

    public static <T> Codec<T> registryDispatchCodec(
            final Supplier<IForgeRegistry<MapCodec<? extends T>>> registry,
            final String typeField,
            final Function<T, ? extends MapCodec<? extends T>> codec
    ) {
        return lazyCodec(() -> Objects.requireNonNull(
                registry.get(),
                "Registry for " + typeField + " has not been created yet"
        ).getCodec().dispatch(typeField, codec, MapCodec::codec));
    }

    public static <T> Codec<Holder<T>> forgeRegistryHolderCodec(final Supplier<IForgeRegistry<T>> registry) {
        return lazyCodec(() -> Objects.requireNonNull(registry.get(), "Forge registry has not been created yet")
                .getCodec()
                .xmap(
                        value -> registry.get().getHolder(value).orElseThrow(),
                        Holder::value
                ));
    }

    private static <T> Codec<T> lazyCodec(final Supplier<Codec<T>> codec) {
        return new Codec<>() {
            @Override
            public <U> DataResult<Pair<T, U>> decode(final DynamicOps<U> ops, final U input) {
                return codec.get().decode(ops, input);
            }

            @Override
            public <U> DataResult<U> encode(final T input, final DynamicOps<U> ops, final U prefix) {
                return codec.get().encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "Lazy[" + codec + "]";
            }
        };
    }

    public static final class RegistryHolder<T> implements Supplier<IForgeRegistry<T>> {
        private IForgeRegistry<T> registry;

        public void set(final IForgeRegistry<T> registry) {
            this.registry = registry;
        }

        @Override
        public IForgeRegistry<T> get() {
            return registry;
        }
    }

    public static Codec<MinMaxBounds.Doubles> percentageBounds() {
        return ExtraCodecs.validate(DOUBLE_BOUNDS_CODEC, value -> {
            boolean isValid = true;

            if (value.getMin() != null) {
                double min = value.getMin();

                if (min < 0 || min > 1) {
                    isValid = false;
                }
            }

            if (value.getMax() != null) {
                double max = value.getMax();

                if (max < 0 || max > 1) {
                    isValid = false;
                }
            }

            return isValid ? DataResult.success(value) : DataResult.error(() -> "Percentage check must be between 0 and 1: [" + value + "]");
        });
    }

    public static Codec<Double> doubleRange(double min, double max) {
        return ExtraCodecs.validate(Codec.DOUBLE, value -> value >= min && value <= max
                ? DataResult.success(value)
                : DataResult.error(() -> "Value must be within range [" + min + ";" + max + "]: " + value)
        );
    }

    public record Bounds(double min, double max) {
        public static final Codec<Bounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("min").forGetter(Bounds::min),
                Codec.DOUBLE.fieldOf("max").forGetter(Bounds::max)
        ).apply(instance, Bounds::new));

        public boolean matches(double value) {
            return min <= value && value <= max;
        }
    }

    public static Codec<Bounds> bounds() {
        return ExtraCodecs.validate(Bounds.CODEC, value -> {
            if (value.min() >= 1 && value.max() > value.min()) {
                return DataResult.success(value);
            } else {
                return DataResult.error(() -> "Min must be at least 1 and max must be larger than min " + value);
            }
        });
    }

    public record DestructionData(EntityPredicate entityPredicate, BlockPredicate blockPredicate, double crushingGrowth, double blockDestructionGrowth, double crushingDamageScalar) {
        public static final Codec<DestructionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ENTITY_PREDICATE_CODEC.fieldOf("entity_predicate").forGetter(DestructionData::entityPredicate),
                BlockPredicate.CODEC.fieldOf("block_predicate").forGetter(DestructionData::blockPredicate),
                Codec.DOUBLE.fieldOf("crushing_growth").forGetter(DestructionData::crushingGrowth),
                Codec.DOUBLE.fieldOf("block_destruction_growth").forGetter(DestructionData::blockDestructionGrowth),
                Codec.DOUBLE.fieldOf("crushing_damage_scalar").forGetter(DestructionData::crushingDamageScalar)
        ).apply(instance, DestructionData::new));

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

    private static <T> Codec<T> jsonCodec(final Function<JsonElement, T> decoder, final Function<T, JsonElement> encoder) {
        return ExtraCodecs.JSON.flatXmap(
                json -> {
                    try {
                        return DataResult.success(decoder.apply(json));
                    } catch (RuntimeException exception) {
                        return DataResult.error(exception::getMessage);
                    }
                },
                value -> {
                    try {
                        return DataResult.success(encoder.apply(value));
                    } catch (RuntimeException exception) {
                        return DataResult.error(exception::getMessage);
                    }
                }
        );
    }
}
