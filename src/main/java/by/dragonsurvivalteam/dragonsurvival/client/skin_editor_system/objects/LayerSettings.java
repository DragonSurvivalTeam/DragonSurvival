package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

/** Entries within the 'saved_customizations.json' file */
public class LayerSettings implements ValueIOSerializable {
    public static final String PART_KEY = "part_key";
    public static final String HUE = "hue";
    public static final String SATURATION = "saturation";
    public static final String BRIGHTNESS = "brightness";
    public static final String IS_COLOR_MODIFIED = "is_modified";
    public static final String IS_GLOWING = "is_glowing";

    public String partKey;

    public float hue;
    public float saturation;
    public float brightness;

    public boolean isModified;
    public boolean isGlowing;

    public LayerSettings() {
        this(DefaultPartLoader.NO_PART, 0.5f, false);
    }

    public LayerSettings(final String partKey, final float defaultHue, final boolean isGlowing) {
        this.partKey = partKey;
        this.hue = defaultHue;
        this.saturation = 0.5f;
        this.brightness = 0.5f;
        this.isGlowing = isGlowing;
    }

    @Override
    public void serialize(@NotNull ValueOutput output) {
        output.putString(PART_KEY, partKey);

        output.putFloat(HUE, hue);
        output.putFloat(SATURATION, saturation);
        output.putFloat(BRIGHTNESS, brightness);

        output.putBoolean(IS_COLOR_MODIFIED, isModified);
        output.putBoolean(IS_GLOWING, isGlowing);
    }

    @Override
    public void deserialize(@NotNull ValueInput input) {
        partKey = input.getString(PART_KEY).orElseThrow();

        hue = input.getFloatOr(HUE, 0.0f);
        saturation = input.getFloatOr(SATURATION, 0.0f);
        brightness = input.getFloatOr(BRIGHTNESS, 0.0f);

        isModified = input.getBooleanOr(IS_COLOR_MODIFIED, false);
        isGlowing = input.getBooleanOr(IS_GLOWING, false);
    }
}