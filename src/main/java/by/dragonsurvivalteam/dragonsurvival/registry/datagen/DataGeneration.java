package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements.DSAdvancements;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.*;
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
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DataGeneration {
    private static final String ANCIENT_STAGE_DATAPACK = "ancient_stage";
    private static final String UNLOCK_WINGS_DATAPACK = "unlock_wings";

    @Translation(comments = "Adds the Ancient dragon stage to Dragon Survival")
    private static final String ANCIENT_STAGE_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + ANCIENT_STAGE_DATAPACK);

    @Translation(comments = "Automatically unlocks dragon wings and prevents the ender dragon from disabling them in Dragon Survival")
    private static final String UNLOCK_WINGS_DATAPACK_DESCRIPTION = Translation.Type.GUI.wrap("datapack." + UNLOCK_WINGS_DATAPACK);

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
        generator.addProvider(event.includeServer(), new DSEnchantmentTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSDragonBodyTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSDragonAbilityTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSDragonSpeciesTags(output, lookup, helper));

        generator.addProvider(event.includeServer(), new DataBlockModelProvider(output, helper));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookup, helper, List.of(new DSAdvancements())));

        // Should run last due to doing weird registry things
        generator.addProvider(event.includeServer(), new DSRecipes(output, lookup));
    }

    @SubscribeEvent
    public static void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            HashMap<MutableComponent, String> resourcepacks = new HashMap<>();
            resourcepacks.put(Component.literal("DS - Old Magic Icons"), "resourcepacks/ds_old_magic");
            resourcepacks.put(Component.literal("DS - Dark GUI"), "resourcepacks/ds_dark_gui");

            for (Map.Entry<MutableComponent, String> entry : resourcepacks.entrySet()) {
                registerBuiltinResourcePack(event, entry.getKey(), entry.getValue());
            }
        } else if (event.getPackType() == PackType.SERVER_DATA) {
            HashMap<MutableComponent, String> datapacks = new HashMap<>();
            datapacks.put(Component.literal("DS - Ancient Dragons"), "data/" + DragonSurvival.MODID + "/datapacks/" + ANCIENT_STAGE_DATAPACK);
            datapacks.put(Component.literal("DS - Unlock Wings"), "data/" + DragonSurvival.MODID + "/datapacks/" + UNLOCK_WINGS_DATAPACK);

            for (Map.Entry<MutableComponent, String> entry : datapacks.entrySet()) {
                registerBuiltInDataPack(event, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void registerBuiltinResourcePack(final AddPackFindersEvent event, MutableComponent name, String folder) {
        event.addPackFinders(DragonSurvival.res(folder), PackType.CLIENT_RESOURCES, name, PackSource.BUILT_IN, false, Pack.Position.TOP);
    }

    private static void registerBuiltInDataPack(AddPackFindersEvent event, MutableComponent name, String folder) {
        event.addPackFinders(DragonSurvival.res(folder), PackType.SERVER_DATA, name, PackSource.FEATURE, true, Pack.Position.TOP);
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
}