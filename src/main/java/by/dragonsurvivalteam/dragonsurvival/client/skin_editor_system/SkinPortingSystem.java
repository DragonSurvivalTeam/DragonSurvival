package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SavedSkinPresets;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.List;
import java.util.Map;

// FIXME :: Deprecate? The new skin storage is so different I'm not sure it is worth trying to version
public class SkinPortingSystem {
    public static void upgrade(SavedSkinPresets presets) {
        if (presets.version == 0) {
            upgrade0to3(presets);
        }
    }

    public static void upgrade0to3(SavedSkinPresets presets) {
        for (ResourceKey<DragonType> type : presets.skinPresets.keySet()) {
            for (int saveSlot : presets.skinPresets.get(type).keySet()) {
                for (ResourceKey<DragonStage> dragonStage : ResourceHelper.keys(null, DragonStage.REGISTRY)) {
                    Lazy<DragonStageCustomization> lazy = presets.skinPresets.get(type).get(saveSlot).get(dragonStage);

                    if (lazy == null) {
                        lazy = Lazy.of(DragonStageCustomization::new);
                    }

                    DragonStageCustomization customization = lazy.get();

                    for (EnumSkinLayer layer : customization.layerSettings.keySet()) {
                        Map<EnumSkinLayer, List<DragonPart>> partMap = DragonPartLoader.DRAGON_PARTS.get(type);

                        // Convert the numbered 'EXTRA' layer to the generic 'EXTRA' layer
                        EnumSkinLayer actualLayer = EnumSkinLayer.valueOf(layer.getNameUpperCase());
                        List<DragonPart> parts = partMap.get(actualLayer);

                        if (parts != null) {
                            String partKey = DefaultPartLoader.getDefaultPartKey(type, dragonStage.location(), layer);
                            LayerSettings settings = customization.layerSettings.get(layer).get();

                            for (DragonPart part : parts) {
                                if (part.key().equals(partKey)) {
                                    settings.hue = settings.modifiedColor ? part.averageHue() - settings.hue - 0.5f : part.averageHue();

                                    if (settings.hue > 1) {
                                        settings.hue -= 1;
                                    } else if (settings.hue < 0) {
                                        settings.hue += 1;
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        presets.version = 3;
    }
}
