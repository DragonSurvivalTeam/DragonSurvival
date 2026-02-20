package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.Copyable;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.HashMap;
import java.util.Map;

/** Saved customization per dragon level */
public class DragonStageCustomization implements ValueIOSerializable, Copyable<DragonStageCustomization> {
    public static final String HAS_WINGS = "wings";
    public static final String IS_DEFAULT_SKIN = "defaultSkin";

    public final HashMap<SkinLayer, Lazy<LayerSettings>> layerSettings = new HashMap<>();

    public boolean wings = true;
    public boolean defaultSkin;

    public DragonStageCustomization(final ResourceKey<DragonStage> stage, final ResourceKey<DragonSpecies> species, final Identifier customModel) {
        this();

        for (SkinLayer layer : SkinLayer.values()) {
            // Convert the numbered 'EXTRA' layer to the generic 'EXTRA' layer
            SkinLayer actualLayer = SkinLayer.valueOf(layer.getNameUpperCase());

            Map<String, DragonPart> parts = DragonPartLoader.getDragonParts(actualLayer, species, null);
            String partKey = DefaultPartLoader.getDefaultPartKey(species, stage, Either.right(customModel), layer);
            DragonPart defaultPart = parts.get(partKey);

            if (defaultPart != null) {
                layerSettings.put(layer, Lazy.of(() -> new LayerSettings(partKey, defaultPart.averageHue(), defaultPart.isGlowing())));
            } else {
                // TODO :: Why exactly are we doing this?
                layerSettings.put(layer, Lazy.of(() -> new LayerSettings(partKey, 0.5f, false)));
            }
        }
    }

    public DragonStageCustomization() {
        for (SkinLayer layer : SkinLayer.values()) {
            layerSettings.computeIfAbsent(layer, key -> Lazy.of(LayerSettings::new));
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean(HAS_WINGS, wings);
        output.putBoolean(IS_DEFAULT_SKIN, defaultSkin);

        for (SkinLayer layer : SkinLayer.values()) {
            output.putChild(layer.name(), layerSettings.getOrDefault(layer, Lazy.of(LayerSettings::new)).get());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        wings = input.getBooleanOr(HAS_WINGS, false);
        defaultSkin = input.getBooleanOr(IS_DEFAULT_SKIN, false);

        for (SkinLayer layer : SkinLayer.values()) {
            layerSettings.put(layer, Lazy.of(() -> {
                LayerSettings settings = new LayerSettings();
                ValueInput layerData = input.child(layer.name()).orElseThrow();
                settings.deserialize(layerData);
                return settings;
            }));
        }
    }
}