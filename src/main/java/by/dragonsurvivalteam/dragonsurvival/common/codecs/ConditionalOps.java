package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.List;
import java.util.Optional;

/**
 * Codec-based resource conditions compatible with the 1.21.1 NeoForge JSON format.
 */
public final class ConditionalOps {
    public static final String DEFAULT_CONDITIONS_KEY = "neoforge:conditions";
    public static final String LEGACY_CONDITIONS_KEY = "forge:conditions";
    public static final String CONDITIONAL_VALUE_KEY = "neoforge:value";

    private ConditionalOps() {}

    public static <T> Codec<Optional<T>> createConditionalCodec(final Codec<T> ownerCodec) {
        return Codec.of(new ConditionalEncoder<>(ownerCodec), new ConditionalDecoder<>(ownerCodec));
    }

    public static <T> Codec<List<T>> decodeListWithElementConditions(final Codec<T> ownerCodec) {
        return createConditionalCodec(ownerCodec).listOf().xmap(
                values -> values.stream().flatMap(Optional::stream).toList(),
                values -> values.stream().map(Optional::of).toList()
        );
    }

    public static boolean shouldRegisterEntry(final JsonElement json) {
        if (!(json instanceof JsonObject object) || !object.has(DEFAULT_CONDITIONS_KEY)) {
            return ICondition.shouldRegisterEntry(json);
        }

        JsonElement normalized = normalizeConditionNamespaces(object.get(DEFAULT_CONDITIONS_KEY));
        return CraftingHelper.processConditions(normalized.getAsJsonArray(), ICondition.IContext.TAGS_INVALID);
    }

    private static final class ConditionalEncoder<A> implements Encoder<Optional<A>> {
        private final Encoder<A> innerCodec;

        private ConditionalEncoder(final Encoder<A> innerCodec) {
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> DataResult<T> encode(final Optional<A> input, final DynamicOps<T> ops, final T prefix) {
            return input.<DataResult<T>>map(value -> innerCodec.encode(value, ops, prefix))
                    .orElseGet(() -> DataResult.error(() -> "Cannot encode an omitted conditional value"));
        }
    }

    private static final class ConditionalDecoder<A> implements Decoder<Optional<A>> {
        private final Decoder<A> innerCodec;

        private ConditionalDecoder(final Decoder<A> innerCodec) {
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> DataResult<Pair<Optional<A>, T>> decode(final DynamicOps<T> ops, final T input) {
            if (ops.compressMaps()) {
                return DataResult.error(() -> "Conditional codecs do not support compressed maps");
            }

            Optional<MapLike<T>> map = ops.getMap(input).result();
            if (map.isEmpty()) {
                return decodeInner(ops, input);
            }

            MapLike<T> inputMap = map.get();
            T conditions = inputMap.get(DEFAULT_CONDITIONS_KEY);
            if (conditions == null) {
                conditions = inputMap.get(LEGACY_CONDITIONS_KEY);
            }
            if (conditions == null) {
                return decodeInner(ops, input);
            }

            return conditionsMatch(ops, conditions).flatMap(conditionMatches -> {
                if (!conditionMatches) {
                    return DataResult.success(Pair.of(Optional.empty(), input));
                }

                T wrappedValue = inputMap.get(CONDITIONAL_VALUE_KEY);
                T value = wrappedValue != null ? wrappedValue : ops.createMap(inputMap.entries().filter(pair -> {
                    Optional<String> key = ops.getStringValue(pair.getFirst()).result();
                    return key.isEmpty()
                            || !key.get().equals(DEFAULT_CONDITIONS_KEY)
                            && !key.get().equals(LEGACY_CONDITIONS_KEY);
                }));
                return decodeInner(ops, value);
            });
        }

        private <T> DataResult<Pair<Optional<A>, T>> decodeInner(final DynamicOps<T> ops, final T input) {
            return innerCodec.decode(ops, input).map(result -> result.mapFirst(Optional::of));
        }
    }

    private static <T> DataResult<Boolean> conditionsMatch(final DynamicOps<T> ops, final T conditions) {
        JsonElement json = ops.convertTo(JsonOps.INSTANCE, conditions);
        if (!json.isJsonArray()) {
            return DataResult.error(() -> "Resource conditions must be an array");
        }

        try {
            for (JsonElement element : json.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    return DataResult.error(() -> "Resource conditions must contain objects");
                }
                DataResult<Boolean> result = conditionMatches(ops, element.getAsJsonObject());
                Optional<Boolean> matched = result.result();
                if (matched.isEmpty()) {
                    return result;
                }
                if (!matched.get()) {
                    return DataResult.success(false);
                }
            }
            return DataResult.success(true);
        } catch (RuntimeException exception) {
            return DataResult.error(() -> "Unable to evaluate resource condition: " + exception.getMessage());
        }
    }

    private static <T> DataResult<Boolean> conditionMatches(final DynamicOps<T> ops, final JsonObject condition) {
        if (!condition.has("type")) {
            return DataResult.error(() -> "Resource condition is missing its type");
        }

        ResourceLocation type = new ResourceLocation(condition.get("type").getAsString());
        String path = type.getPath();
        if (type.getNamespace().equals("neoforge") || type.getNamespace().equals("forge")) {
            if (path.equals("and") || path.equals("or")) {
                JsonArray values = condition.getAsJsonArray("values");
                boolean expected = path.equals("and");
                for (JsonElement value : values) {
                    Optional<Boolean> matched = conditionMatches(ops, value.getAsJsonObject()).result();
                    if (matched.isEmpty()) {
                        return DataResult.error(() -> "Unable to evaluate nested resource condition");
                    }
                    if (matched.get() != expected) {
                        return DataResult.success(!expected);
                    }
                }
                return DataResult.success(expected);
            }
            if (path.equals("not")) {
                return conditionMatches(ops, condition.getAsJsonObject("value")).map(value -> !value);
            }
        }

        if (type.equals(new ResourceLocation("dragonsurvival", "registered"))) {
            return registeredConditionMatches(ops, condition);
        }

        JsonObject normalized = normalizeConditionNamespaces(condition).getAsJsonObject();
        ICondition parsed = CraftingHelper.getCondition(normalized);
        return DataResult.success(parsed.test(ICondition.IContext.EMPTY));
    }

    @SuppressWarnings("unchecked")
    private static <T> DataResult<Boolean> registeredConditionMatches(final DynamicOps<T> ops, final JsonObject condition) {
        if (!(ops instanceof RegistryOps<?> registryOps)) {
            return DataResult.error(() -> "The dragonsurvival:registered condition requires registry-aware decoding");
        }

        ResourceLocation registryId = new ResourceLocation(condition.get("registry").getAsString());
        ResourceLocation valueId = new ResourceLocation(condition.get("value").getAsString());
        ResourceKey<? extends Registry<Object>> registryKey =
                (ResourceKey<? extends Registry<Object>>) (ResourceKey<?>) ResourceKey.createRegistryKey(registryId);
        ResourceKey<Object> valueKey = ResourceKey.create(registryKey, valueId);
        boolean present = registryContains(registryOps, registryKey, valueKey);
        return DataResult.success(present);
    }

    private static <E> boolean registryContains(
            final RegistryOps<?> ops,
            final ResourceKey<? extends Registry<? extends E>> registryKey,
            final ResourceKey<E> valueKey
    ) {
        return ops.getter(registryKey).flatMap(getter -> getter.get(valueKey)).isPresent();
    }

    private static JsonElement normalizeConditionNamespaces(final JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            element.getAsJsonArray().forEach(value -> result.add(normalizeConditionNamespaces(value)));
            return result;
        }
        if (!element.isJsonObject()) {
            return element.deepCopy();
        }

        JsonObject result = new JsonObject();
        element.getAsJsonObject().entrySet().forEach(entry -> {
            JsonElement value = normalizeConditionNamespaces(entry.getValue());
            if (entry.getKey().equals("type") && value.isJsonPrimitive()) {
                String type = value.getAsString();
                if (type.startsWith("neoforge:")) {
                    value = new com.google.gson.JsonPrimitive("forge:" + type.substring("neoforge:".length()));
                }
            }
            result.add(entry.getKey(), value);
        });
        return result;
    }
}
