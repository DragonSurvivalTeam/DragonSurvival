package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
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
import java.util.Map;

public class SkinData implements INBTSerializable<CompoundTag> {
    public static final String RENDER_CUSTOM_SKIN = "render_custom_skin";

    public final Map<ResourceKey<DragonStage>, Boolean> recompileSkin = new HashMap<>();
    public final Map<ResourceKey<DragonStage>, Boolean> isCompiled = new HashMap<>();
    public final Lazy<HashMap<ResourceKey<DragonSpecies>, SkinPreset>> skinPresets = Lazy.of(this::initialize);

    public boolean renderCustomSkin;
    public boolean blankSkin;

    public HashMap<ResourceKey<DragonSpecies>, SkinPreset> initialize() {
        HashMap<ResourceKey<DragonSpecies>, SkinPreset> presets = new HashMap<>();

        for (ResourceKey<DragonSpecies> dragonSpecies : ResourceHelper.keys(null, DragonSpecies.REGISTRY)) {
            SkinPreset preset = new SkinPreset();
            preset.setSpecies(dragonSpecies);
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
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(RENDER_CUSTOM_SKIN, renderCustomSkin);

        for (Map.Entry<ResourceKey<DragonSpecies>, SkinPreset> entry : skinPresets.get().entrySet()) {
            tag.put(entry.getKey().location().toString(), entry.getValue().serializeNBT(provider));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        renderCustomSkin = tag.getBoolean(RENDER_CUSTOM_SKIN);

        for (String key : tag.getAllKeys()) {
            ResourceKey<DragonSpecies> dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(key));

            if (provider.lookup(DragonSpecies.REGISTRY).flatMap(lookup -> lookup.get(dragonSpecies)).isPresent()) {
                SkinPreset preset = new SkinPreset();
                preset.deserializeNBT(provider, tag.getCompound(key));
                skinPresets.get().put(dragonSpecies, preset);
            }
        }
    }
}