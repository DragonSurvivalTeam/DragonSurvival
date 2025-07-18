package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber(value = Dist.CLIENT)
public class CustomizationFileHandler {
    public static final Function<Integer, String> FILE_NAME = slot -> "saved_customizations_" + slot + ".nbt";
    public static final String DIRECTORY = "dragon-survival";
    public static final int STARTING_SLOT = 1;
    public static final int MAX_SAVE_SLOTS = 5;

    private static final String CUSTOMIZATION = "customization";
    private static final String DRAGON_SPECIES = "dragon_species";
    private static final String DRAGON_MODEL = "dragon_model";

    public static class SavedCustomization implements INBTSerializable<CompoundTag> {
        private DragonStageCustomization customization;
        private ResourceKey<DragonSpecies> dragonSpecies;
        private ResourceLocation dragonModel;

        public SavedCustomization(DragonStageCustomization customization, ResourceKey<DragonSpecies> dragonSpecies, ResourceLocation dragonModel) {
            this.customization = customization;
            this.dragonSpecies = dragonSpecies;
            this.dragonModel = dragonModel;
        }

        public SavedCustomization() {}

        public static SavedCustomization fromHandler(DragonStateHandler handler) {
            // Create a new customization object, otherwise we'll end up entangling pointers together and cause some weird behavior
            DragonStageCustomization newCustomization = new DragonStageCustomization();
            //noinspection DataFlowIssue -> proxy is expected to be present
            newCustomization.deserializeNBT(DragonSurvival.PROXY.getAccess(), handler.getCurrentStageCustomization().serializeNBT(DragonSurvival.PROXY.getAccess()));
            return new SavedCustomization(newCustomization, handler.speciesKey(), handler.body().value().model());
        }

        public static SavedCustomization fromNbt(@NotNull HolderLookup.Provider provider, CompoundTag nbt) {
            SavedCustomization customization = new SavedCustomization();
            customization.deserializeNBT(provider, nbt);

            if (customization.customization == null || customization.dragonSpecies == null || customization.dragonModel == null) {
                return null;
            }

            return customization;
        }

        @Override
        public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
            CompoundTag nbt = new CompoundTag();
            nbt.put(CUSTOMIZATION, customization.serializeNBT(provider));
            nbt.putString(DRAGON_SPECIES, dragonSpecies.location().toString());
            nbt.putString(DRAGON_MODEL, dragonModel.toString());
            return nbt;
        }

        @Override
        public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            this.customization = new DragonStageCustomization();

            if (nbt.contains(CUSTOMIZATION)) {
                this.customization.deserializeNBT(provider, nbt.getCompound(CUSTOMIZATION));
            }

            if (nbt.contains(DRAGON_SPECIES)) {
                this.dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(nbt.getString(DRAGON_SPECIES)));
            }

            if (nbt.contains(DRAGON_MODEL)) {
                this.dragonModel = ResourceLocation.parse(nbt.getString(DRAGON_MODEL));
            }
        }

        public DragonStageCustomization getCustomization() {
            return customization;
        }

        public ResourceKey<DragonSpecies> getDragonSpecies() {
            return dragonSpecies;
        }

        public ResourceLocation getDragonModel() {
            return dragonModel;
        }
    }

    /**
     * Contains {@link File} objects for the specified slots {@link CustomizationFileHandler#MAX_SAVE_SLOTS} <br>
     * Said files may not exist in the actual file system though
     */
    private static final Map<Integer, File> savedFileForSlot = new HashMap<>();
    private static final Map<Integer, SavedCustomization> savedCustomizations = new HashMap<>();
    private static boolean hasInitialized;

    @SubscribeEvent
    public static void initializeSavedCustomizations(final EntityJoinLevelEvent event) {
        loadSavedCustomizations(event.getLevel().registryAccess());
    }

    public static void loadSavedCustomizations(final RegistryAccess access) {
        if (hasInitialized) {
            return;
        }

        File directory = new File(FMLPaths.GAMEDIR.get().toFile(), DIRECTORY);

        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored -> ignore
            directory.mkdirs();
        }

        for (int slot = STARTING_SLOT; slot <= MAX_SAVE_SLOTS; slot++) {
            File savedFile = new File(directory, FILE_NAME.apply(slot));
            savedFileForSlot.put(slot, savedFile);

            if (!savedFile.exists()) {
                continue;
            }

            try {
                CompoundTag nbt = NbtIo.read(savedFile.toPath());

                if (nbt == null) {
                    DragonSurvival.LOGGER.warn("Could not read saved skin from the file [{}]", savedFile);
                    continue;
                }

                SavedCustomization savedCustomization = SavedCustomization.fromNbt(access, nbt);

                if (savedCustomization == null) {
                    DragonSurvival.LOGGER.warn("Could not read saved skin from the file [{}]", savedFile);
                    continue;
                }

                savedCustomizations.put(slot, SavedCustomization.fromNbt(access, nbt));
            } catch (IOException exception) {
                DragonSurvival.LOGGER.warn("An error occurred while processing the file [{}]", savedFile, exception);
            }
        }

        hasInitialized = true;
    }

    public static void save(final DragonStateHandler handler, int slot, final RegistryAccess access) {
        if (!hasInitialized) {
            loadSavedCustomizations(access);
        }

        SavedCustomization savedCustomization = SavedCustomization.fromHandler(handler);

        try {
            NbtIo.write(savedCustomization.serializeNBT(access), savedFileForSlot.get(slot).toPath());
        } catch (IOException exception) {
            DragonSurvival.LOGGER.error("An error occurred while trying to save the dragon skin", exception);
        }

        savedCustomizations.put(slot, savedCustomization);
    }

    public static SavedCustomization load(int slot, final RegistryAccess access) {
        if (!hasInitialized) {
            loadSavedCustomizations(access);
        }

        return savedCustomizations.get(slot);
    }
}