package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.compat.CreateDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.compat.SilentGemsDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.BodyIconProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.DietEntryProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.DragonBeaconDataProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.EndPlatformProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.StageResourceProvider;
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
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.UnlockWingsDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalties;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DataGeneration {
    private static final String ANCIENT_STAGE_DATAPACK = "ancient_stage";

    @Translation(comments = "Adds the Ancient dragon stage to Dragon Survival")
    private static final String ANCIENT_STAGE_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + ANCIENT_STAGE_DATAPACK);

    private static final String UNLOCK_WINGS_DATAPACK = "unlock_wings";

    @Translation(comments = "Automatically unlocks dragon wings and prevents the ender dragon from disabling them in Dragon Survival")
    private static final String UNLOCK_WINGS_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + UNLOCK_WINGS_DATAPACK);

    // --- Compatibility --- //

    private static final String SILENT_GEMS_DATAPACK = ModCheck.SILENTGEMS;

    @Translation(comments = "Adds loot tables to the Silent Gems treasures")
    private static final String SILENT_GEMS_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + SILENT_GEMS_DATAPACK);

    private static final String CREATE_DATAPACK = ModCheck.CREATE;

    @Translation(comments = "Adds loot tables to the Create treasures")
    private static final String CREATE_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + CREATE_DATAPACK);

    @SubscribeEvent
    public static void generateData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        // Client
        generator.addProvider(event.includeClient(), new DataBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new DataItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new DataSpriteSourceProvider(output, lookup, helper));
        generator.addProvider(event.includeClient(), new DSLanguageProvider(output, lookup, "en_us"));

        // Server
        LootTableProvider.SubProviderEntry blockLootTableSubProvider = new LootTableProvider.SubProviderEntry(BlockLootTableSubProvider::new, LootContextParamSets.BLOCK);
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) lootTableOutput -> new LootTableProvider(lootTableOutput, Collections.emptySet(), List.of(blockLootTableSubProvider), event.getLookupProvider()));

        if (event.includeServer()) {
            addSilentGemsLootTables(generator, lookup);
            addCreateLootTables(generator, lookup);
        }

        // built-in registries
        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(Registries.DAMAGE_TYPE, DSDamageTypes::registerDamageTypes);
        builder.add(Registries.ENCHANTMENT, DSEnchantments::registerEnchantments);
        builder.add(DragonEmoteSet.REGISTRY, DragonEmoteSets::registerEmoteSets);
        builder.add(DragonBody.REGISTRY, DragonBodies::registerBodies);
        builder.add(DragonStage.REGISTRY, DragonStages::registerStages);
        builder.add(DragonAbility.REGISTRY, DragonAbilities::registerAbilities);
        builder.add(ProjectileData.REGISTRY, Projectiles::registerProjectiles);
        builder.add(DragonPenalty.REGISTRY, DragonPenalties::registerPenalties);
        builder.add(DragonSpecies.REGISTRY, BuiltInDragonSpecies::registerTypes);
        DatapackBuiltinEntriesProvider datapackProvider = new DatapackBuiltinEntriesProvider(output, lookup, builder, Set.of(DragonSurvival.MODID));
        generator.addProvider(event.includeServer(), datapackProvider);

        // Update the lookup provider with our registries
        lookup = datapackProvider.getRegistryProvider();

        // Handle additional datapacks
        addAncientStageDatapack(generator, lookup);
        addUnlockWingsDatapack(generator, lookup, /* May not be the correct way to handle this */ lookup.join().lookupOrThrow(DragonAbility.REGISTRY));

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
        // TODO :: Re-add this when we update to 1.22
//        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookup, helper, List.of(new DSAdvancements())));

        // Should run last due to doing weird registry things
        generator.addProvider(event.includeServer(), new DSRecipes(output, lookup));
    }

    @SubscribeEvent
    public static void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            registerBuiltinResourcePack(event, Component.literal("DS - Draconized Armor"), "resourcepacks/draconized_armor");
            registerBuiltinResourcePack(event, Component.literal("DS - Dark GUI"), "resourcepacks/ds_dark_gui");
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            registerDataPack(event, Component.literal("DS - Ancient Dragons"), ANCIENT_STAGE_DATAPACK);
            registerDataPack(event, Component.literal("DS - Unlock Wings"), UNLOCK_WINGS_DATAPACK);

            if (ModCheck.isModLoaded(ModCheck.SILENTGEMS)) {
                registerBuiltInDataPack(event, Component.literal("DS - Silent Gems"), SILENT_GEMS_DATAPACK);
            }

            if (ModCheck.isModLoaded(ModCheck.CREATE)) {
                registerBuiltInDataPack(event, Component.literal("DS - Create"), CREATE_DATAPACK);
            }
        }
    }

    private static void registerBuiltinResourcePack(final AddPackFindersEvent event, final MutableComponent name, final String folder) {
        event.addPackFinders(DragonSurvival.res(folder), PackType.CLIENT_RESOURCES, name, PackSource.BUILT_IN, false, Pack.Position.TOP);
    }

    private static void registerDataPack(final AddPackFindersEvent event, final MutableComponent name, final String datapack) {
        // Can only be disabled through '/datapack disable (...)' after world creation
        event.addPackFinders(DragonSurvival.res("data/" + DragonSurvival.MODID + "/datapacks/" + datapack), PackType.SERVER_DATA, name, PackSource.DEFAULT, false, Pack.Position.TOP);
    }

    private static void registerBuiltInDataPack(final AddPackFindersEvent event, final MutableComponent name, final String datapack) {
        event.addPackFinders(DragonSurvival.res("data/" + DragonSurvival.MODID + "/datapacks/" + datapack), PackType.SERVER_DATA, name, PackSource.BUILT_IN, true, Pack.Position.TOP);
    }

    private static void addAncientStageDatapack(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        DataGenerator.PackGenerator datapack = generator.getBuiltinDatapack(true, DragonSurvival.MODID, ANCIENT_STAGE_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(ANCIENT_STAGE_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));
        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(DragonStage.REGISTRY, AncientDatapack::register);
        datapack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, lookup, builder, Set.of(DragonSurvival.MODID)));
    }

    private static void addUnlockWingsDatapack(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup, final HolderLookup.RegistryLookup<DragonAbility> registryLookup) {
        DataGenerator.PackGenerator datapack = generator.getBuiltinDatapack(true, DragonSurvival.MODID, UNLOCK_WINGS_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(UNLOCK_WINGS_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));
        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(DragonAbility.REGISTRY, context -> UnlockWingsDatapack.register(context, registryLookup));
        datapack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, lookup, builder, Set.of(DragonSurvival.MODID)));
    }

    // --- Compatibility --- //

    private static void addSilentGemsLootTables(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        DataGenerator.PackGenerator datapack = generator.getBuiltinDatapack(true, DragonSurvival.MODID, SILENT_GEMS_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(SILENT_GEMS_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));
        LootTableProvider.SubProviderEntry subProvider = new LootTableProvider.SubProviderEntry(SilentGemsDatapack::new, LootContextParamSets.BLOCK);
        datapack.addProvider(output -> new SilentGemsDatapack.Provider(output, Collections.emptySet(), List.of(subProvider), lookup));
    }

    private static void addCreateLootTables(final DataGenerator generator, final CompletableFuture<HolderLookup.Provider> lookup) {
        DataGenerator.PackGenerator datapack = generator.getBuiltinDatapack(true, DragonSurvival.MODID, CREATE_DATAPACK);
        datapack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.translatable(CREATE_DATAPACK_DESCRIPTION), FeatureFlagSet.of()));
        LootTableProvider.SubProviderEntry subProvider = new LootTableProvider.SubProviderEntry(CreateDatapack::new, LootContextParamSets.BLOCK);
        datapack.addProvider(output -> new CreateDatapack.Provider(output, Collections.emptySet(), List.of(subProvider), lookup));
    }
}