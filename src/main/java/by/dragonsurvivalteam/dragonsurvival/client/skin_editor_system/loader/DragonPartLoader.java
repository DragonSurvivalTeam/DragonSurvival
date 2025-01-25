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
    private static final int LAYER = 0;

    public DragonPartLoader() {
        super(new Gson(), "skin/parts");
    }

    @Override
    protected void apply(final @NotNull Map<ResourceLocation, JsonElement> map, @NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler) {
        DRAGON_PARTS.clear();

        map.forEach((location, value) -> {
            String[] elements = location.getPath().split("/", 2);

            if (elements.length != 2) {
                DragonSurvival.LOGGER.error("The dragon parts need to be stored as '<layer>/*.json' - [{}] is invalid", location);
                return;
            }

            try {
                SkinLayer layer = SkinLayer.valueOf(elements[LAYER].toUpperCase(Locale.ENGLISH));
                DragonPart part = DragonPart.load(value.getAsJsonObject());

                // TODO :: if no species are specific -> throw into generic map?
                for (ResourceKey<DragonSpecies> species : part.applicableSpecies()) {
                    DRAGON_PARTS.computeIfAbsent(species, key -> new HashMap<>()).computeIfAbsent(layer, key -> new ArrayList<>()).add(part);
                }
            } catch (IllegalArgumentException exception) {
                DragonSurvival.LOGGER.error("[{}] is not a valid layer for the dragon part - [{}] is invalid", elements[LAYER], location);
            }
        });
    }
}
