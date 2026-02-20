package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SkinData implements ValueIOSerializable {
    public static final String RENDER_CUSTOM_SKIN = "render_custom_skin";

    public final Map<ResourceKey<DragonStage>, Boolean> recompileSkin = new HashMap<>();
    public final Map<ResourceKey<DragonStage>, Boolean> isCompiled = new HashMap<>();
    public final Lazy<HashMap<ResourceKey<DragonSpecies>, SkinPreset>> skinPresets = Lazy.of(this::initialize);

    public boolean renderCustomSkin;
    public boolean blankSkin;

    public HashMap<ResourceKey<DragonSpecies>, SkinPreset> initialize() {
        if (FMLLoader.getCurrent().getDist().isDedicatedServer()) {
            // Don't try to initialize default data for skin presets when asked by the server, as the server doesn't have the part data
            // to construct this data anyways
            return new HashMap<>();
        }

        HashMap<ResourceKey<DragonSpecies>, SkinPreset> presets = new HashMap<>();

        for (ResourceKey<DragonSpecies> dragonSpecies : ResourceHelper.keys(null, DragonSpecies.REGISTRY)) {
            SkinPreset preset = new SkinPreset();
            preset.initDefaults(ResourceHelper.get(null, dragonSpecies).get(), null);
            presets.put(dragonSpecies, preset);
        }

        return presets;
    }

    public void compileSkin(final ResourceKey<DragonStage> dragonStage) {
        recompileSkin.put(dragonStage, true);
    }

    public Lazy<DragonStageCustomization> get(final ResourceKey<DragonSpecies> dragonSpecies, final ResourceKey<DragonStage> dragonStage) {
        return skinPresets.get().get(dragonSpecies).get(dragonStage);
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.putBoolean(RENDER_CUSTOM_SKIN, renderCustomSkin);

        for (Map.Entry<ResourceKey<DragonSpecies>, SkinPreset> entry : skinPresets.get().entrySet()) {
            valueOutput.putChild(entry.getKey().identifier().toString(), entry.getValue());
        }
    }

    // Used when loading the dragon handler data to properly setup skin data on the client if the server sends empty skin data
    public void deserialize(final ValueInput valueInput, @Nullable final Holder<DragonBody> currentBody) {
        renderCustomSkin = valueInput.getBooleanOr(RENDER_CUSTOM_SKIN, false);

        for (String key : valueInput.keySet()) {
            ResourceKey<DragonSpecies> dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, Identifier.parse(key));

            if (valueInput.lookup().lookup(DragonSpecies.REGISTRY).flatMap(lookup -> lookup.get(dragonSpecies)).isPresent()) {
                SkinPreset preset = new SkinPreset();
                preset.deserialize(valueInput.childOrEmpty(key));

                if (preset.isEmpty()) {
                    Holder<DragonSpecies> speciesHolder = ResourceHelper.get(valueInput.lookup(), dragonSpecies).get();

                    if (currentBody != null && speciesHolder.value().isValidForBody(currentBody)) {
                        preset.initDefaults(speciesHolder, currentBody.value().model());
                    }

                    // Don't bother initializing defaults if the player isn't even a valid body type for the species in question
                }

                skinPresets.get().put(dragonSpecies, preset);
            }
        }
    }

    @Override
    public void deserialize(ValueInput valueInput) {
        renderCustomSkin = valueInput.getBooleanOr(RENDER_CUSTOM_SKIN, false);

        for (String key : valueInput.keySet()) {
            ResourceKey<DragonSpecies> dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, Identifier.parse(key));

            if (valueInput.lookup().lookup(DragonSpecies.REGISTRY).flatMap(lookup -> lookup.get(dragonSpecies)).isPresent()) {
                SkinPreset preset = new SkinPreset();
                preset.deserialize(valueInput.childOrEmpty(key));
                skinPresets.get().put(dragonSpecies, preset);
            }
        }
    }
}