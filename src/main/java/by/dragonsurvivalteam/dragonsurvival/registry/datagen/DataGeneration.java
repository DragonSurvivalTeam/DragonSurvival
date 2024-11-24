package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements.DSAdvancements;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.*;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonStages;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DataGeneration {
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
        generator.addProvider(event.includeClient(), new DSLanguageProvider(output, "en_us"));

        // Server
        LootTableProvider.SubProviderEntry blockLootTableSubProvider = new LootTableProvider.SubProviderEntry(
                BlockLootTableSubProvider::new,
                LootContextParamSets.BLOCK);
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) lootTableOutput -> new LootTableProvider(lootTableOutput, Collections.emptySet(), List.of(blockLootTableSubProvider), event.getLookupProvider()));

        // built-in registries
        RegistrySetBuilder builder = new RegistrySetBuilder();
        builder.add(Registries.DAMAGE_TYPE, DSDamageTypes::registerDamageTypes);
        builder.add(Registries.ENCHANTMENT, DSEnchantments::registerEnchantments);
        builder.add(DragonBody.REGISTRY, DragonBodies::registerBodies);
        builder.add(DragonStage.REGISTRY, DragonStages::registerLevels);
        DatapackBuiltinEntriesProvider datapackProvider = new DatapackBuiltinEntriesProvider(output, lookup, builder, Set.of(DragonSurvival.MODID));
        generator.addProvider(event.includeServer(), datapackProvider);

        // external datapack for ancient dragon stage
        DataGenerator.PackGenerator ancientFeaturePack = generator.getBuiltinDatapack(true, DragonSurvival.MODID, "ancient_stage");
        ancientFeaturePack.addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(
                packOutput,
                Component.literal("Add in the ancient dragon stage to dragon survival."),
                FeatureFlagSet.of()
        ));
        RegistrySetBuilder ancientBuilder = new RegistrySetBuilder();
        ancientBuilder.add(DragonStage.REGISTRY, DragonStages::registerAncientDatapack);
        CompletableFuture<HolderLookup.Provider> finalLookup = lookup;
        ancientFeaturePack.addProvider(packOutput -> new DatapackBuiltinEntriesProvider(packOutput, finalLookup, ancientBuilder, Set.of(DragonSurvival.MODID)));

        // Update the lookup provider with our datapack entries
        lookup = datapackProvider.getRegistryProvider();

        BlockTagsProvider blockTagsProvider = new DSBlockTags(output, lookup, helper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new DSItemTags(output, lookup, blockTagsProvider.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new DSDamageTypeTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSEntityTypeTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSEffectTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSPoiTypeTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSEnchantmentTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DSBodyTags(output, lookup, helper));
        generator.addProvider(event.includeServer(), new DataBlockModelProvider(output, helper));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookup, helper, List.of(new DSAdvancements())));

        // Should run last due to doing weird registry things
        generator.addProvider(event.includeServer(), new DSRecipes(output, lookup));
    }
}