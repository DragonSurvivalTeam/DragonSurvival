package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Saved customization per dragon level */
public class DragonStageCustomization implements INBTSerializable<CompoundTag> {
    public static final String HAS_WINGS = "wings";
    public static final String IS_DEFAULT_SKIN = "defaultSkin";

    public final HashMap<SkinLayer, Lazy<LayerSettings>> layerSettings = new HashMap<>();

    public boolean wings = true;
    public boolean defaultSkin;

    public DragonStageCustomization(final ResourceKey<DragonStage> stage, final ResourceKey<DragonSpecies> species, final ResourceLocation customModel) {
        this();

        for (SkinLayer layer : SkinLayer.values()) {
            // Convert the numbered 'EXTRA' layer to the generic 'EXTRA' layer
            SkinLayer actualLayer = SkinLayer.valueOf(layer.getNameUpperCase());
            Map<SkinLayer, List<DragonPart>> partMap = DragonPartLoader.DRAGON_PARTS.get(species);

            if (partMap != null) {
                List<DragonPart> parts = partMap.get(actualLayer);
                String partKey = DefaultPartLoader.getDefaultPartKey(species, stage, Either.right(customModel), layer);

                if (parts != null) {
                    for (DragonPart part : parts) {
                        if (part.key().equals(partKey)) {
                            layerSettings.put(layer, Lazy.of(() -> new LayerSettings(partKey, part.averageHue())));
                            break;
                        }
                    }
                } else {
                    layerSettings.put(layer, Lazy.of(() -> new LayerSettings(partKey, 0.5f)));
                }
            } else {
                layerSettings.put(layer, Lazy.of(LayerSettings::new));
            }
        }
    }

    public DragonStageCustomization() {
        for (SkinLayer layer : SkinLayer.values()) {
            layerSettings.computeIfAbsent(layer, key -> Lazy.of(LayerSettings::new));
        }
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag layerData = new CompoundTag();
        layerData.putBoolean(HAS_WINGS, wings);
        layerData.putBoolean(IS_DEFAULT_SKIN, defaultSkin);

        for (SkinLayer layer : SkinLayer.values()) {
            layerData.put(layer.name(), layerSettings.getOrDefault(layer, Lazy.of(LayerSettings::new)).get().serializeNBT(provider));
        }

        return layerData;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        wings = tag.getBoolean(HAS_WINGS);
        defaultSkin = tag.getBoolean(IS_DEFAULT_SKIN);

        for (SkinLayer layer : SkinLayer.values()) {
            layerSettings.put(layer, Lazy.of(() -> {
                LayerSettings settings = new LayerSettings();
                CompoundTag layerData = tag.getCompound(layer.name());
                settings.deserializeNBT(provider, layerData);
                return settings;
            }));
        }
    }
}