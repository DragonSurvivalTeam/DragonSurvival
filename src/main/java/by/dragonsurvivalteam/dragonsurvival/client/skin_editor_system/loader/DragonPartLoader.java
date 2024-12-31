package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DragonPartLoader extends SimpleJsonResourceReloadListener {
    public static final Map<ResourceKey<DragonSpecies>, Map<EnumSkinLayer, List<DragonPart>>> DRAGON_PARTS = new HashMap<>();

    public DragonPartLoader() {
        super(new Gson(), "skin/parts");
    }

    @Override
    protected void apply(final @NotNull Map<ResourceLocation, JsonElement> map, @NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler) {
        map.forEach((location, value) -> value.getAsJsonArray().forEach(element -> {
            // Location path is without the specified directory
            // The format is expected to be '<dragon_species>/<part>.json'
            String[] elements = location.getPath().split("/");

            ResourceKey<DragonSpecies> dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, DragonSurvival.location(location.getNamespace(), elements[0]));
            EnumSkinLayer layer = EnumSkinLayer.valueOf(elements[1].toUpperCase(Locale.ENGLISH));

            DRAGON_PARTS.computeIfAbsent(dragonSpecies, key -> new HashMap<>()).computeIfAbsent(layer, key -> new ArrayList<>()).add(DragonPart.load(element.getAsJsonObject()));
        }));
    }
}
