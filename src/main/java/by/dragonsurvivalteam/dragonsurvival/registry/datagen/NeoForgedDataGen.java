package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = DragonSurvivalMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgedDataGen {
	@SubscribeEvent
	public static void dataGen(final GatherDataEvent event) {
		final DataGenerator generator = event.getGenerator();
		final PackOutput packOutput = generator.getPackOutput();
		final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		// Client

		generator.addProvider(event.includeClient(), new DataBlockStateProvider(packOutput, DragonSurvivalMod.MODID, existingFileHelper));
		generator.addProvider(event.includeClient(), new DataItemModelProvider(packOutput, DragonSurvivalMod.MODID, existingFileHelper));
		generator.addProvider(event.includeClient(), new DataSpriteSourceProvider(packOutput, lookupProvider, DragonSurvivalMod.MODID, existingFileHelper));

		// Server
		LootTableProvider.SubProviderEntry blockLootTableSubProvider = new LootTableProvider.SubProviderEntry(
				BlockLootTableSubProvider::new,
				LootContextParamSets.BLOCK);
		generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) output -> new LootTableProvider(output, Collections.emptySet(), List.of(blockLootTableSubProvider), event.getLookupProvider()));

		BlockTagsProvider blockTagsProvider = new DataBlockTagProvider(packOutput, lookupProvider, DragonSurvivalMod.MODID, existingFileHelper);
		generator.addProvider(event.includeServer(), blockTagsProvider);

		generator.addProvider(event.includeServer(), new DSItemTags(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
		generator.addProvider(event.includeServer(), new DataDamageTypeTagsProvider(packOutput, lookupProvider, DragonSurvivalMod.MODID, existingFileHelper));
		generator.addProvider(event.includeServer(), new DSEntityTypeTags(packOutput, lookupProvider, existingFileHelper));
		generator.addProvider(event.includeServer(), new DataPoiTypeTagsProvider(packOutput, lookupProvider, DragonSurvivalMod.MODID, existingFileHelper));
		generator.addProvider(event.includeServer(), new DataBlockModelProvider(packOutput, DragonSurvivalMod.MODID, existingFileHelper));
	}
}