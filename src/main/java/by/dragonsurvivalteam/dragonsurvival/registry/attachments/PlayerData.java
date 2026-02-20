package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PlayerData implements ValueIOSerializable {
    public boolean enabledDragonSoulPlacement = true;

    /** Tracks which keys are currently held down */
    private final Set<String> keys = new HashSet<>();

    /* TODO :: add key override system
        to allow players to keybind ability key press / release triggers to different keys
        - either global (client config)
        - or per ability (client resource -> dragonsurvival/key_overrides)
        - the triggers will have a flag whether to respect this override or not
    */

    public boolean updateKey(final String key, final boolean isDown) {
        return isDown ? keys.add(key) : keys.remove(key);
    }

    public void clearKeys() {
        keys.clear();
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.putBoolean(ENABLED_DRAGON_SOUL_PLACEMENT, enabledDragonSoulPlacement);
        ValueOutput keys = valueOutput.child(KEYS);
        this.keys.forEach(key -> keys.putBoolean(String.valueOf(key), true));
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        this.keys.clear();
        enabledDragonSoulPlacement = valueInput.getBooleanOr(ENABLED_DRAGON_SOUL_PLACEMENT, false);
        this.keys.addAll(valueInput.child(KEYS).orElseThrow().keySet());
    }

    private final String ENABLED_DRAGON_SOUL_PLACEMENT = "enabled_dragon_soul_placement";
    private final String KEYS = "keys";
}
