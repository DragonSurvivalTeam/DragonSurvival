package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT)
public class CustomizationFileHandler {
    private static final String SAVED_CUSTOMIZATIONS = "saved_customizations_";
    private static final int MAX_SAVE_SLOTS = 9;

    /**
     * Contains {@link File} objects for the specified slots {@link CustomizationFileHandler#MAX_SAVE_SLOTS} <br>
     * Said files may not exist in the actual file system though
     */
    private static final Map<Integer, File> savedFileForSlot = new HashMap<>();
    private static final Map<Integer, SkinPreset> savedCustomizations = new HashMap<>();
    private static boolean hasInitialized;

    @SubscribeEvent
    public static void initializeSavedCustomizations(final EntityJoinLevelEvent event) {
        loadSavedCustomizations();
    }

    public static void loadSavedCustomizations() {
        if (hasInitialized) {
            return;
        }

        File directory = new File(FMLPaths.GAMEDIR.get().toFile(), "dragon-survival");

        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored -> ignore
            directory.mkdirs();
        }

        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            File savedFile = new File(directory, SAVED_CUSTOMIZATIONS + i + ".nbt");
            savedFileForSlot.put(i, savedFile);

            if (!savedFile.exists()) {
                continue;
            }

            try {
                CompoundTag nbt = NbtIo.read(savedFile.toPath());

                if (nbt == null) {
                    DragonSurvival.LOGGER.warn("Could not read saved skin from the file [{}]", savedFile);
                    continue;
                }

                SkinPreset preset = new SkinPreset();
                //noinspection DataFlowIssue -> access is expected to be present
                preset.deserializeNBT(DragonSurvival.PROXY.getAccess(), nbt);
                savedCustomizations.put(i, preset);
            } catch (IOException exception) {
                DragonSurvival.LOGGER.warn("An error occurred while processing the file [{}]", savedFile, exception);
            }
        }

        hasInitialized = true;
    }

    public static void save(final SkinPreset preset, int slot) {
        if (!hasInitialized) {
            loadSavedCustomizations();
        }

        try {
            //noinspection DataFlowIssue -> access is expected to be present
            NbtIo.write(preset.serializeNBT(DragonSurvival.PROXY.getAccess()), savedFileForSlot.get(slot).toPath());
        } catch (IOException exception) {
            DragonSurvival.LOGGER.error("An error occurred while trying to save the dragon skin", exception);
        }

        savedCustomizations.put(slot, preset);
    }

    public static SkinPreset load(int slot) {
        if (!hasInitialized) {
            loadSavedCustomizations();
        }

        return savedCustomizations.get(slot);
    }
}