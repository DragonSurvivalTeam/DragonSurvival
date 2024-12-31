package by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SkinCap extends SubCap {
    public static final String RENDER_CUSTOM_SKIN = "render_custom_skin";

    public Map<ResourceKey<DragonStage>, Boolean> recompileSkin = new HashMap<>();
    public Map<ResourceKey<DragonStage>, Boolean> isCompiled = new HashMap<>();

    public Lazy<HashMap<ResourceKey<DragonSpecies>, SkinPreset>> skinPresets = Lazy.of(this::initialize);

    public boolean renderCustomSkin;
    public boolean blankSkin;

    public SkinCap(DragonStateHandler handler) {
        super(handler);
    }

    public HashMap<ResourceKey<DragonSpecies>, SkinPreset> initialize() {
        HashMap<ResourceKey<DragonSpecies>, SkinPreset> presets = new HashMap<>();
        for (ResourceKey<DragonSpecies> dragonSpecies : ResourceHelper.keys(null, DragonSpecies.REGISTRY)) {
            presets.put(dragonSpecies, new SkinPreset());
        }
        return presets;
    }

    public void compileSkin(final Holder<DragonStage> dragonStage) {
        recompileSkin.put(dragonStage.getKey(), true);
    }

    public Lazy<DragonStageCustomization> get(final ResourceKey<DragonSpecies> dragonSpecies, final ResourceKey<DragonStage> dragonStage) {
        return skinPresets.get().get(dragonSpecies).get(dragonStage);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(RENDER_CUSTOM_SKIN, renderCustomSkin);
        for(Map.Entry<ResourceKey<DragonSpecies>, SkinPreset> entry : skinPresets.get().entrySet()) {
            tag.put(entry.getKey().location().toString(), entry.getValue().serializeNBT(provider));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag tag) {
        renderCustomSkin = tag.getBoolean(RENDER_CUSTOM_SKIN);
        for(String key : tag.getAllKeys()) {
            SkinPreset preset = new SkinPreset();
            preset.deserializeNBT(provider, tag.getCompound(key));
            ResourceKey<DragonSpecies> dragonSpecies = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(key));
            if(provider.lookup(DragonSpecies.REGISTRY).flatMap(lookup -> lookup.get(dragonSpecies)).isPresent()) {
                skinPresets.get().put(dragonSpecies, preset);
            }
        }
    }
}