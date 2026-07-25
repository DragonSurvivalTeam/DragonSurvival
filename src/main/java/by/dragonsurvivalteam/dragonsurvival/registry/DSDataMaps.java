package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.EndPlatform;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.BodyIconRemover;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.BodyIcons;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DataMapReloadListener;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryMerger;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryRemover;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.StageResourceRemover;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncDataMaps;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.datamaps.AdvancedDataMapType;
import net.minecraftforge.registries.datamaps.DataMapType;
import net.minecraftforge.registries.datamaps.DataMapValueMerger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class DSDataMaps {
    // --- Dragon Species --- //

    public static final AdvancedDataMapType<DragonSpecies, List<DietEntry>, DietEntryRemover> DIET_ENTRIES = AdvancedDataMapType.builder(DragonSurvival.res("diet_entries"), DragonSpecies.REGISTRY, DietEntry.CODEC.listOf())
            .merger(new DietEntryMerger()).remover(DietEntryRemover.CODEC).synced(DietEntry.CODEC.listOf(), true).build();

    // TODO :: make it a client resource? server never interacts with it
    public static final AdvancedDataMapType<DragonSpecies, Map<ResourceKey<DragonStage>, StageResources.StageResource>, StageResourceRemover> STAGE_RESOURCES = AdvancedDataMapType.builder(DragonSurvival.res("stage_resources"), DragonSpecies.REGISTRY, StageResources.CODEC)
            .merger(DataMapValueMerger.mapMerger()).remover(StageResourceRemover.CODEC).synced(StageResources.CODEC, true).build();

    public static final DataMapType<DragonSpecies, EndPlatform> END_PLATFORMS = DataMapType.builder(DragonSurvival.res("end_platforms"), DragonSpecies.REGISTRY, EndPlatform.CODEC).build();

    public static final DataMapType<DragonSpecies, DragonBeaconData> DRAGON_BEACON_DATA = DataMapType.builder(DragonSurvival.res("dragon_beacon_data"), DragonSpecies.REGISTRY, DragonBeaconData.CODEC).build();

    // --- Dragon Body --- //

    // TODO :: make it a client resource? server never interacts with it
    public static final AdvancedDataMapType<DragonBody, Map<ResourceKey<DragonSpecies>, ResourceLocation>, BodyIconRemover> BODY_ICONS = AdvancedDataMapType.builder(DragonSurvival.res("body_icons"), DragonBody.REGISTRY, BodyIcons.CODEC)
            .merger(DataMapValueMerger.mapMerger()).remover(BodyIconRemover.CODEC).synced(BodyIcons.CODEC, true).build();

    private static final List<DataMapType<?, ?>> ALL = List.of(
            DIET_ENTRIES,
            STAGE_RESOURCES,
            END_PLATFORMS,
            DRAGON_BEACON_DATA,
            BODY_ICONS
    );

    @SubscribeEvent
    public static void addReloadListener(final AddReloadListenerEvent event) {
        event.addListener(new DataMapReloadListener(
                event.getRegistryAccess(), ALL, DietEntryCache::rebuild
        ));
    }

    @SubscribeEvent
    public static void sync(final OnDatapackSyncEvent event) {
        SyncDataMaps packet = new SyncDataMaps(encodeSynced(event.getPlayerList().getServer().registryAccess()));
        if (event.getPlayer() == null) {
            PacketDistributor.sendToAllPlayers(packet);
        } else {
            PacketDistributor.sendToPlayer(event.getPlayer(), packet);
        }
    }

    public static CompoundTag encodeSynced(final RegistryAccess registryAccess) {
        CompoundTag result = new CompoundTag();
        ALL.stream()
                .filter(dataMap -> dataMap.networkCodec() != null)
                .forEach(dataMap -> encodeUnchecked(result, registryAccess, dataMap));
        return result;
    }

    public static void applySynced(final CompoundTag data, final RegistryAccess registryAccess) {
        ALL.stream()
                .filter(dataMap -> dataMap.networkCodec() != null)
                .forEach(dataMap -> decodeUnchecked(data, registryAccess, dataMap));
        DietEntryCache.rebuild();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void encodeUnchecked(
            final CompoundTag result,
            final RegistryAccess registryAccess,
            final DataMapType<?, ?> dataMap
    ) {
        encode(result, registryAccess, (DataMapType) dataMap);
    }

    private static <R, T> void encode(
            final CompoundTag result,
            final RegistryAccess registryAccess,
            final DataMapType<R, T> dataMap
    ) {
        Codec<T> networkCodec = dataMap.networkCodec();
        if (networkCodec == null) {
            return;
        }

        Map<ResourceLocation, T> values = new HashMap<>();
        dataMap.values().forEach((key, value) -> values.put(key.location(), value));
        Tag encoded = Codec.unboundedMap(ResourceLocation.CODEC, networkCodec)
                .encodeStart(RegistryOps.create(NbtOps.INSTANCE, registryAccess), values)
                .getOrThrow(false, message -> DragonSurvival.LOGGER.error(
                        "Could not encode synced data map [{}]: {}", dataMap.id(), message
                ));
        result.put(dataMap.id().toString(), encoded);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void decodeUnchecked(
            final CompoundTag data,
            final RegistryAccess registryAccess,
            final DataMapType<?, ?> dataMap
    ) {
        decode(data, registryAccess, (DataMapType) dataMap);
    }

    private static <R, T> void decode(
            final CompoundTag data,
            final RegistryAccess registryAccess,
            final DataMapType<R, T> dataMap
    ) {
        Codec<T> networkCodec = dataMap.networkCodec();
        Tag encoded = data.get(dataMap.id().toString());
        if (networkCodec == null || encoded == null) {
            dataMap.replaceValues(Map.of());
            return;
        }

        Map<ResourceLocation, T> decoded = Codec.unboundedMap(ResourceLocation.CODEC, networkCodec)
                .parse(RegistryOps.create(NbtOps.INSTANCE, registryAccess), encoded)
                .getOrThrow(false, message -> DragonSurvival.LOGGER.error(
                        "Could not decode synced data map [{}]: {}", dataMap.id(), message
                ));
        Map<ResourceKey<R>, T> values = new HashMap<>();
        decoded.forEach((id, value) -> values.put(ResourceKey.create(dataMap.registryKey(), id), value));
        dataMap.replaceValues(values);
    }
}
