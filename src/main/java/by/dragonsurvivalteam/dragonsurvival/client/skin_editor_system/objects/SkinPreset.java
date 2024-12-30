package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SkinPreset implements INBTSerializable<CompoundTag> {
    private static final String MODEL = "model";
    private static final String TYPE = "type";

    private final Lazy<HashMap<ResourceKey<DragonStage>, Lazy<DragonStageCustomization>>> skins = Lazy.of(this::initialize);
    private ResourceKey<DragonType> type;
    private ResourceLocation model = DragonBody.DEFAULT_MODEL;

    public Lazy<DragonStageCustomization> get(final ResourceKey<DragonStage> dragonStage) {
        return skins.get().get(dragonStage);
    }

    public void put(final ResourceKey<DragonStage> dragonStage, final Lazy<DragonStageCustomization> customization) {
        skins.get().put(dragonStage, customization);
    }

    public void initDefaults(final ResourceKey<DragonType> type, final ResourceLocation model) {
        if (type == null) {
            return;
        }

        this.type = type;

        if (model != null) {
            this.model = model;
        }

        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            skins.get().put(dragonStage, Lazy.of(() -> new DragonStageCustomization(dragonStage, type)));
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
        if(type != null) {
            tag.putString(TYPE, type.location().toString());
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

        this.type = ResourceKey.create(DragonType.REGISTRY, ResourceLocation.parse(base.getString(TYPE)));
        ResourceLocation.read(base.getString(MODEL)).ifSuccess(model -> this.model = model);
    }
}