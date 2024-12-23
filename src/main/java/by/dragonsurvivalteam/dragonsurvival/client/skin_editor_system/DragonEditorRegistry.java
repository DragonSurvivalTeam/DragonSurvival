package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.util.json.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.HolderLookup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

@EventBusSubscriber(value = Dist.CLIENT)
public class DragonEditorRegistry {
    private static final String SAVED_CUSTOMIZATIONS = "saved_customizations_";
    private static final int MAX_SAVE_SLOTS = 9;

    private static final Map<Integer, File> savedFileForSlot = new HashMap<>();
    private static final Map<Integer, SkinPreset> savedCustomizations = new HashMap<>();
    private static boolean hasInitialized;

    @SubscribeEvent
    public static void initializeSavedCustomizations(final EntityJoinLevelEvent event) {
        loadSavedCustomizations();
    }

    // TODO :: Move away from GSON class parsing to have more control on which fields are stored and in which way
    @SuppressWarnings("ResultOfMethodCallIgnored") // ignore
    public static void loadSavedCustomizations() {
        if (hasInitialized) {
            return;
        }

        File directory = new File(FMLPaths.GAMEDIR.get().toFile(), "dragon-survival");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for(int i = 0; i < MAX_SAVE_SLOTS; i++) {
            File savedFile = new File(directory, SAVED_CUSTOMIZATIONS + i + ".json");
            savedFileForSlot.put(i, savedFile);
        }

        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            File savedFile = savedFileForSlot.get(i);
            if (savedFile.exists()) {
                try {
                    Gson gson = GsonFactory.getDefault();
                    InputStream input = new FileInputStream(savedFile);

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                        savedCustomizations.put(i, gson.fromJson(reader, SkinPreset.class));

                        if (savedCustomizations.get(i) == null) {
                            throw new IOException("Customization file [" + SAVED_CUSTOMIZATIONS + "] file could not be read");
                        }
                    } catch (IOException | JsonSyntaxException exception) {
                        DragonSurvival.LOGGER.warn("An error occurred while processing the [" + SAVED_CUSTOMIZATIONS + "] file", exception);
                    }
                } catch (FileNotFoundException exception) {
                    DragonSurvival.LOGGER.error("Saved customization [{}] could not be found", savedFile.getName(), exception);
                }
            }
        }

        hasInitialized = true;
    }

    public static void save(final SkinPreset presetToSave, int slot) {
        if (!hasInitialized) {
            loadSavedCustomizations();
        }

        try {
            if(presetToSave == null) {
                return;
            }

            Gson gson = GsonFactory.newBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(savedFileForSlot.get(slot));
            gson.toJson(presetToSave, writer);
            writer.close();
        } catch (IOException exception) {
            DragonSurvival.LOGGER.error("An error occurred while trying to save the dragon skin", exception);
        }

        savedCustomizations.put(slot, presetToSave);
    }

    public static SkinPreset load(int slot) {
        if (!hasInitialized) {
            loadSavedCustomizations();
        }

        return savedCustomizations.get(slot);
    }
}