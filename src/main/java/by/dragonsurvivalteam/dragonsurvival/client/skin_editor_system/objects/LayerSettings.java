package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

/** Entries within the 'saved_customizations.json' file */
public class LayerSettings implements INBTSerializable<CompoundTag> {
    public static final String PART_KEY = "part_key";
    public static final String HUE = "hue";
    public static final String SATURATION = "saturation";
    public static final String BRIGHTNESS = "brightness";
    public static final String IS_COLOR_MODIFIED = "modifiedColor";
    public static final String IS_GLOWING = "glowing";

    public String partKey;

    public float hue;
    public float saturation;
    public float brightness;

    public boolean modifiedColor;
    public boolean glowing;

    public LayerSettings() {
        this(DefaultPartLoader.NO_PART, 0.5f);
    }

    public LayerSettings(final String partKey, float defaultHue) {
        this.partKey = partKey;
        this.hue = defaultHue;
        this.saturation = 0.5f;
        this.brightness = 0.5f;
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(PART_KEY, partKey);

        nbt.putFloat(HUE, hue);
        nbt.putFloat(SATURATION, saturation);
        nbt.putFloat(BRIGHTNESS, brightness);

        nbt.putBoolean(IS_COLOR_MODIFIED, modifiedColor);
        nbt.putBoolean(IS_GLOWING, glowing);
        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        partKey = tag.getString(PART_KEY);

        hue = tag.getFloat(HUE);
        saturation = tag.getFloat(SATURATION);
        brightness = tag.getFloat(BRIGHTNESS);

        modifiedColor = tag.getBoolean(IS_COLOR_MODIFIED);
        glowing = tag.getBoolean(IS_GLOWING);
    }
}