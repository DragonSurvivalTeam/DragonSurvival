package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
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
    public static final Map<ResourceKey<DragonType>, Map<ResourceKey<DragonStage>, HashMap<EnumSkinLayer, String>>> DEFAULT_PARTS = new HashMap<>();
    public static final String NO_PART = "none";

    public DefaultPartLoader() {
        super(new Gson(), "skin/default_parts");
    }

    @Override
    protected void apply(@NotNull final Map<ResourceLocation, JsonElement> map, @NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler) {
        map.forEach((type, value) -> {
            // Location path is without the specified directory
            ResourceKey<DragonType> dragonType = ResourceKey.create(DragonType.REGISTRY, type);
            JsonObject dragonStageMap = value.getAsJsonObject();

            for (String stage : dragonStageMap.keySet()) {
                JsonObject partMap = dragonStageMap.get(stage).getAsJsonObject();

                for (String part : partMap.keySet()) {
                    ResourceKey<DragonStage> dragonStage = ResourceKey.create(DragonStage.REGISTRY, ResourceLocation.parse(stage));
                    DEFAULT_PARTS.computeIfAbsent(dragonType, key -> new HashMap<>()).computeIfAbsent(dragonStage, key -> new HashMap<>()).put(EnumSkinLayer.valueOf(part.toUpperCase(Locale.ENGLISH)), partMap.get(part).getAsString());
                }
            }
        });
    }

    public static String getDefaultPartKey(final ResourceKey<DragonType> type, final ResourceKey<DragonStage> stage, final EnumSkinLayer layer) {
        HashMap<EnumSkinLayer, String> partMap = DEFAULT_PARTS.get(type).get(stage);
        String partKey = partMap != null ? partMap.getOrDefault(layer, NO_PART) : NO_PART;

        if (layer == EnumSkinLayer.BASE && partKey.equals(NO_PART)) {
            // Without a base the dragon will be invisible
            return DragonPartLoader.DRAGON_PARTS.get(type).get(layer).getFirst().key();
        }

        return partKey;
    }
}
