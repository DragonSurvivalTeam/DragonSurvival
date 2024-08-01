package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SavedSkinPresets;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import net.minecraftforge.common.util.Lazy;

import java.util.HashMap;
import java.util.Locale;

public class SkinPortingSystem {
    public static SavedSkinPresets upgrade(SavedSkinPresets presets) {
        if (presets.version == 0) {
            upgrade0to3(presets);
        }
        return presets;
    }

    public static void upgrade0to3(SavedSkinPresets presets) {
        for (String type : presets.skinPresets.keySet()) {
            for (int age : presets.skinPresets.get(type).keySet()) {
                for(DragonLevel level : DragonLevel.values()){
                    SkinPreset.SkinAgeGroup sag = presets.skinPresets.get(type).get(age).skinAges.getOrDefault(level, Lazy.of(()->new SkinPreset.SkinAgeGroup(level))).get();
                    for (EnumSkinLayer layer : sag.layerSettings.keySet()) {
                        LayerSettings settings = sag.layerSettings.get(layer).get();

                        String part = DragonEditorRegistry.getDefaultPart(DragonTypes.getStatic(type), level, layer);
                        EnumSkinLayer trueLayer = EnumSkinLayer.valueOf(layer.getNameUpperCase());
                        HashMap<EnumSkinLayer, DragonEditorObject.Texture[]> hm = DragonEditorRegistry.CUSTOMIZATIONS.get(type.toUpperCase(Locale.ENGLISH));
                        if (hm != null) {
                            DragonEditorObject.Texture[] texts = hm.get(trueLayer);
                            if (texts != null) {
                                for (DragonEditorObject.Texture text : texts) {
                                    if (text.key.equals(part)) {
                                        settings.hue = settings.modifiedColor ? text.average_hue - settings.hue - 0.5f : text.average_hue;
                                        if (settings.hue > 1) { settings.hue -= 1; }
                                        else if (settings.hue < 0) { settings.hue += 1; }
                                        break;
                                    }
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
