package by.dragonsurvivalteam.dragonsurvival.common.compat.datamaps;

import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DataMapReloadListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the same registry data-map files as the 1.21.1 NeoForge provider.
 */
public abstract class DataMapProvider implements DataProvider {
    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final PackOutput.PathProvider pathProvider;
    private final Map<DataMapType<?, ?>, Builder<?, ?>> builders = new HashMap<>();

    protected DataMapProvider(
            final PackOutput packOutput,
            final CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        this.lookupProvider = lookupProvider;
        this.pathProvider = packOutput.createPathProvider(
                PackOutput.Target.DATA_PACK, DataMapReloadListener.PATH
        );
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        return lookupProvider.thenCompose(provider -> {
            gather(provider);
            DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
            return CompletableFuture.allOf(builders.entrySet().stream()
                    .map(entry -> generate(cache, entry.getKey(), entry.getValue(), ops))
                    .toArray(CompletableFuture[]::new));
        });
    }

    protected void gather() {}

    protected void gather(final HolderLookup.Provider provider) {
        gather();
    }

    @SuppressWarnings("unchecked")
    public <T, R> Builder<T, R> builder(final DataMapType<R, T> type) {
        if (type instanceof AdvancedDataMapType<R, T, ?> advanced) {
            return builder(advanced);
        }
        return (Builder<T, R>) builders.computeIfAbsent(type, ignored -> new Builder<>(type));
    }

    @SuppressWarnings("unchecked")
    public <T, R, VR extends DataMapValueRemover<R, T>> AdvancedBuilder<T, R, VR> builder(
            final AdvancedDataMapType<R, T, VR> type
    ) {
        return (AdvancedBuilder<T, R, VR>) builders.computeIfAbsent(
                type, ignored -> new AdvancedBuilder<>(type)
        );
    }

    private CompletableFuture<?> generate(
            final CachedOutput cache,
            final DataMapType<?, ?> type,
            final Builder<?, ?> builder,
            final DynamicOps<JsonElement> ops
    ) {
        String registryFolder = registryFolder(type.registryKey().location());
        ResourceLocation outputId = type.id().withPrefix(registryFolder + "/");
        Path output = pathProvider.json(outputId);
        JsonElement json = builder.serialize(ops);
        return DataProvider.saveStable(cache, json, output);
    }

    private static String registryFolder(final ResourceLocation registryId) {
        return registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
                ? registryId.getPath()
                : registryId.getNamespace() + "/" + registryId.getPath();
    }

    @Override
    public String getName() {
        return "Data Maps";
    }

    public static class Builder<T, R> {
        private final Map<Either<TagKey<R>, ResourceKey<R>>, Entry<T>> values =
                new LinkedHashMap<>();
        protected final List<Removal<R>> removals = new ArrayList<>();
        protected final ResourceKey<Registry<R>> registryKey;
        protected final DataMapType<R, T> type;
        private final List<ICondition> conditions = new ArrayList<>();
        private boolean replace;

        public Builder(final DataMapType<R, T> type) {
            this.type = type;
            this.registryKey = type.registryKey();
        }

        public Builder<T, R> add(
                final ResourceKey<R> key,
                final T value,
                final boolean replace,
                final ICondition... conditions
        ) {
            values.put(Either.right(key), new Entry<>(value, replace, List.of(conditions)));
            return this;
        }

        public Builder<T, R> add(
                final ResourceLocation id,
                final T value,
                final boolean replace,
                final ICondition... conditions
        ) {
            return add(ResourceKey.create(registryKey, id), value, replace, conditions);
        }

        public Builder<T, R> add(
                final Holder<R> object,
                final T value,
                final boolean replace,
                final ICondition... conditions
        ) {
            return add(object.unwrapKey().orElseThrow(), value, replace, conditions);
        }

        public Builder<T, R> add(
                final TagKey<R> tag,
                final T value,
                final boolean replace,
                final ICondition... conditions
        ) {
            values.put(Either.left(tag), new Entry<>(value, replace, List.of(conditions)));
            return this;
        }

        public Builder<T, R> remove(final ResourceLocation id) {
            removals.add(new Removal<>(Either.right(ResourceKey.create(registryKey, id)), null));
            return this;
        }

        public Builder<T, R> remove(final TagKey<R> tag) {
            removals.add(new Removal<>(Either.left(tag), null));
            return this;
        }

        public Builder<T, R> remove(final Holder<R> value) {
            removals.add(new Removal<>(Either.right(value.unwrapKey().orElseThrow()), null));
            return this;
        }

        public Builder<T, R> replace(final boolean replace) {
            this.replace = replace;
            return this;
        }

        public Builder<T, R> conditions(final ICondition... conditions) {
            Collections.addAll(this.conditions, conditions);
            return this;
        }

        private JsonElement serialize(final DynamicOps<JsonElement> ops) {
            JsonObject root = new JsonObject();
            if (replace) {
                root.addProperty("replace", true);
            }

            JsonObject encodedValues = new JsonObject();
            values.forEach((target, entry) -> {
                JsonElement value = type.codec().encodeStart(ops, entry.value())
                        .getOrThrow(false, message -> {
                            throw new IllegalStateException(
                                    "Could not encode data map " + type.id() + ": " + message
                            );
                        });
                if (entry.replace()) {
                    JsonObject wrapped = new JsonObject();
                    wrapped.add("value", value);
                    wrapped.addProperty("replace", true);
                    value = wrapped;
                }
                encodedValues.add(targetName(target), withConditions(value, entry.conditions()));
            });
            root.add("values", encodedValues);

            if (!removals.isEmpty()) {
                root.add("remove", serializeRemovals(ops));
            }
            return withConditions(root, conditions);
        }

        protected JsonElement serializeRemovals(final DynamicOps<JsonElement> ops) {
            JsonArray result = new JsonArray();
            removals.forEach(removal -> result.add(targetName(removal.target())));
            return result;
        }
    }

    public static class AdvancedBuilder<T, R, VR extends DataMapValueRemover<R, T>>
            extends Builder<T, R> {
        private final AdvancedDataMapType<R, T, VR> advancedType;

        public AdvancedBuilder(final AdvancedDataMapType<R, T, VR> type) {
            super(type);
            this.advancedType = type;
        }

        public AdvancedBuilder<T, R, VR> remove(final TagKey<R> tag, final VR remover) {
            removals.add(new Removal<>(Either.left(tag), remover));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(final Holder<R> value, final VR remover) {
            removals.add(new Removal<>(Either.right(value.unwrapKey().orElseThrow()), remover));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(final ResourceLocation id, final VR remover) {
            removals.add(new Removal<>(
                    Either.right(ResourceKey.create(registryKey, id)), remover
            ));
            return this;
        }

        @Override
        protected JsonElement serializeRemovals(final DynamicOps<JsonElement> ops) {
            JsonArray result = new JsonArray();
            removals.forEach(removal -> {
                JsonObject encoded = new JsonObject();
                encoded.addProperty("key", targetName(removal.target()));
                if (removal.remover() != null) {
                    @SuppressWarnings("unchecked")
                    VR remover = (VR) removal.remover();
                    encoded.add("remover", advancedType.remover().encodeStart(ops, remover)
                            .getOrThrow(false, message -> {
                                throw new IllegalStateException(
                                        "Could not encode remover for " + type.id() + ": " + message
                                );
                            }));
                }
                result.add(encoded);
            });
            return result;
        }
    }

    private static JsonElement withConditions(
            final JsonElement value,
            final List<ICondition> conditions
    ) {
        if (conditions.isEmpty()) {
            return value;
        }

        JsonObject result = new JsonObject();
        JsonArray encodedConditions = new JsonArray();
        conditions.stream()
                .map(CraftingHelper::serialize)
                .map(DataMapProvider::normalizeCondition)
                .forEach(encodedConditions::add);
        result.add("neoforge:conditions", encodedConditions);

        if (value.isJsonObject()) {
            value.getAsJsonObject().entrySet().forEach(
                    entry -> result.add(entry.getKey(), entry.getValue())
            );
        } else {
            result.add("neoforge:value", value);
        }
        return result;
    }

    private static JsonObject normalizeCondition(final JsonObject condition) {
        JsonObject result = condition.deepCopy();
        if (result.has("type")) {
            String type = result.get("type").getAsString();
            if (type.startsWith("forge:")) {
                result.addProperty("type", "neoforge:" + type.substring("forge:".length()));
            }
        }
        return result;
    }

    private static <R> String targetName(
            final Either<TagKey<R>, ResourceKey<R>> target
    ) {
        return target.map(
                tag -> "#" + tag.location(),
                key -> key.location().toString()
        );
    }

    private record Entry<T>(T value, boolean replace, List<ICondition> conditions) {}

    private record Removal<R>(
            Either<TagKey<R>, ResourceKey<R>> target,
            Object remover
    ) {}
}
