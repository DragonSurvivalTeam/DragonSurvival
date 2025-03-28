package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.mixins.BaseMappedRegistryAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

// TODO :: Remove when https://github.com/neoforged/NeoForge/issues/1867 is fixed
@EventBusSubscriber
public class DataMapBandaid {
    public static final Map<ResourceKey<? extends Registry<?>>, Map<DataMapType<?, ?>, Map<ResourceKey<?>, ?>>> BANDAID = new HashMap<>();

    @SubscribeEvent
    public static void storeData(final DataMapsUpdatedEvent event) {
        if (event.getRegistry() instanceof BaseMappedRegistryAccess access) {
            if (event.getRegistryKey() != DragonSpecies.REGISTRY && event.getRegistryKey() != DragonBody.REGISTRY) {
                // Avoid storing unneeded data
                return;
            }

            var data = access.dragonSurvival$getDataMaps();

            if (data.isEmpty()) {
                return;
            }

            BANDAID.put(event.getRegistryKey(), data);
        }
    }
}
