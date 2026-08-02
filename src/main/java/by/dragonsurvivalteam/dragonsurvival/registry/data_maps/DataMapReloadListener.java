package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ConditionalOps;
import by.dragonsurvivalteam.dragonsurvival.common.compat.datamaps.AdvancedDataMapType;
import by.dragonsurvivalteam.dragonsurvival.common.compat.datamaps.DataMapType;
import by.dragonsurvivalteam.dragonsurvival.common.compat.datamaps.DataMapValueMerger;
import by.dragonsurvivalteam.dragonsurvival.common.compat.datamaps.DataMapValueRemover;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Loads the 1.21.1 data-map JSON format on Forge 1.20.1.
 */
public final class DataMapReloadListener implements PreparableReloadListener {
    public static final String PATH = "data_maps";

    private static final Codec<JsonElement> JSON_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );

    private final RegistryAccess registryAccess;
    private final List<DataMapType<?, ?>> dataMaps;
    private final Runnable afterApply;

    public DataMapReloadListener(
            final RegistryAccess registryAccess,
            final List<DataMapType<?, ?>> dataMaps,
            final Runnable afterApply
    ) {
        this.registryAccess = registryAccess;
        this.dataMaps = dataMaps;
        this.afterApply = afterApply;
    }

    @Override
    public CompletableFuture<Void> reload(
            final PreparationBarrier preparationBarrier,
            final ResourceManager resourceManager,
            final ProfilerFiller preparationsProfiler,
            final ProfilerFiller reloadProfiler,
            final Executor backgroundExecutor,
            final Executor gameExecutor
    ) {
        return CompletableFuture.supplyAsync(
                        () -> load(resourceManager, preparationsProfiler),
                        backgroundExecutor
                )
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(this::apply, gameExecutor);
    }

    private Map<DataMapType<?, ?>, Map<?, ?>> load(
            final ResourceManager resourceManager,
            final ProfilerFiller profiler
    ) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        Map<DataMapType<?, ?>, Map<?, ?>> loaded = new LinkedHashMap<>();

        for (DataMapType<?, ?> dataMap : dataMaps) {
            profiler.push("dragon_survival_data_map/" + dataMap.id());
            loadUnchecked(resourceManager, ops, dataMap, loaded);
            profiler.pop();
        }
        return loaded;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void loadUnchecked(
            final ResourceManager manager,
            final RegistryOps<JsonElement> ops,
            final DataMapType<?, ?> dataMap,
            final Map<DataMapType<?, ?>, Map<?, ?>> loaded
    ) {
        loaded.put(dataMap, loadMap(manager, ops, (DataMapType) dataMap));
    }

    private <R, T> Map<ResourceKey<R>, T> loadMap(
            final ResourceManager manager,
            final RegistryOps<JsonElement> ops,
            final DataMapType<R, T> dataMap
    ) {
        Registry<R> registry = registryAccess.registryOrThrow(dataMap.registryKey());
        ResourceLocation registryId = dataMap.registryKey().location();
        String registryFolder = registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
                ? registryId.getPath()
                : registryId.getNamespace() + "/" + registryId.getPath();
        ResourceLocation file = new ResourceLocation(
                dataMap.id().getNamespace(),
                PATH + "/" + registryFolder + "/" + dataMap.id().getPath() + ".json"
        );

        Map<ResourceKey<R>, WithSource<R, T>> result = new HashMap<>();
        for (Resource resource : manager.getResourceStack(file)) {
            Map<ResourceKey<R>, WithSource<R, T>> beforeResource = new HashMap<>(result);
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                Optional<JsonElement> conditioned = ConditionalOps.createConditionalCodec(JSON_CODEC)
                        .parse(ops, json)
                        .getOrThrow(false, message -> logDecodeError(dataMap, resource, message));
                conditioned.ifPresent(value -> applyFile(ops, registry, dataMap, value, result));
            } catch (Exception exception) {
                result.clear();
                result.putAll(beforeResource);
                DragonSurvival.LOGGER.error(
                        "Could not read data map [{}] for registry [{}] from pack [{}]",
                        dataMap.id(), dataMap.registryKey().location(), resource.sourcePackId(), exception
                );
            }
        }

        Map<ResourceKey<R>, T> values = new HashMap<>();
        result.forEach((key, value) -> values.put(key, value.value()));
        return values;
    }

    private static void logDecodeError(
            final DataMapType<?, ?> dataMap,
            final Resource resource,
            final String message
    ) {
        DragonSurvival.LOGGER.error(
                "Could not decode data map [{}] from pack [{}]: {}",
                dataMap.id(), resource.sourcePackId(), message
        );
    }

    private <R, T> void applyFile(
            final RegistryOps<JsonElement> ops,
            final Registry<R> registry,
            final DataMapType<R, T> dataMap,
            final JsonElement json,
            final Map<ResourceKey<R>, WithSource<R, T>> result
    ) {
        JsonObject root = GsonHelper.convertToJsonObject(json, "data map");
        if (GsonHelper.getAsBoolean(root, "replace", false)) {
            result.clear();
        }

        JsonObject values = GsonHelper.getAsJsonObject(root, "values");
        Codec<Optional<MapEntry<T>>> entryCodec =
                ConditionalOps.createConditionalCodec(mapEntryCodec(dataMap.codec()));
        values.entrySet().forEach(entry -> {
            Either<TagKey<R>, ResourceKey<R>> target = parseTarget(dataMap.registryKey(), entry.getKey());
            Optional<MapEntry<T>> decoded = entryCodec.parse(ops, entry.getValue())
                    .getOrThrow(false, message -> DragonSurvival.LOGGER.error(
                            "Could not decode value [{}] in data map [{}]: {}",
                            entry.getKey(), dataMap.id(), message
                    ));
            decoded.ifPresent(value -> resolve(registry, target, true, holder -> {
                ResourceKey<R> key = holder.unwrapKey().orElseThrow();
                WithSource<R, T> old = result.get(key);
                if (old == null || value.replace()) {
                    result.put(key, new WithSource<>(value.value(), target));
                    return;
                }

                DataMapValueMerger<R, T> merger = dataMap instanceof AdvancedDataMapType<R, T, ?> advanced
                        ? advanced.merger()
                        : DataMapValueMerger.defaultMerger();
                T merged = merger.merge(
                        registry, old.source(), old.value(), target, value.value()
                );
                result.put(key, new WithSource<>(merged, target));
            }));
        });

        if (root.has("remove")) {
            applyRemovals(ops, registry, dataMap, root.get("remove"), result);
        }
    }

    private <R, T> void applyRemovals(
            final RegistryOps<JsonElement> ops,
            final Registry<R> registry,
            final DataMapType<R, T> dataMap,
            final JsonElement removals,
            final Map<ResourceKey<R>, WithSource<R, T>> result
    ) {
        if (removals.isJsonObject()) {
            if (!(dataMap instanceof AdvancedDataMapType<R, T, ?> advanced)) {
                throw new IllegalArgumentException("Object removals require an advanced data map");
            }
            removals.getAsJsonObject().entrySet().forEach(entry -> applyRemoval(
                    ops, registry, dataMap, parseTarget(dataMap.registryKey(), entry.getKey()),
                    decodeRemover(ops, advanced, entry.getValue()), result
            ));
            return;
        }

        for (JsonElement removal : GsonHelper.convertToJsonArray(removals, "remove")) {
            if (removal.isJsonPrimitive()) {
                applyRemoval(
                        ops, registry, dataMap,
                        parseTarget(dataMap.registryKey(), removal.getAsString()),
                        Optional.empty(), result
                );
                continue;
            }

            JsonObject object = GsonHelper.convertToJsonObject(removal, "removal");
            Either<TagKey<R>, ResourceKey<R>> target = parseTarget(
                    dataMap.registryKey(), GsonHelper.getAsString(object, "key")
            );
            Optional<DataMapValueRemover<R, T>> remover = Optional.empty();
            if (object.has("remover")) {
                if (!(dataMap instanceof AdvancedDataMapType<R, T, ?> advanced)) {
                    throw new IllegalArgumentException("Custom removers require an advanced data map");
                }
                remover = decodeRemover(ops, advanced, object.get("remover"));
            }
            applyRemoval(ops, registry, dataMap, target, remover, result);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <R, T> Optional<DataMapValueRemover<R, T>> decodeRemover(
            final RegistryOps<JsonElement> ops,
            final AdvancedDataMapType<R, T, ?> dataMap,
            final JsonElement json
    ) {
        return Optional.of((DataMapValueRemover<R, T>) ((Codec) dataMap.remover())
                .parse(ops, json)
                .getOrThrow(false, message -> DragonSurvival.LOGGER.error(
                        "Could not decode remover in data map [{}]: {}",
                        dataMap.id(), message
                )));
    }

    private <R, T> void applyRemoval(
            final RegistryOps<JsonElement> ops,
            final Registry<R> registry,
            final DataMapType<R, T> dataMap,
            final Either<TagKey<R>, ResourceKey<R>> target,
            final Optional<DataMapValueRemover<R, T>> remover,
            final Map<ResourceKey<R>, WithSource<R, T>> result
    ) {
        resolve(registry, target, false, holder -> {
            ResourceKey<R> key = holder.unwrapKey().orElseThrow();
            WithSource<R, T> old = result.get(key);
            if (old == null || remover.isEmpty()) {
                result.remove(key);
                return;
            }

            Optional<T> remaining = remover.get().remove(
                    old.value(), registry, old.source(), holder.value()
            );
            if (remaining.isPresent()) {
                result.put(key, new WithSource<>(remaining.get(), old.source()));
            } else {
                result.remove(key);
            }
        });
    }

    private static <R> Either<TagKey<R>, ResourceKey<R>> parseTarget(
            final ResourceKey<Registry<R>> registryKey,
            final String value
    ) {
        if (value.startsWith("#")) {
            return Either.left(TagKey.create(registryKey, new ResourceLocation(value.substring(1))));
        }
        return Either.right(ResourceKey.create(registryKey, new ResourceLocation(value)));
    }

    private static <R> void resolve(
            final Registry<R> registry,
            final Either<TagKey<R>, ResourceKey<R>> target,
            final boolean required,
            final Consumer<Holder<R>> consumer
    ) {
        if (target.left().isPresent()) {
            registry.getTagOrEmpty(target.left().orElseThrow()).forEach(consumer);
            return;
        }

        ResourceKey<R> key = target.right().orElseThrow();
        Optional<Holder.Reference<R>> holder = registry.getHolder(key);
        if (holder.isPresent()) {
            consumer.accept(holder.get());
        } else if (required) {
            DragonSurvival.LOGGER.error(
                    "Object [{}] specified in data map for registry [{}] does not exist",
                    key.location(), registry.key().location()
            );
        }
    }

    private static <T> Codec<MapEntry<T>> mapEntryCodec(final Codec<T> valueCodec) {
        Codec<MapEntry<T>> wrapped = RecordCodecBuilder.create(instance -> instance.group(
                valueCodec.fieldOf("value").forGetter(MapEntry::value),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(MapEntry::replace)
        ).apply(instance, MapEntry::new));
        return Codec.either(wrapped, valueCodec).xmap(
                either -> either.map(value -> value, value -> new MapEntry<>(value, false)),
                entry -> entry.replace() ? Either.left(entry) : Either.right(entry.value())
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void apply(final Map<DataMapType<?, ?>, Map<?, ?>> loaded) {
        loaded.forEach((dataMap, values) -> ((DataMapType) dataMap).replaceValues(values));
        afterApply.run();
    }

    private record MapEntry<T>(T value, boolean replace) {}

    private record WithSource<R, T>(
            T value,
            Either<TagKey<R>, ResourceKey<R>> source
    ) {}
}
