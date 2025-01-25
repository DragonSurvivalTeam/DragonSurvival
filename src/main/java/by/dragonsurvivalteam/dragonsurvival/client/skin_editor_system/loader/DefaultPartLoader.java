package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DefaultPartLoader extends SimpleJsonResourceReloadListener {
    public static final String NO_PART = "none";

    private static final Map</* Model */ ResourceLocation, Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, HashMap<SkinLayer, /* Part key */ String>>>> DEFAULT_PARTS = new HashMap<>();
    private static final int MODEL_NAMESPACE = 0;
    private static final int MODEL = 1;
    private static final int SPECIES_NAMESPACE = 2;
    private static final int SPECIES = 3;

    public DefaultPartLoader() {
        super(new Gson(), "skin/default_parts");
    }

    @Override
    protected void apply(@NotNull final Map<ResourceLocation, JsonElement> map, @NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler) {
        DEFAULT_PARTS.clear();

        map.forEach((location, value) -> {
            String[] elements = location.getPath().split("/");

            if (elements.length != 4) {
                DragonSurvival.LOGGER.error("The default parts need to be stored as '<namespace>/<model>/<namespace>/<species>.json' - [{}] is invalid", location);
                return;
            }

            ResourceLocation model = ResourceLocation.fromNamespaceAndPath(elements[MODEL_NAMESPACE], elements[MODEL]);
            ResourceLocation species = ResourceLocation.fromNamespaceAndPath(elements[SPECIES_NAMESPACE], elements[SPECIES]);

            ResourceKey<DragonSpecies> dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, species);
            JsonObject dragonStageMap = value.getAsJsonObject();

            for (String stage : dragonStageMap.keySet()) {
                JsonObject partMap = dragonStageMap.get(stage).getAsJsonObject();

                for (String part : partMap.keySet()) {
                    ResourceKey<DragonStage> dragonStage = ResourceKey.create(DragonStage.REGISTRY, ResourceLocation.parse(stage));
                    DEFAULT_PARTS.computeIfAbsent(model, key -> new HashMap<>())
                            .computeIfAbsent(dragonSpecies, key -> new HashMap<>())
                            .computeIfAbsent(dragonStage, key -> new HashMap<>())
                            .put(SkinLayer.valueOf(part.toUpperCase(Locale.ENGLISH)), partMap.get(part).getAsString());
                }
            }
        });
    }

    public static String getDefaultPartKey(final ResourceKey<DragonSpecies> species, final ResourceKey<DragonStage> stage, final ResourceLocation customModel, final SkinLayer layer) {
        if (!DEFAULT_PARTS.containsKey(customModel)) {
            return NO_PART;
        }

        if (!DEFAULT_PARTS.get(customModel).containsKey(species)) {
            return NO_PART;
        }

        if (!DEFAULT_PARTS.get(customModel).get(species).containsKey(stage)) {
            return NO_PART;
        }

        HashMap<SkinLayer, String> partMap = DEFAULT_PARTS.get(customModel).get(species).get(stage);
        String partKey = partMap != null ? partMap.getOrDefault(layer, NO_PART) : NO_PART;

        if (layer == SkinLayer.BASE && partKey.equals(NO_PART)) {
            // Without a base the dragon will be invisible
            return DragonPartLoader.DRAGON_PARTS.get(species).get(layer).getFirst().key();
        }

        return partKey;
    }
}
