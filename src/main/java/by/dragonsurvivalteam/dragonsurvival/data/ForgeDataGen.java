package by.dragonsurvivalteam.dragonsurvival.data;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.data.loot.DragonHeartLootModifierSerializer;
import by.dragonsurvivalteam.dragonsurvival.data.loot.DragonOreLootModifierSerializer;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeDataGen {
	@SubscribeEvent // Currently not runnable in 1.19.2
	public static void configureDataGen(GatherDataEvent event){
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		BlockTagsProvider provider = new ForgeBlockTagsProvider(generator, existingFileHelper);

		generator.addProvider(true, new DataLanguageProvider(generator, DragonSurvivalMod.MODID, "en_us"));
		generator.addProvider(true, new DataSoundProvider(generator, DragonSurvivalMod.MODID, existingFileHelper));

		generator.addProvider(true, new DataRecipeProvider(generator));
		generator.addProvider(true, new DataLootTableProvider(generator, existingFileHelper));

		generator.addProvider(true, new DataItemTagProvider(generator, provider, DragonSurvivalMod.MODID, existingFileHelper));
		generator.addProvider(true, new DataBlockTagProvider(generator, DragonSurvivalMod.MODID, existingFileHelper));

		generator.addProvider(true, new DataBlockModelProvider(generator, DragonSurvivalMod.MODID, existingFileHelper));
		generator.addProvider(true, new DataItemModelProvider(generator, DragonSurvivalMod.MODID, existingFileHelper));
		generator.addProvider(true, new DataBlockStateProvider(generator, DragonSurvivalMod.MODID, existingFileHelper));

		generator.addProvider(event.includeServer(), new DragonOreLootModifierSerializer(generator, DragonSurvivalMod.MODID));
		generator.addProvider(event.includeServer(), new DragonHeartLootModifierSerializer(generator, DragonSurvivalMod.MODID));
		generator.addProvider(event.includeServer(), new DSEntityTypeTags(generator, existingFileHelper));
	}
}