package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class DietEntryCache {
    // In singleplayer '/reload' while having the species screen did not cause any multithreading issues
    private static final Map<ResourceKey<DragonSpecies>, Map<Item, FoodProperties>> CACHE = new HashMap<>();

    @SubscribeEvent
    public static void buildCache(final DataMapsUpdatedEvent event) {
        event.ifRegistry(DragonSpecies.REGISTRY, registry -> {
            registry.getDataMap(DSDataMaps.DIET_ENTRY_MAP).forEach((key, diet) -> CACHE.put(key, DietEntry.map(diet == null ? List.of() : diet)));
        });
    }

    public static @Nullable FoodProperties getDiet(final Holder<DragonSpecies> species, final Item item) {
        return CACHE.computeIfAbsent(species.getKey(), key -> {
            List<DietEntry> diet = species.getData(DSDataMaps.DIET_ENTRY_MAP);
            return DietEntry.map(diet == null ? List.of() : diet);
        }).get(item);
    }

    public static List<Item> getDietItems(final Holder<DragonSpecies> species) {
        return List.copyOf(CACHE.computeIfAbsent(species.getKey(), key -> {
            List<DietEntry> diet = species.getData(DSDataMaps.DIET_ENTRY_MAP);
            return DietEntry.map(diet == null ? List.of() : diet);
        }).keySet());
    }
}
