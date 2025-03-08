package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DragonPartLoader extends SimpleJsonResourceReloadListener {
    public static final Map<ResourceKey<DragonSpecies>, Map<SkinLayer, Map<String, DragonPart>>> DRAGON_PARTS = new HashMap<>();
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
                    DRAGON_PARTS.computeIfAbsent(species, key -> new HashMap<>()).computeIfAbsent(layer, key -> new HashMap<>()).put(part.key(), part);
                }
            } catch (IllegalArgumentException exception) {
                DragonSurvival.LOGGER.error("[{}] is not a valid layer for the dragon part - [{}] is invalid", elements[LAYER], location);
            }
        });
    }

    public static @Nullable DragonPart getDragonPart(final SkinLayer layer, final ResourceKey<DragonSpecies> species, @Nullable final Holder<DragonBody> body, final String partKey) {
        return getDragonParts(layer, species, body).get(partKey);
    }

    public static Map<String, DragonPart> getDragonParts(final SkinLayer layer, final ResourceKey<DragonSpecies> species, @Nullable final Holder<DragonBody> body) {
        if (Objects.equals(layer.name, "Extra") && layer != SkinLayer.EXTRA) {
            return getDragonParts(SkinLayer.EXTRA, species, body);
        }

        Map<SkinLayer, Map<String, DragonPart>> layers = DragonPartLoader.DRAGON_PARTS.get(species);

        if (layers == null) {
            DragonSurvival.LOGGER.error("Part type map missing for dragon type {}", species);
            return Collections.emptyMap();
        }

        Map<String, DragonPart> parts = layers.get(layer);

        if (parts == null) {
            DragonSurvival.LOGGER.error("Dragon part layer {} not found", layer.name);
            return Collections.emptyMap();
        }

        if (parts.isEmpty()) {
            DragonSurvival.LOGGER.error("Dragon part layer {} is empty", layer.name);
            return Collections.emptyMap();
        }

        if (body != null) {
            return parts.keySet().stream().filter(key -> {
                DragonPart part = parts.get(key);
                return part.applicableBodies().isEmpty() && body.value().model() == DragonBody.DEFAULT_MODEL || part.applicableBodies().contains(body.getKey());
            }).collect(Collectors.toMap(key -> key, parts::get));
        }

        return parts;
    }

    public static Map<String, DragonPart> getDragonParts(final Player player, final SkinLayer layer) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        return getDragonParts(layer, handler.speciesKey(), handler.body());
    }
}
