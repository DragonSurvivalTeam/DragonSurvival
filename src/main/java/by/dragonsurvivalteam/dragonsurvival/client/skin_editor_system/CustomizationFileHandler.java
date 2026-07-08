package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.Copyable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static class SavedCustomization implements ValueIOSerializable, Copyable<SavedCustomization> {
        private DragonStageCustomization customization;
        private ResourceKey<DragonSpecies> dragonSpecies;
        private Identifier dragonModel;

        public SavedCustomization(final DragonStageCustomization customization, final ResourceKey<DragonSpecies> dragonSpecies, final Identifier dragonModel) {
            this.customization = customization;
            this.dragonSpecies = dragonSpecies;
            this.dragonModel = dragonModel;
        }

        public SavedCustomization() {}

        public static SavedCustomization fromHandler(final DragonStateHandler handler, final HolderLookup.Provider provider) {
            // Create a new customization object, otherwise we'll end up entangling pointers together and cause some weird behavior
            return new SavedCustomization(handler.getCurrentStageCustomization().copy(provider), handler.speciesKey(), handler.body().value().model());
        }

        public static SavedCustomization fromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            SavedCustomization customization = new SavedCustomization();
            ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, provider, nbt);
            customization.deserialize(valueInput);

            if (customization.customization == null || customization.dragonSpecies == null || customization.dragonModel == null) {
                return null;
            }

            return customization;
        }

        public DragonStageCustomization getCustomization() {
            return customization;
        }

        public ResourceKey<DragonSpecies> getDragonSpecies() {
            return dragonSpecies;
        }

        public Identifier getDragonModel() {
            return dragonModel;
        }

        @Override
        public void serialize(@NotNull ValueOutput output) {
            output.putChild(CUSTOMIZATION, customization);
            output.putString(DRAGON_SPECIES, dragonSpecies.identifier().toString());
            output.putString(DRAGON_MODEL, dragonModel.toString());
        }

        @Override
        public void deserialize(@NotNull ValueInput input) {
            this.customization = new DragonStageCustomization();

            input.child(CUSTOMIZATION).ifPresent(customization -> this.customization.deserialize(customization));
            input.getString(DRAGON_SPECIES).ifPresent(dragonSpecies -> this.dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, Identifier.parse(dragonSpecies)));
            input.getString(DRAGON_MODEL).ifPresent(dragonModel -> this.dragonModel = Identifier.parse(dragonModel));
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

    public static void loadSavedCustomizations(final HolderLookup.Provider provider) {
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

                SavedCustomization savedCustomization = SavedCustomization.fromNBT(provider, nbt);

                if (savedCustomization == null) {
                    DragonSurvival.LOGGER.warn("Could not read saved skin from the file [{}]", savedFile);
                    continue;
                }

                savedCustomizations.put(slot, savedCustomization);
            } catch (IOException exception) {
                DragonSurvival.LOGGER.warn("An error occurred while processing the file [{}]", savedFile, exception);
            }
        }

        hasInitialized = true;
    }

    public static void save(final DragonStateHandler handler, int slot, final HolderLookup.Provider provider) {
        if (!hasInitialized) {
            loadSavedCustomizations(provider);
        }

        SavedCustomization savedCustomization = SavedCustomization.fromHandler(handler, provider);
        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        savedCustomization.serialize(valueOutput);

        try {
            NbtIo.write(valueOutput.buildResult(), savedFileForSlot.get(slot).toPath());
        } catch (IOException exception) {
            DragonSurvival.LOGGER.error("An error occurred while trying to save the dragon skin", exception);
        }

        savedCustomizations.put(slot, savedCustomization);
    }

    public static @Nullable SavedCustomization load(int slot, final HolderLookup.Provider provider) {
        if (!hasInitialized) {
            loadSavedCustomizations(provider);
        }

        SavedCustomization customization = savedCustomizations.get(slot);

        if (customization != null) {
            return customization.copy(provider);
        }

        return null;
    }
}
