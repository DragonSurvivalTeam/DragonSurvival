package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SkinPreset implements INBTSerializable<CompoundTag> {
    private String MODEL = "model";
    private ResourceLocation model = DragonBody.DEFAULT_MODEL;
    private final Lazy<HashMap<ResourceLocation, Lazy<DragonStageCustomization>>> skins = Lazy.of(this::initialize);

    public Lazy<DragonStageCustomization> get(final ResourceKey<DragonStage> dragonStage) {
        return skins.get().get(dragonStage.location());
    }

    public void put(final ResourceKey<DragonStage> dragonStage, final Lazy<DragonStageCustomization> customization) {
        skins.get().put(dragonStage.location(), customization);
    }

    public void initDefaults(final DragonStateHandler handler) {
        initDefaults(handler.getType().getKey(), handler.getBody() != null ? handler.getBody().value().customModel() : null);
    }

    public void initDefaults(final ResourceKey<DragonType> type, final ResourceLocation model) {
        if (type == null) {
            return;
        }

        if(model != null) {
            this.model = model;
        }

        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            skins.get().put(dragonStage.location(), Lazy.of(() -> new DragonStageCustomization(dragonStage.location(), type)));
        }
    }

    public HashMap<ResourceLocation, Lazy<DragonStageCustomization>> initialize() {
        HashMap<ResourceLocation, Lazy<DragonStageCustomization>> customizations = new HashMap<>();

        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
            customizations.computeIfAbsent(dragonStage.location(), location -> Lazy.of(DragonStageCustomization::new));
        }

        return customizations;
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString(MODEL, model.toString());
        for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(provider, DragonStage.REGISTRY)) {
            tag.put(dragonStage.location().toString(), skins.get().getOrDefault(dragonStage.location(), Lazy.of(DragonStageCustomization::new)).get().serializeNBT(provider));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag base) {
        for (ResourceKey<DragonStage> level : ResourceHelper.keys(provider, DragonStage.REGISTRY)) {
            skins.get().put(level.location(), Lazy.of(() -> {
                        DragonStageCustomization group = new DragonStageCustomization();
                        CompoundTag dragonStageData = base.getCompound(level.location().toString());
                        group.deserializeNBT(provider, dragonStageData);
                        return group;
                    })
            );
        }

        DataResult<ResourceLocation> result = ResourceLocation.read(base.getString(MODEL));
        if (result.result().isPresent()) {
            model = result.result().get();
        }
    }
}