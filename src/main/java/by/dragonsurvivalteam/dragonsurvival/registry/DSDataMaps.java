package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.EndPlatform;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryMerger;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryRemover;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.StageResourceRemover;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSDataMaps {
    public static final AdvancedDataMapType<DragonSpecies, List<DietEntry>, DietEntryRemover> DIET_ENTRIES = AdvancedDataMapType.builder(DragonSurvival.res("diet_entries"), DragonSpecies.REGISTRY, DietEntry.CODEC.listOf())
            .merger(new DietEntryMerger()).remover(DietEntryRemover.CODEC).synced(DietEntry.CODEC.listOf(), true).build();

    public static final AdvancedDataMapType<DragonSpecies, Map<ResourceKey<DragonStage>, StageResources.StageResource>, StageResourceRemover> STAGE_RESOURCES = AdvancedDataMapType.builder(DragonSurvival.res("stage_resources"), DragonSpecies.REGISTRY, StageResources.CODEC)
            .merger(DataMapValueMerger.mapMerger()).remover(StageResourceRemover.CODEC).synced(StageResources.CODEC, true).build();

    public static final DataMapType<DragonSpecies, EndPlatform> END_PLATFORMS = DataMapType.builder(DragonSurvival.res("end_platforms"), DragonSpecies.REGISTRY, EndPlatform.CODEC).build();

    public static final DataMapType<DragonSpecies, DragonBeaconData> DRAGON_BEACON_DATA = DataMapType.builder(DragonSurvival.res("dragon_beacon_data"), DragonSpecies.REGISTRY, DragonBeaconData.CODEC).build();

    @SubscribeEvent
    public static void register(final RegisterDataMapTypesEvent event) {
        event.register(DIET_ENTRIES);
        event.register(STAGE_RESOURCES);
        event.register(END_PLATFORMS);
        event.register(DRAGON_BEACON_DATA);
    }
}
