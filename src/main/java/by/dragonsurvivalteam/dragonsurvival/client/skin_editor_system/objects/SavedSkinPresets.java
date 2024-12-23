package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;

public class SavedSkinPresets {
    /** The current 'saved_customizations.json' values */
    public final HashMap</* Dragon type */ ResourceKey<DragonType>, HashMap</* Save slot */ Integer, SkinPreset>> skinPresets = new HashMap<>();

    public int version;
}