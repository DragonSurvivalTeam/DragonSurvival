package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class PlayerData implements INBTSerializable<CompoundTag> {
    public boolean enabledDragonSoulPlacement = true;

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(ENABLED_DRAGON_SOUL_PLACEMENT, enabledDragonSoulPlacement);
        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag nbt) {
        enabledDragonSoulPlacement = nbt.getBoolean(ENABLED_DRAGON_SOUL_PLACEMENT);
    }

    private final String ENABLED_DRAGON_SOUL_PLACEMENT = "enabledDragonSoulPlacement";
}
