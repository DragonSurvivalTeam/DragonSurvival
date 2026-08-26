package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements.DSAdvancements;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.compat.CreateDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.compat.SilentGemsDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.BodyIconProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.DietEntryProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.DragonBeaconDataProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.EndPlatformProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.StageResourceProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks.AncientDatapacks;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks.NoPenaltiesAbilityProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks.NoPenaltiesPenaltyProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks.UnlockWingsDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonAbilityTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonBodyTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonPenaltyTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEffectTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEnchantmentTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSPoiTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSProfessionTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmoteSet;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmoteSets;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalties;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = DragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    private static final String ANCIENT_STAGE_DATAPACK = "ancient_stage";
    private static final String ANCIENT_STAGE_DATAPACK_NO_CRUSHING = "ancient_stage_no_crushing";

    @Translation(comments = "Adds the Ancient dragon stage to Dragon Survival")
    private static final String ANCIENT_STAGE_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + ANCIENT_STAGE_DATAPACK);

    @Translation(comments = "Adds the Ancient stage (with crushing disabled)")
    private static final String ANCIENT_STAGE_DATAPACK_DESCRIPTION_NO_CRUSHING = Translation.Type.GUI.wrap("datapack." + ANCIENT_STAGE_DATAPACK_NO_CRUSHING);

    private static final String UNLOCK_WINGS_DATAPACK = "unlock_wings";

    @Translation(comments = "Automatically unlocks flight and prevents the ender dragon from disabling them")
    private static final String UNLOCK_WINGS_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + UNLOCK_WINGS_DATAPACK);

    private static final String NO_PENALTIES_DATAPACK = "no_penalties";

    @Translation(comments = "Removes all penalties from the base dragon types")
    private static final String NO_PENALTIES_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + NO_PENALTIES_DATAPACK);

    // --- Compatibility --- //

    private static final String SILENT_GEMS_DATAPACK = ModID.SILENTGEMS.value();

    @Translation(comments = "Adds loot tables to the Silent Gems treasures")
    private static final String SILENT_GEMS_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + SILENT_GEMS_DATAPACK);

    private static final String CREATE_DATAPACK = ModID.CREATE.value();

    @Translation(comments = "Adds loot tables to the Create treasures")
    private static final String CREATE_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + CREATE_DATAPACK);

    @SubscribeEvent
    public static void generateData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> vanillaLookup = event.getLookupProvider();

        // Client
        generator.addProvider(event.includeClient(), new DataBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new DataItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new DataSpriteSourceProvider(output, helper));
        generator.addProvider(event.includeClient(), new DSLanguageProvider(output, vanillaLookup, "en_us"));

        // Server
        LootTableProvider.SubProviderEntry blockLootTableSubProvider = new LootTableProvider.SubProviderEntry(() -> new BlockLootTableSubProvider(vanillaLookup.join()), LootContextParamSets.BLOCK);
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) lootTableOutput -> new LootTableProvider(lootTableOutput, Collections.emptySet(), List.of(blockLootTableSubProvider)));

        if (event.includeServer()) {
            addSilentGemsLootTables(generator, vanillaLookup);
            addCreateLootTables(generator, vanillaLookup);
        }

        // built-in registries
        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(Registries.DAMAGE_TYPE, DSDamageTypes::registerDamageTypes);
        builder.add(DragonEmoteSet.REGISTRY, DragonEmoteSets::registerEmoteSets);
        builder.add(DragonBody.REGISTRY, DragonBodies::registerBodies);
        builder.add(DragonStage.REGISTRY, DragonStages::registerStages);
        builder.add(DragonAbility.REGISTRY, DragonAbilities::registerAbilities);
        builder.add(ProjectileData.REGISTRY, Projectiles::registerProjectiles);
        builder.add(DragonPenalty.REGISTRY, DragonPenalties::registerPenalties);
        builder.add(DragonSpecies.REGISTRY, BuiltInDragonSpecies::registerTypes);
        DatapackBuiltinEntriesProvider datapackProvider = new DatapackBuiltinEntriesProvider(output, vanillaLookup, builder, Set.of(DragonSurvival.MODID));
        generator.addProvider(event.includeServer(), datapackProvider);

        // Update the lookup provider with our registries
        CompletableFuture<HolderLookup.Provider> lookup = datapackProvider.getRegistryProvider();

        // Handle additional datapacks
        addAncientStageDatapack(generator, lookup);
        addAncientStageDatapackNoCrushing(generator, lookup);
        addUnlockWingsDatapack(generator, lookup);
        addNoPenaltiesDatapack(generator, lookup, helper);

        BlockTagsProvider blockTagsProvider = new DSBlockTags(output, lookup, helper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new DSItemTags(output, lookup, blockTagsProvider.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new DSDamageTypeTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSEntityTypeTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSEffectTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSPoiTypeTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSProfessionTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSEnchantmentTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSDragonBodyTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSDragonAbilityTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSDragonPenaltyTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSDragonSpeciesTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DietEntryProvider(output, lookup));
        generator.addProvider(event.includeServer(), new EndPlatformProvider(output, lookup));
        generator.addProvider(event.includeServer(), new DragonBeaconDataProvider(output, lookup));
        generator.addProvider(event.includeServer(), new StageResourceProvider(output, lookup));
        generator.addProvider(event.includeServer(), new BodyIconProvider(output, lookup));

        generator.addProvider(event.includeServer(), new DataBlockModelProvider(output, helper));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookup, List.of(new DSAdvancements())));

        generator.addProvider(event.includeServer(), new DSRecipes(output));
    }

    @SubscribeEvent
    public static void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            registerResourcePack(event, Component.literal("DS - Draconized Armor"), "resourcepacks/draconized_armor");
            registerResourcePack(event, Component.literal("DS - Dark GUI"), "resourcepacks/ds_dark_gui");
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            registerDataPack(event, Component.literal("DS - Ancient Dragons"), ANCIENT_STAGE_DATAPACK, PackSource.DEFAULT, false);
            registerDataPack(event, Component.literal("DS - Unlock Wings"), UNLOCK_WINGS_DATAPACK, PackSource.DEFAULT, false);

            if (ModID.SILENTGEMS.isLoaded()) {
                registerDataPack(event, Component.literal("DS - Silent Gems"), SILENT_GEMS_DATAPACK, PackSource.BUILT_IN, true);
            }

            if (ModID.CREATE.isLoaded()) {
                registerDataPack(event, Component.literal("DS - Create"), CREATE_DATAPACK, PackSource.BUILT_IN, true);
            }

            // Feature datapacks (things that are not enabled by default)
            registerDataPack(event, Component.literal("DS - Ancient (no crushing)"), ANCIENT_STAGE_DATAPACK_NO_CRUSHING, PackSource.FEATURE, false);
            registerDataPack(event, Component.literal("DS - No Penalties"), NO_PENALTIES_DATAPACK, PackSource.FEATURE, false);
        }
    }

    private static void registerResourcePack(final AddPackFindersEvent event, final MutableComponent name, final String folder) {
        registerPack(event, name, folder, PackSource.BUILT_IN, false);
    }

    private static void registerDataPack(final AddPackFindersEvent event, final MutableComponent name, final String datapack, final PackSource source, final boolean alwaysActive) {
        registerPack(event, name, "data/" + DragonSurvival.MODID + "/datapacks/" + datapack, source, alwaysActive);
    }

    private static void registerPack(final AddPackFindersEvent event, final MutableComponent name, final String folder, final PackSource source, final boolean alwaysActive) {
        Path path = ModList.get().getModFileById(DragonSurvival.MODID).getFile().findResource(folder.split("/"));
        String id = DragonSurvival.MODID + "/" + folder;

        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate(id, name, alwaysActive, packId -> new PathPackResources(packId, path, true), event.getPackType(), Pack.Position.TOP, source);
            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }

    private static void addAncientStageDatapack(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        BuiltinDatapackGenerator datapack = getBuiltinDatapack(generator, ANCIENT_STAGE_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(ANCIENT_STAGE_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));

        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(DragonStage.REGISTRY, AncientDatapacks::registerAncient);

        datapack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, lookup, builder, Set.of(DragonSurvival.MODID)));
    }

    private static void addAncientStageDatapackNoCrushing(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        BuiltinDatapackGenerator datapack = getBuiltinDatapack(generator, ANCIENT_STAGE_DATAPACK_NO_CRUSHING);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(ANCIENT_STAGE_DATAPACK_DESCRIPTION_NO_CRUSHING), FeatureFlagSet.of()));

        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(DragonStage.REGISTRY, AncientDatapacks::registerAncientNoCrushing);

        datapack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, lookup, builder, Set.of(DragonSurvival.MODID)));
    }

    private static void addNoPenaltiesDatapack(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        BuiltinDatapackGenerator datapack = getBuiltinDatapack(generator, NO_PENALTIES_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(NO_PENALTIES_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));
        datapack.addProvider(output -> new NoPenaltiesPenaltyProvider(output, DragonPenalty.REGISTRY, lookup, DragonSurvival.MODID, helper));
        datapack.addProvider(output -> new NoPenaltiesAbilityProvider(output, DragonAbility.REGISTRY, lookup, DragonSurvival.MODID, helper));
    }

    private static void addUnlockWingsDatapack(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        BuiltinDatapackGenerator datapack = getBuiltinDatapack(generator, UNLOCK_WINGS_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(UNLOCK_WINGS_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));

        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(DragonAbility.REGISTRY, UnlockWingsDatapack::register);

        datapack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, lookup, builder, Set.of(DragonSurvival.MODID)));
    }

    // --- Compatibility --- //

    private static void addSilentGemsLootTables(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        BuiltinDatapackGenerator datapack = getBuiltinDatapack(generator, SILENT_GEMS_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(SILENT_GEMS_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));
        LootTableProvider.SubProviderEntry subProvider = new LootTableProvider.SubProviderEntry(() -> new SilentGemsDatapack(lookup.join()), LootContextParamSets.BLOCK);
        datapack.addProvider(output -> new SilentGemsDatapack.Provider(output, Collections.emptySet(), List.of(subProvider), lookup));
    }

    private static void addCreateLootTables(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        BuiltinDatapackGenerator datapack = getBuiltinDatapack(generator, CREATE_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(CREATE_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));
        LootTableProvider.SubProviderEntry subProvider = new LootTableProvider.SubProviderEntry(() -> new CreateDatapack(lookup.join()), LootContextParamSets.BLOCK);
        datapack.addProvider(output -> new CreateDatapack.Provider(output, Collections.emptySet(), List.of(subProvider), lookup));
    }
    private static BuiltinDatapackGenerator getBuiltinDatapack(final DataGenerator generator, final String name) {
        PackOutput output = new PackOutput(
                generator.getPackOutput().getOutputFolder(PackOutput.Target.DATA_PACK)
                        .resolve(DragonSurvival.MODID)
                        .resolve("datapacks")
                        .resolve(name)
        );
        return new BuiltinDatapackGenerator(generator, output, name);
    }

    private record BuiltinDatapackGenerator(DataGenerator generator, PackOutput output, String name) {
        private <T extends DataProvider> void addProvider(final DataProvider.Factory<T> factory) {
            generator.addProvider(true, new NamedDataProvider(name, factory.create(output)));
        }
    }

    private record NamedDataProvider(String packName, DataProvider delegate) implements DataProvider {
        @Override
        public CompletableFuture<?> run(final CachedOutput cache) {
            return delegate.run(cache);
        }

        @Override
        public String getName() {
            return packName + "/" + delegate.getName();
        }
    }
}
