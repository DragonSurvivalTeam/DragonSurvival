package by.dragonsurvivalteam.dragonsurvival.client.emotes;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonEmoteScreen;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class EmoteKeybinds {
    private final Map<Integer, String> emoteKeybinds = new HashMap<>();

    public @Nullable String get(int keyCode) {
        return emoteKeybinds.get(keyCode);
    }

    public void put(int keyCode, final String emote) {
        // Remove the previous keybind if it exists
        emoteKeybinds.entrySet().removeIf(entry -> entry.getValue().equals(emote));
        emoteKeybinds.put(keyCode, emote);
    }

    public void remove(final String emote) {
        emoteKeybinds.remove(getKey(emote));
    }

    public boolean contains(final String emote) {
        return getKey(emote) != DragonEmoteScreen.NO_KEY;
    }

    public int getKey(final String emote) {
        for (Map.Entry<Integer, String> entry : emoteKeybinds.entrySet()) {
            String mappedEmote = entry.getValue();

            if (mappedEmote.equals(emote)) {
                return entry.getKey();
            }
        }

        return DragonEmoteScreen.NO_KEY;
    }

    public void clear() {
        emoteKeybinds.clear();
    }
}
