package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DragonPartLoader extends SimpleJsonResourceReloadListener {
    public static final Map<ResourceKey<DragonSpecies>, Map<SkinLayer, List<DragonPart>>> DRAGON_PARTS = new HashMap<>();

    private static final int NAMESPACE = 0;
    private static final int SPECIES = 1;
    private static final int PART = 2;

    public DragonPartLoader() {
        super(new Gson(), "skin/parts");
    }

    @Override
    protected void apply(final @NotNull Map<ResourceLocation, JsonElement> map, @NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler) {
        map.forEach((location, value) -> value.getAsJsonArray().forEach(element -> {
            String[] elements = location.getPath().split("/");

            if (elements.length != 3) {
                DragonSurvival.LOGGER.error("The parts need to be stored as '<namespace>/<species>/*.json' - [{}] is invalid", location);
                return;
            }

            ResourceKey<DragonSpecies> dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, DragonSurvival.location(elements[NAMESPACE], elements[SPECIES]));
            SkinLayer layer = SkinLayer.valueOf(elements[PART].toUpperCase(Locale.ENGLISH));

            DRAGON_PARTS.computeIfAbsent(dragonSpecies, key -> new HashMap<>()).computeIfAbsent(layer, key -> new ArrayList<>()).add(DragonPart.load(element.getAsJsonObject()));
        }));
    }
}
