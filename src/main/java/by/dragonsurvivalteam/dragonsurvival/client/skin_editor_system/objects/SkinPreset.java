package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SkinPreset implements ValueIOSerializable {
    private static final String MODEL = "model";
    private static final String SPECIES = "species";

    private final Lazy<HashMap<ResourceKey<DragonStage>, Lazy<DragonStageCustomization>>> skins = Lazy.of(this::initialize);
    private ResourceKey<DragonSpecies> species;
    private Identifier model = DragonBody.DEFAULT_MODEL;

    public boolean isEmpty() {
        return skins.get().values().stream().allMatch(
                dragonStageCustomizationLazy -> dragonStageCustomizationLazy.get().layerSettings.values().stream().allMatch(
                        layerSettingsLazy -> Objects.equals(layerSettingsLazy.get().partKey, DefaultPartLoader.NO_PART)));
    }

    public void setAllStagesToUseDefaultSkin(boolean defaultSkin) {
        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            Lazy<DragonStageCustomization> customizationForStage = skins.get().get(dragonStage);
            if(customizationForStage == null) {
                continue;
            }

            DragonStageCustomization stageCustomization = customizationForStage.get();
            stageCustomization.defaultSkin = defaultSkin;
            skins.get().put(dragonStage, Lazy.of(() -> stageCustomization));
        }
    }

    public boolean isAnyStageUsingDefaultSkin() {
        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            Lazy<DragonStageCustomization> customizationForStage = skins.get().get(dragonStage);
            if (customizationForStage != null && customizationForStage.get().defaultSkin) {
                return true;
            }
        }
        return false;
    }

    public boolean isStageUsingDefaultSkin(final ResourceKey<DragonStage> dragonStage) {
        return skins.get().get(dragonStage).get().defaultSkin;
    }

    public Lazy<DragonStageCustomization> get(final ResourceKey<DragonStage> dragonStage) {
        return skins.get().get(dragonStage);
    }

    public void put(final ResourceKey<DragonStage> dragonStage, final Lazy<DragonStageCustomization> customization) {
        skins.get().put(dragonStage, customization);
    }

    public void initDefaults(final Holder<DragonSpecies> species, final Identifier model) {
        if (FMLLoader.getCurrent().getDist().isDedicatedServer()) {
            // Don't try to initialize default data for skin presets when asked by the server, as the server doesn't have the part data
            // to construct this data anyways
            return;
        }

        if (species == null) {
            return;
        }

        this.species = species.getKey();

        if (model != null) {
            this.model = model;
        }

        if (this.model == null) {
            for (Holder<DragonStage> dragonStage : species.value().getStages(null)) {
                skins.get().put(dragonStage.getKey(), Lazy.of(DragonStageCustomization::new));
            }
        } else {
            for (Holder<DragonStage> dragonStage : species.value().getStages(null)) {
                skins.get().put(dragonStage.getKey(), Lazy.of(() -> new DragonStageCustomization(dragonStage.getKey(), species.getKey(), this.model)));
            }
        }
    }

    public HashMap<ResourceKey<DragonStage>, Lazy<DragonStageCustomization>> initialize() {
        if (FMLLoader.getCurrent().getDist().isDedicatedServer()) {
            // Don't try to initialize default data for skin presets when asked by the server, as the server doesn't have the part data
            // to construct this data anyways
            return new HashMap<>();
        }

        HashMap<ResourceKey<DragonStage>, Lazy<DragonStageCustomization>> customizations = new HashMap<>();

        List<ResourceKey<DragonStage>> stageKeys;
        if (species != null) {
            Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(null, species);
            stageKeys = speciesHolder.map(dragonSpeciesReference -> dragonSpeciesReference.value().getStages(null).stream().map(Holder::getKey).toList()).orElseGet(List::of);
        } else {
            stageKeys = ResourceHelper.keys(null, DragonStage.REGISTRY);
        }

        for (ResourceKey<DragonStage> dragonStage : stageKeys) {
            customizations.computeIfAbsent(dragonStage, location -> Lazy.of(() -> new DragonStageCustomization(dragonStage, species, model)));
        }

        return customizations;
    }

    @Override
    public void serialize(@NotNull ValueOutput output) {
        CompoundTag tag = new CompoundTag();
        tag.putString(MODEL, model.toString());

        if (species != null) {
            output.putString(SPECIES, species.identifier().toString());
        }

        List<ResourceKey<DragonStage>> stageKeys;
        if (species != null) {
            Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(null, species);
            stageKeys = speciesHolder.map(dragonSpeciesReference -> dragonSpeciesReference.value().getStages(null).stream().map(Holder::getKey).toList()).orElseGet(List::of);
        } else {
            stageKeys = ResourceHelper.keys(null, DragonStage.REGISTRY);
        }

        for (ResourceKey<DragonStage> dragonStage : stageKeys) {
            if(skins.get().containsKey(dragonStage)) {
                output.putChild(dragonStage.identifier().toString(), skins.get().getOrDefault(dragonStage, Lazy.of(DragonStageCustomization::new)).get());
            }
        }
    }

    @Override
    public void deserialize(@NotNull ValueInput valueInput) {
        String speciesId = valueInput.getStringOr(SPECIES, null);
        if (speciesId != null) {
            this.species = ResourceKey.create(DragonSpecies.REGISTRY, Identifier.parse(speciesId));
        } else {
            this.species = null;
        }

        String modelId = valueInput.getStringOr(MODEL, null);
        if (modelId != null) {
            Identifier.read(modelId).ifSuccess(model -> this.model = model);
        }

        List<ResourceKey<DragonStage>> stageKeys;
        if (species != null) {
            Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(null, species);
            stageKeys = speciesHolder.map(dragonSpeciesReference -> dragonSpeciesReference.value().getStages(null).stream().map(Holder::getKey).toList()).orElseGet(List::of);
        } else {
            stageKeys = ResourceHelper.keys(null, DragonStage.REGISTRY);
        }

        for (ResourceKey<DragonStage> dragonStage : stageKeys) {
            if (valueInput.child(dragonStage.identifier().toString()).isPresent()) {
                skins.get().put(dragonStage, Lazy.of(() -> {
                    DragonStageCustomization group = new DragonStageCustomization();
                    ValueInput dragonStageData = valueInput.childOrEmpty(dragonStage.identifier().toString());
                    group.deserialize(dragonStageData);
                    return group;
                }));
            }
        }
    }

    public Identifier getModel() {
        return model;
    }

    public ResourceKey<DragonSpecies> getSpecies() {
        return species;
    }
}
