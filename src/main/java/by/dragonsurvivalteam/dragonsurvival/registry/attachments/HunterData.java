package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class HunterData implements INBTSerializable<CompoundTag> {
    /** Translucent rendering in the inventory screen leads to issues (invisible model) */
    public boolean isBeingRenderedInInventory;

    /** Only needs to be updated on effect removal (server -> client) */
    private int hunterStacks;

    public void modifyHunterStacks(int modification) {
        hunterStacks = Math.clamp(hunterStacks + modification, 0, HunterHandler.getMaxStacks());
    }

    public boolean hasMaxHunterStacks() {
        return hunterStacks == HunterHandler.getMaxStacks();
    }

    public boolean hasHunterStacks() {
        return hunterStacks > 0;
    }

    public void clearHunterStacks() {
        hunterStacks = 0;
    }

    public int getHunterStacks() {
        return hunterStacks;
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) { /* Nothing to do */ }
}
