package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DietEntryCache {
    // In singleplayer '/reload' while having the species screen did not cause any multithreading issues
    private static final Map<ResourceKey<DragonSpecies>, Map<Item, FoodProperties>> CACHE = new HashMap<>();

    public static void rebuild() {
        CACHE.clear();
        DSDataMaps.DIET_ENTRIES.values().forEach(
                (key, diet) -> CACHE.put(key, DietEntry.map(diet == null ? List.of() : diet))
        );
    }

    public static boolean isEmpty(final Holder<DragonSpecies> species) {
        return CACHE.computeIfAbsent(species.unwrapKey().orElseThrow(), key -> generate(species)).isEmpty();
    }

    public static @Nullable FoodProperties getDiet(final Holder<DragonSpecies> species, final Item item) {
        return CACHE.computeIfAbsent(species.unwrapKey().orElseThrow(), key -> generate(species)).get(item);
    }

    public static List<Item> getDietItems(final Holder<DragonSpecies> species) {
        return List.copyOf(CACHE.computeIfAbsent(species.unwrapKey().orElseThrow(), key -> generate(species)).keySet());
    }

    private static Map<Item, FoodProperties> generate(final Holder<DragonSpecies> species) {
        List<DietEntry> diet = DSDataMaps.DIET_ENTRIES.get(species);
        return DietEntry.map(diet == null ? List.of() : diet);
    }
}
