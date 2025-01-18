package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryRemover;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSDataMaps {
    public static final AdvancedDataMapType<DragonSpecies, List<DietEntry>, DietEntryRemover> DIET_ENTRY_MAP = AdvancedDataMapType.builder(DragonSurvival.res("diet_entries"), DragonSpecies.REGISTRY, DietEntry.CODEC.listOf())
            .merger(DataMapValueMerger.listMerger()).remover(DietEntryRemover.CODEC).synced(DietEntry.CODEC.listOf(), true).build();

    @SubscribeEvent
    public static void register(final RegisterDataMapTypesEvent event) {
        event.register(DIET_ENTRY_MAP);
    }
}
