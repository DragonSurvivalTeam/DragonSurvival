package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** Part data read from the 'skin/parts/*.json' files */
public record DragonPart(String key, String texture, List<String> bodies, float averageHue, boolean isColorable, boolean isRandom, boolean isHueRandom) {
    public static final String KEY = "key";
    public static final String TEXTURE = "texture";
    public static final String BODIES = "bodies";
    public static final String AVERAGE_HUE = "average_hue";
    public static final String IS_COLORABLE = "colorable";
    public static final String IS_RANDOM = "random";
    public static final String IS_HUE_RANDOM = "randomHue";

    @SuppressWarnings("SimplifiableConditionalExpression") // ignore for clarity
    public static DragonPart load(final JsonObject json) {
        String key = json.get(KEY).getAsString();
        String texture = json.get(TEXTURE).getAsString();
        float averageHue = json.get(AVERAGE_HUE).getAsFloat();

        List<String> bodies = new ArrayList<>();

        if (json.has(BODIES)) {
            json.get(BODIES).getAsJsonArray().forEach(entry -> bodies.add(entry.getAsString()));
        }

        boolean isColorable = json.has(IS_COLORABLE) ? json.get(IS_COLORABLE).getAsBoolean() : true;
        boolean isRandom = json.has(IS_RANDOM) ? json.get(IS_RANDOM).getAsBoolean() : true;
        boolean isHueRandom = json.has(IS_HUE_RANDOM) ? json.get(IS_HUE_RANDOM).getAsBoolean() : true;

        return new DragonPart(key, texture, !bodies.isEmpty() ? bodies : null, averageHue, isColorable, isRandom, isHueRandom);
    }
}