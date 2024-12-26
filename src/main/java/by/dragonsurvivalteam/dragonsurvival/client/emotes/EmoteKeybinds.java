package by.dragonsurvivalteam.dragonsurvival.client.emotes;

import java.util.ArrayList;
import java.util.List;

public class EmoteKeybinds {
    public List<EmoteKeybind> EMOTE_KEYBINDS = new ArrayList<>();
    public EmoteKeybinds() {}
    public record EmoteKeybind(Integer keyCode, String emote) {}

    public void remove(String emote) {
        EMOTE_KEYBINDS.removeIf(emoteKeybind -> emoteKeybind.emote().equals(emote));
    }

    public void put(Integer keyCode, String emote) {
        if(get(keyCode) != null) {
            remove(get(keyCode));
        }

        EMOTE_KEYBINDS.add(new EmoteKeybind(keyCode, emote));
    }

    public String get(Integer keyCode) {
        for (EmoteKeybind emoteKeybind : EMOTE_KEYBINDS) {
            if (emoteKeybind.keyCode().equals(keyCode)) {
                return emoteKeybind.emote();
            }
        }
        return null;
    }

    public int getKey(String emote) {
        for (EmoteKeybind emoteKeybind : EMOTE_KEYBINDS) {
            if (emoteKeybind.emote().equals(emote)) {
                return emoteKeybind.keyCode();
            }
        }

        return 0;
    }

    public boolean contains(String emote) {
        for (EmoteKeybind emoteKeybind : EMOTE_KEYBINDS) {
            if (emoteKeybind.emote().equals(emote)) {
                return true;
            }
        }
        return false;
    }
}
