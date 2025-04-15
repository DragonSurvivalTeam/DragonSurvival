package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SkinPreset implements INBTSerializable<CompoundTag> {
    private static final String MODEL = "model";
    private static final String SPECIES = "species";

    private final Lazy<HashMap<ResourceKey<DragonStage>, Lazy<DragonStageCustomization>>> skins = Lazy.of(this::initialize);
    private ResourceKey<DragonSpecies> species;
    private ResourceLocation model = DragonBody.DEFAULT_MODEL;

    public boolean isEmpty() {
        return skins.get().values().stream().allMatch(
                dragonStageCustomizationLazy -> dragonStageCustomizationLazy.get().layerSettings.values().stream().allMatch(
                        layerSettingsLazy -> Objects.equals(layerSettingsLazy.get().partKey, DefaultPartLoader.NO_PART)));
    }

    public void setAllStagesToUseDefaultSkin(boolean defaultSkin) {
        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            DragonStageCustomization stageCustomization = skins.get().get(dragonStage).get();
            stageCustomization.defaultSkin = defaultSkin;
            skins.get().put(dragonStage, Lazy.of(() -> stageCustomization));
        }
    }

    public boolean isAnyStageUsingDefaultSkin() {
        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            if (skins.get().get(dragonStage).get().defaultSkin) {
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

    public void initDefaults(final Holder<DragonSpecies> species, final ResourceLocation model) {
        if (FMLLoader.getDist().isDedicatedServer()) {
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
        if (FMLLoader.getDist().isDedicatedServer()) {
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
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString(MODEL, model.toString());

        if (species != null) {
            tag.putString(SPECIES, species.location().toString());
        }

        List<ResourceKey<DragonStage>> stageKeys;
        if (species != null) {
            Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(provider, species);
            stageKeys = speciesHolder.map(dragonSpeciesReference -> dragonSpeciesReference.value().getStages(provider).stream().map(Holder::getKey).toList()).orElseGet(List::of);
        } else {
            stageKeys = ResourceHelper.keys(provider, DragonStage.REGISTRY);
        }

        for (ResourceKey<DragonStage> dragonStage : stageKeys) {
            if(skins.get().containsKey(dragonStage)) {
                tag.put(dragonStage.location().toString(), skins.get().getOrDefault(dragonStage, Lazy.of(DragonStageCustomization::new)).get().serializeNBT(provider));
            }
        }

        return tag;
    }

    // Special version of deserializeNBT to fix up broken data from an older version of the mod
    // The tag encoding of the species was broken and was just giving "minecraft:" as the species instead of what it should be
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag base, ResourceKey<DragonSpecies> species) {
        this.species = species;
        ResourceLocation.read(base.getString(MODEL)).ifSuccess(model -> this.model = model);

        List<ResourceKey<DragonStage>> stageKeys;
        if (species != null) {
            Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(provider, species);
            stageKeys = speciesHolder.map(dragonSpeciesReference -> dragonSpeciesReference.value().getStages(provider).stream().map(Holder::getKey).toList()).orElseGet(List::of);
        } else {
            stageKeys = ResourceHelper.keys(provider, DragonStage.REGISTRY);
        }

        for (ResourceKey<DragonStage> dragonStage : stageKeys) {
            if (base.contains(dragonStage.location().toString())) {
                skins.get().put(dragonStage, Lazy.of(() -> {
                    DragonStageCustomization group = new DragonStageCustomization();
                    CompoundTag dragonStageData = base.getCompound(dragonStage.location().toString());
                    group.deserializeNBT(provider, dragonStageData);
                    return group;
                }));
            }
        }
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag base) {
        this.species = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(base.getString(SPECIES)));
        ResourceLocation.read(base.getString(MODEL)).ifSuccess(model -> this.model = model);

        List<ResourceKey<DragonStage>> stageKeys;
        if (species != null) {
            Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(provider, species);
            stageKeys = speciesHolder.map(dragonSpeciesReference -> dragonSpeciesReference.value().getStages(provider).stream().map(Holder::getKey).toList()).orElseGet(List::of);
        } else {
            stageKeys = ResourceHelper.keys(provider, DragonStage.REGISTRY);
        }

        for (ResourceKey<DragonStage> dragonStage : stageKeys) {
            if (base.contains(dragonStage.location().toString())) {
                skins.get().put(dragonStage, Lazy.of(() -> {
                    DragonStageCustomization group = new DragonStageCustomization();
                    CompoundTag dragonStageData = base.getCompound(dragonStage.location().toString());
                    group.deserializeNBT(provider, dragonStageData);
                    return group;
                }));
            }
        }
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceKey<DragonSpecies> getSpecies() {
        return species;
    }
}