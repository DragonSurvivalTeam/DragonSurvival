package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.CustomPredicates;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.EntityCheckPredicate;
import by.dragonsurvivalteam.dragonsurvival.mixins.RegistryOpsAccess;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;

import java.util.function.Supplier;

public class DSSubPredicates {
    // FIXME 1.22 :: these should probably be moved / adjusted to LootItemConditions
    //  since they are not actual "entity" specific predicates. just extensions of the predicate functionality
    public static final EntitySubPredicate.Type DRAGON_PREDICATE = json -> deserialize(DragonPredicate.CODEC, json);
    public static final EntitySubPredicate.Type ENTITY_CHECK_PREDICATE = json -> deserialize(EntityCheckPredicate.CODEC, json);
    public static final EntitySubPredicate.Type CUSTOM_PREDICATES = json -> deserialize(CustomPredicates.CODEC, json);

    private static final ThreadLocal<DynamicOps<?>> CODEC_OPS = new ThreadLocal<>();

    public static <T> T withCodecOps(final DynamicOps<?> ops, final Supplier<T> action) {
        DynamicOps<?> previous = CODEC_OPS.get();
        CODEC_OPS.set(ops);

        try {
            return action.get();
        } finally {
            if (previous == null) {
                CODEC_OPS.remove();
            } else {
                CODEC_OPS.set(previous);
            }
        }
    }

    public static <T extends EntitySubPredicate> JsonObject serialize(final MapCodec<T> codec, final T predicate) {
        DataResult<JsonElement> result = codec.codec().encodeStart(jsonOps(), predicate);
        JsonElement encoded = result.result().orElseThrow(() -> codecException(result));

        if (!encoded.isJsonObject()) {
            throw new JsonSyntaxException("Entity sub-predicate codec did not produce a JSON object");
        }

        return encoded.getAsJsonObject();
    }

    private static <T extends EntitySubPredicate> T deserialize(final MapCodec<T> codec, final JsonObject json) {
        DataResult<T> result = codec.codec().parse(jsonOps(), json);
        return result.result().orElseThrow(() -> codecException(result));
    }

    private static JsonSyntaxException codecException(final DataResult<?> result) {
        String message = result.error()
                .map(DataResult.PartialResult::message)
                .orElse("Unknown entity sub-predicate codec error");
        return new JsonSyntaxException(message);
    }

    private static DynamicOps<JsonElement> jsonOps() {
        DynamicOps<?> current = CODEC_OPS.get();

        if (current instanceof RegistryOps<?> registryOps) {
            RegistryOps.RegistryInfoLookup lookup = ((RegistryOpsAccess) registryOps).dragonSurvival$getLookupProvider();
            return RegistryOps.create(JsonOps.INSTANCE, lookup);
        }

        HolderLookup.Provider lookup = DragonSurvival.PROXY == null ? null : DragonSurvival.PROXY.getAccess();
        return lookup == null ? JsonOps.INSTANCE : RegistryOps.create(JsonOps.INSTANCE, lookup);
    }
}
