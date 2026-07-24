package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DefaultPart;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DefaultPartLoader extends SimpleJsonResourceReloadListener {
    public static final String NO_PART = "none";

    private static final Map<ResourceKey<DragonBody>, Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>>>> PARTY_BY_BODY = new HashMap<>();
    private static final Map<ResourceLocation, Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>>>> PARTY_BY_MODEL = new HashMap<>();

    public DefaultPartLoader() {
        super(new Gson(), "skin/default_parts");
    }

    @Override
    protected void apply(@NotNull final Map<ResourceLocation, JsonElement> map, @NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler) {
        PARTY_BY_MODEL.clear();

        map.forEach((location, value) -> {
            DataResult<DefaultPart> result = DefaultPart.CODEC.decode(JsonOps.INSTANCE, value).ifError(DragonSurvival.LOGGER::error).map(Pair::getFirst);

            if (!result.isSuccess()) {
                return;
            }

            DefaultPart part = result.getOrThrow();

            if (part.body().isPresent()) {
                PARTY_BY_BODY.computeIfAbsent(part.body().get(), key -> new HashMap<>())
                        .computeIfAbsent(part.species(), key -> new HashMap<>())
                        .computeIfAbsent(part.stage(), key -> new HashMap<>()).putAll(part.parts());
            } else if (part.model().isPresent()) {
                PARTY_BY_MODEL.computeIfAbsent(part.model().get(), key -> new HashMap<>())
                        .computeIfAbsent(part.species(), key -> new HashMap<>())
                        .computeIfAbsent(part.stage(), key -> new HashMap<>()).putAll(part.parts());
            }
        });
    }

    // TODO :: add a call to also query by body in case people want to define things different defaults for them
    //  idea is body is checked first and then the model if no valid entry was found
    //  this means that 'NO_PART' return might need to be replaced by returning null or sth. like that
    public static @Nullable String getDefaultPartKey(final ResourceKey<DragonSpecies> species, final ResourceKey<DragonStage> stage, final Either<ResourceKey<DragonBody>, ResourceLocation> partsKey, final SkinLayer layer) {
        Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>>> entries = partsKey.map(PARTY_BY_BODY::get, PARTY_BY_MODEL::get);

        if (entries == null) {
            return NO_PART;
        }

        Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>> perSpecies = entries.get(species);

        if (perSpecies == null) {
            return NO_PART;
        }

        HashMap<SkinLayer, String> perStage = perSpecies.get(stage);

        if (perStage == null) {
            return NO_PART;
        }

        return perStage.getOrDefault(layer, NO_PART);
    }
}
