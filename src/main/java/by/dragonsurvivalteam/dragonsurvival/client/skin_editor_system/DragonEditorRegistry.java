package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject.Texture;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SavedSkinPresets;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.GsonFactory;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD )
public class DragonEditorRegistry{
	public static final String SAVED_FILE_NAME = "saved_customizations.json";
	public static final ResourceLocation CUSTOMIZATION = new ResourceLocation(DragonSurvivalMod.MODID, "customization.json");
	public static final HashMap<String, HashMap<EnumSkinLayer, Texture[]>> CUSTOMIZATIONS = new HashMap<>();
	private static boolean init = false;
	private static SavedSkinPresets savedCustomizations = null;
	public static HashMap<String, HashMap<DragonLevel, HashMap<EnumSkinLayer, String>>> defaultSkinValues = new HashMap<>();
	public static File folder;
	public static File savedFile;

	public static String getDefaultPart(AbstractDragonType type, DragonLevel level, EnumSkinLayer layer){
		return defaultSkinValues.getOrDefault(type.getTypeNameUpperCase(), new HashMap<>()).getOrDefault(level, new HashMap<>()).getOrDefault(layer, SkinCap.defaultSkinValue);
	}

	public static SavedSkinPresets getSavedCustomizations(){
		if(!init) genDefaults();
		return savedCustomizations;
	}

	@OnlyIn( Dist.CLIENT )
	@SubscribeEvent
	public static void clientStart(FMLClientSetupEvent event){
		genDefaults();

		if(Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager){
			((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener((ResourceManagerReloadListener)manager -> {
				CUSTOMIZATIONS.clear();
				reload(Minecraft.getInstance().getResourceManager(), CUSTOMIZATION);
			});
		}
	}

	private static void genDefaults(){
		if(init) return;

		reload(Minecraft.getInstance().getResourceManager(), CUSTOMIZATION);

		folder = new File(FMLPaths.GAMEDIR.get().toFile(), "dragon-survival");
		savedFile = new File(folder, SAVED_FILE_NAME);

		if(!folder.exists()){
			folder.mkdirs();
		}

		File oldFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "/dragon-survival/" + SAVED_FILE_NAME);

		if(oldFile.exists()){
			oldFile.renameTo(savedFile);
			oldFile.getParentFile().delete();
			savedFile = new File(folder, SAVED_FILE_NAME);
		}

		if(!savedFile.exists()){
			try{
				savedFile.createNewFile();
				Gson gson = GsonFactory.newBuilder().setPrettyPrinting().create();
				savedCustomizations = new SavedSkinPresets();

				for(String t : DragonTypes.getTypes()){
					String type = t.toUpperCase(Locale.ENGLISH);
					savedCustomizations.skinPresets.computeIfAbsent(type, b -> new HashMap<>());
					savedCustomizations.current.computeIfAbsent(type, b -> new HashMap<>());

					for(int i = 0; i < 9; i++){
						savedCustomizations.skinPresets.get(type).computeIfAbsent(i, b -> {
							SkinPreset preset = new SkinPreset();
							preset.initDefaults(DragonTypes.getStatic(type));
							return preset;
						});
					}

					for(DragonLevel level : DragonLevel.values()){
						savedCustomizations.current.get(type).put(level, 0);
					}
				}

				FileWriter writer = new FileWriter(savedFile);
				gson.toJson(savedCustomizations, writer);
				writer.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		} else {
			try {
				Gson gson = GsonFactory.getDefault();
				InputStream in = new FileInputStream(savedFile);

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
					savedCustomizations = gson.fromJson(reader, SavedSkinPresets.class);
					savedCustomizations = SkinPortingSystem.upgrade(savedCustomizations);
				} catch (IOException exception) {
					DragonSurvivalMod.LOGGER.warn("Reader could not be closed", exception);
				}
			} catch (FileNotFoundException exception) {
				DragonSurvivalMod.LOGGER.error("Saved customization [" + savedFile.getName() + "] could not be found", exception);
			}
		}

		init = true;
	}

	protected static void reload(ResourceManager manager, ResourceLocation location){
		try{
			Gson gson = GsonFactory.getDefault();
			Optional<Resource> resource = manager.getResource(location);
			if (resource.isEmpty())
				throw new IOException(String.format("Resource %s not found!", location.getPath()));
			InputStream in = resource.get().open();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
				DragonEditorObject je = gson.fromJson(reader, DragonEditorObject.class);
				CUSTOMIZATIONS.computeIfAbsent(DragonTypes.SEA.getTypeNameUpperCase(), type -> new HashMap<>());
				CUSTOMIZATIONS.computeIfAbsent(DragonTypes.CAVE.getTypeNameUpperCase(), type -> new HashMap<>());
				CUSTOMIZATIONS.computeIfAbsent(DragonTypes.FOREST.getTypeNameUpperCase(), type -> new HashMap<>());

				dragonType(DragonTypes.SEA, je.sea_dragon);
				dragonType(DragonTypes.CAVE, je.cave_dragon);
				dragonType(DragonTypes.FOREST, je.forest_dragon);

				defaultSkinValues = je.defaults;
			} catch (IOException exception) {
				DragonSurvivalMod.LOGGER.warn("Reader could not be closed", exception);
			}
		} catch (IOException exception) {
			DragonSurvivalMod.LOGGER.error("Resource [" + location + "] could not be opened", exception);
		}
	}

	private static void dragonType(AbstractDragonType type, DragonEditorObject.Dragon je){
		if(je != null){
			if(je.layers != null){
				je.layers.forEach((layer, keys) -> {
					for(Texture key : keys){
						if(key.key == null){
							key.key = key.texture.substring(key.texture.lastIndexOf("/") + 1);
							key.key = key.key.substring(0, key.key.lastIndexOf("."));
						}
					}
					CUSTOMIZATIONS.get(type.getTypeNameUpperCase()).put(layer, keys);
				});
			}
		}
	}
}