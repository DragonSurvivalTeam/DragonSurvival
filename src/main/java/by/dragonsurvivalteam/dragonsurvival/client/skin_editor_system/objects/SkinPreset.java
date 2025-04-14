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
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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
        if (species == null) {
            return;
        }

        this.species = species.getKey();

        if (model != null) {
            this.model = model;
        }

        for (Holder<DragonStage> dragonStage : species.value().getStages(null)) {
            skins.get().put(dragonStage.getKey(), Lazy.of(() -> new DragonStageCustomization(dragonStage.getKey(), species.getKey(), this.model)));
        }
    }

    public HashMap<ResourceKey<DragonStage>, Lazy<DragonStageCustomization>> initialize() {
        HashMap<ResourceKey<DragonStage>, Lazy<DragonStageCustomization>> customizations = new HashMap<>();

        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            customizations.computeIfAbsent(dragonStage, location -> Lazy.of(DragonStageCustomization::new));
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

        if (species != null) {
            Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(provider, species);
            if(speciesHolder.isPresent()) {
                for (Holder<DragonStage> dragonStage : speciesHolder.get().value().getStages(provider)) {
                    tag.put(dragonStage.getKey().location().toString(), skins.get().getOrDefault(dragonStage.getKey(), Lazy.of(DragonStageCustomization::new)).get().serializeNBT(provider));
                }

                return tag;
            }
        }

        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(provider, DragonStage.REGISTRY)) {
            tag.put(dragonStage.location().toString(), skins.get().getOrDefault(dragonStage, Lazy.of(DragonStageCustomization::new)).get().serializeNBT(provider));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag base) {
        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(provider, DragonStage.REGISTRY)) {
            skins.get().put(dragonStage, Lazy.of(() -> {
                        DragonStageCustomization group = new DragonStageCustomization();
                        CompoundTag dragonStageData = base.getCompound(dragonStage.location().toString());
                        group.deserializeNBT(provider, dragonStageData);
                        return group;
                    })
            );
        }

        this.species = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(base.getString(SPECIES)));
        ResourceLocation.read(base.getString(MODEL)).ifSuccess(model -> this.model = model);
    }

    public void setSpecies(final ResourceKey<DragonSpecies> species) {
        this.species = species;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceKey<DragonSpecies> getSpecies() {
        return species;
    }
}