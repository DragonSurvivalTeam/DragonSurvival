package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PlayerData implements INBTSerializable<CompoundTag> {
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
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(ENABLED_DRAGON_SOUL_PLACEMENT, enabledDragonSoulPlacement);

        CompoundTag keys = new CompoundTag();
        this.keys.forEach(key -> keys.putBoolean(String.valueOf(key), true));
        nbt.put(KEYS, keys);

        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag nbt) {
        this.keys.clear();

        // TODO 1.22 :: check not needed anymore, only exists due to a rename of the field
        if (nbt.contains(ENABLED_DRAGON_SOUL_PLACEMENT)) {
            enabledDragonSoulPlacement = nbt.getBoolean(ENABLED_DRAGON_SOUL_PLACEMENT);
        }

        CompoundTag keys = nbt.getCompound(KEYS);
        this.keys.addAll(keys.getAllKeys());
    }

    private final String ENABLED_DRAGON_SOUL_PLACEMENT = "enabled_dragon_soul_placement";
    private final String KEYS = "keys";
}
